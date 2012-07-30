//#define UseBoost
//#define UseMPI
/*
 * Server.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "Server.h"
//#include "SpaceTimeDataControllerLocal.h"

using namespace std;
using namespace MPI;
namespace TraceviewerServer
{
	bool Compression = true;
	int MainPort = 21590;
	int XMLPort = 0;

	static SpaceTimeDataControllerLocal* STDCL;

	Server::Server()
	{
		// TODO Auto-generated constructor stub

	}

	Server::~Server()
	{
		delete (STDCL);
	}
	int Server::main(int argc, char *argv[])
	{

		DataSocketStream* socketptr;
		try
		{
//TODO: Change the port to 21591 because 21590 has some other use...
			//DataSocketStream CLsocket = new DataSocketStream(21590);
			socketptr = new DataSocketStream(MainPort, true);
			/*
			 ip::tcp::socket CLsocket(io_service);
			 //CLsocket.open(ip::tcp::v4());
			 acceptor.accept(CLsocket);
			 socketptr = (DataSocketStream*) &CLsocket;
			 */

			//socketptr = &CLsocket;
		} catch (std::exception& e)
		{
			std::cerr << e.what() << std::endl;
			return -3;
		}
		cout << "Received connection" << endl;

		//vector<char> test(4);
		//as::read(*socketptr, as::buffer(test));

		ParseOpenDB(socketptr);

		if (STDCL == NULL)
		{
			cout << "Could not open database" << endl;
			SendDBOpenFailed(socketptr);
		}
		else
		{
			cout << "Database opened" << endl;
			SendDBOpenedSuccessfully(socketptr);
		}

		int Message = socketptr->ReadInt();
		if (Message == Constants::INFO)
			ParseInfo(socketptr);
		else
			cerr << "Did not receive info packet" << endl;

		bool EndingConnection = false;
		while (!EndingConnection)
		{
			int NextCommand = socketptr->ReadInt();
			switch (NextCommand)
			{
				case Constants::DATA:
					GetAndSendData(socketptr);
					break;
				case Constants::DONE:
					EndingConnection = true;
					break;
				default:
					cerr << "Unknown command received" << endl;
					return (-7);
					break;
			}
		}

		return 0;
	}
	void Server::ParseInfo(DataSocketStream* socket)
	{

		Long minBegTime = socket->ReadLong();
		Long maxEndTime = socket->ReadLong();
		int headerSize = socket->ReadInt();
		STDCL->SetInfo(minBegTime, maxEndTime, headerSize);
#ifdef UseMPI
		MPICommunication::CommandMessage Info;
		Info.Command = Constants::INFO;
		Info.minfo.minBegTime = minBegTime;
		Info.minfo.maxEndTime = maxEndTime;
		Info.minfo.headerSize = headerSize;
		COMM_WORLD.Bcast(&Info, sizeof(Info), MPI_PACKED, MPICommunication::SOCKET_SERVER);
#endif
	}
	void Server::SendDBOpenedSuccessfully(DataSocketStream* socket)
	{
//TODO: Use the variable XML Port & obtain an open port if it is -1
		socket->WriteInt(Constants::DBOK);

		//int port = 2224;

		//socket->WriteInt(port);
		DataSocketStream XmlSocket(XMLPort, false);
		int port = XmlSocket.GetPort();
		socket->WriteInt(port);

		int NumFiles = STDCL->GetHeight();
		socket->WriteInt(NumFiles);

		int CompressionType; //This is an int so that it is possible to have different compression algorithms in the future. For now, it is just 0=no compression, 1= normal compression
		CompressionType = Compression ? 1 : 0;
		socket->WriteInt(CompressionType);

		//Send ValuesX
		const string* ValuesX = STDCL->GetValuesX();
		for (int i = 0; i < NumFiles; i++)
		{
			socket->WriteString(ValuesX[i]);
		}

		socket->Flush();

		cout << "Waiting to send XML on port " << port << ". Num traces was "
				<< STDCL->GetHeight() << endl;

		XmlSocket.AcceptS();
		int fd = XmlSocket.GetDescriptor();
		SendXML(fd);

		cout << "XML Sent" << endl;
	}

	void Server::SendXML(int FileDescriptor)
	{
		FILE* in = fopen(STDCL->GetExperimentXML().c_str(), "r");
		gzFile out = gzdopen(FileDescriptor, "w");

		int size = TraceviewerServer::FileUtils::GetFileSize(STDCL->GetExperimentXML());

#define CHUNK 0x40000//256k
		int BytesProcessed = 0;
		char Buffer[CHUNK];
		while (BytesProcessed < size)
		{
			int br = fread(Buffer, 1, CHUNK, in);
			gzwrite(out, Buffer, br);
			BytesProcessed += br;
		}
		gzflush(out, Z_FINISH);
		gzclose(out);
		fclose(in);
	}

	void Server::ParseOpenDB(DataSocketStream* receiver)
	{

		if (false) //(!receiver->is_open())
			cout << "Socket not open!" << endl;
		int Command = receiver->ReadInt();
		if (Command != Constants::OPEN)
			cerr << "Expected an open command, got " << Command << endl;
		string PathToDB = receiver->ReadString();
#ifdef UseMPI
		MPICommunication::CommandMessage cmdPathToDB;
		cmdPathToDB.Command = Constants::OPEN;
		if (PathToDB.length() > 1023)
		{
			cerr << "Path too long" << endl;
			throw 1008;
		}
		copy(PathToDB.begin(), PathToDB.end(), cmdPathToDB.ofile.Path);
		cmdPathToDB.ofile.Path[PathToDB.size()] = '\0';

		COMM_WORLD.Bcast(&cmdPathToDB, sizeof(cmdPathToDB), MPI_PACKED,
				MPICommunication::SOCKET_SERVER);
#endif
		LocalDBOpener DBO;
		cout << "Opening database: " << PathToDB << endl;
		STDCL = DBO.OpenDbAndCreateSTDC(PathToDB);

	}

	void Server::SendDBOpenFailed(DataSocketStream* socket)
	{
		socket->WriteInt(Constants::NODB);
		socket->WriteInt(0);
		socket->Flush();
	}

#define ISN(g) (g<0)

	void Server::GetAndSendData(DataSocketStream* Stream)
	{

		int processStart = Stream->ReadInt();
		int processEnd = Stream->ReadInt();
		double timeStart = Stream->ReadDouble();
		double timeEnd = Stream->ReadDouble();
		int verticalResolution = Stream->ReadInt();
		int horizontalResolution = Stream->ReadInt();

		if (ISN(processStart) || ISN(processEnd) || (processStart > processEnd)
				|| ISN(verticalResolution) || ISN(horizontalResolution)
				|| (timeEnd < timeStart))
		{
			cerr
					<< "A data request with invalid parameters was received. This sometimes happens if the client shuts down in the middle of a request. The server will now shut down."
					<< endl;
			throw(-99);
		}

#ifndef UseMPI
		ImageTraceAttributes correspondingAttributes;

		correspondingAttributes.begProcess = processStart;
		correspondingAttributes.endProcess = processEnd;
		correspondingAttributes.numPixelsH = horizontalResolution;
		correspondingAttributes.numPixelsV = verticalResolution;
		// Time start and Time end?? Should actually be longs instead of
		// doubles????
		//double timeSpan = timeEnd - timeStart;
		correspondingAttributes.begTime = (long) timeStart;
		correspondingAttributes.endTime = (long) timeEnd;
		correspondingAttributes.lineNum = 0;
		STDCL->Attributes = &correspondingAttributes;

		// TODO: Make this so that the Lines get sent as soon as they are
		// filled.

		STDCL->FillTraces(-1, true);
#else
		MPICommunication::CommandMessage toBcast;
		toBcast.Command = Constants::DATA;
		toBcast.gdata.processStart = processStart;
		toBcast.gdata.processEnd = processEnd;
		toBcast.gdata.timeStart = timeStart;
		toBcast.gdata.timeEnd = timeEnd;
		toBcast.gdata.verticalResolution = verticalResolution;
		toBcast.gdata.horizontalResolution = horizontalResolution;
		COMM_WORLD.Bcast(&toBcast, sizeof(toBcast), MPI_PACKED,
				MPICommunication::SOCKET_SERVER);
#endif

		Stream->WriteInt(Constants::HERE);
		Stream->Flush();


		/*#define stWr(type,val) CompL.Write##type((val))
		 #else
		 #define stWr(type,val) Stream->Write##type((val))
		 #endif*/

#ifndef UseMPI

		for (int i = 0; i < STDCL->TracesLength; i++)
		{

			ProcessTimeline* T = STDCL->Traces[i];
			DataSender->WriteInt( T->Line());
			vector<TimeCPID> data = T->Data->ListCPID;
			DataSender->WriteInt( data.size());
			DataSender->WriteDouble( data[0].Timestamp);
			// Begin time
			DataSender->WriteDouble( data[data.size() - 1].Timestamp);
			//End time

			vector<TimeCPID>::iterator it;
			cout << "Sending process timeline with " << data.size() << " entries" << endl;
			for (it = data.begin(); it != data.end(); ++it)
			{
				DataSender->WriteInt( it->CPID);
			}
		}
		DataSender->Flush();

#else
		int RanksDone = 1;
		int Size = COMM_WORLD.Get_size();

		while (RanksDone < Size)
		{
			MPICommunication::ResultMessage msg;
			COMM_WORLD.Recv(&msg, sizeof(msg), MPI_PACKED, MPI_ANY_SOURCE, MPI_ANY_TAG);
			if (msg.Tag == Constants::SLAVE_REPLY)
			{

				//Stream->WriteInt(msg.Data.Line);
				Stream->WriteInt(msg.Data.Line);
				Stream->WriteInt(msg.Data.Entries);
				Stream->WriteDouble(msg.Data.Begtime); // Begin time
				Stream->WriteDouble(msg.Data.Endtime); //End time
				Stream->WriteInt(msg.Data.CompressedSize);

				char CompressedTraceLine[msg.Data.CompressedSize];
				COMM_WORLD.Recv(CompressedTraceLine, msg.Data.CompressedSize, MPI_BYTE, msg.Data.RankID,
						MPI_ANY_TAG);

				Stream->WriteRawData(CompressedTraceLine, msg.Data.CompressedSize);

				//delete(&msg);
				Stream->Flush();
			}
			else if (msg.Tag == Constants::SLAVE_DONE)
			{
				cout << "Rank " << msg.Done.RankID << " done" << endl;
				RanksDone++;
			}
		}

#endif
		cout << "Data sent" << endl;
	}

} /* namespace TraceviewerServer */
