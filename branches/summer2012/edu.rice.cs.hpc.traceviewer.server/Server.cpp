/*
 * Server.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */
#include "Server.h"
//#include "SpaceTimeDataControllerLocal.h"

namespace as = boost::asio;
namespace ip = boost::asio::ip;
using namespace std;
namespace TraceviewerServer
{

	static SpaceTimeDataControllerLocal* STDCL;

	Server::Server()
	{
		// TODO Auto-generated constructor stub

	}

	Server::~Server()
	{
		// TODO Auto-generated destructor stub
	}
	int Server::main(int argc, char *argv[])
	{


		DataSocketStream* socketptr;
		try
		{
//TODO: Change the port to 21591 because 21590 has some other use...
				DataSocketStream CLsocket(21590);
			cout << "Waiting for connection" << endl;
			/*
			 ip::tcp::socket CLsocket(io_service);
			 //CLsocket.open(ip::tcp::v4());
			 acceptor.accept(CLsocket);
			 socketptr = (DataSocketStream*) &CLsocket;
			 */

			socketptr = &CLsocket;
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
			//Send no DB
		}
		else
		{
			cout << "Database opened" << endl;
			SendDBOpenedSuccessfully(socketptr);
		}

		int Message = socketptr->ReadInt();
		if (Message == INFO)
			ParseInfo(socketptr);
		else
			cerr << "Did not receive info packet" << endl;

		bool EndingConnection = false;
		while (!EndingConnection)
		{
			int NextCommand = socketptr->ReadInt();
			switch (NextCommand)
			{
			case DATA:
				GetAndSendData(socketptr);
				break;
			case DONE:
				EndingConnection = true;
				break;
			default:
				cerr << "Unknown command received" << endl;
				exit(-7);
				break;
			}
		}

		return 0;
	}
	void Server::ParseInfo(DataSocketStream* socket)
	{

		long minBegTime = socket->ReadLong();
		long maxEndTime = socket->ReadLong();
		int headerSize = socket->ReadInt();
		STDCL->SetInfo(minBegTime, maxEndTime, headerSize);
	}
	void Server::SendDBOpenedSuccessfully(DataSocketStream* socket)
	{

		socket->WriteInt(DBOK);

		as::io_service XMLio_service;
		ip::tcp::acceptor XMLacceptor(XMLio_service, ip::tcp::endpoint(ip::tcp::v4(), 0));
		int port = XMLacceptor.local_endpoint().port();

		socket->WriteInt(port);

		socket->WriteInt(STDCL->GetHeight());

		socket->Flush();

		cout << "Waiting to send XML on port " << port << ". Num traces was "
				<< STDCL->GetHeight() << endl;

		ip::tcp::iostream XMLstr;
		XMLacceptor.accept(*XMLstr.rdbuf());
		SendXML(&XMLstr);

		cout << "XML Sent" << endl;
	}

	void Server::SendXML(ip::tcp::iostream* XMLSocket)
	{
		string PathToFile = STDCL->GetExperimentXML();
		cout << "Compressing XML File from " << PathToFile << endl;
		std::ifstream XMLFile(PathToFile.c_str(),
				std::ios_base::in | std::ios_base::binary);

		boost::iostreams::filtering_streambuf<boost::iostreams::output> out;

		out.push(boost::iostreams::gzip_compressor());
		out.push(*XMLSocket);
		boost::iostreams::copy(XMLFile, out);
		if (!XMLSocket->good())
			cerr << "Sending XML failed" << endl;
		XMLSocket->flush();
	}

	void Server::ParseOpenDB(DataSocketStream* receiver)
	{

		if (false)//(!receiver->is_open())
			cout << "Socket not open!" << endl;
		int Command = receiver->ReadInt();
		if (Command != OPEN)
			cerr << "Expected an open command, got " << Command << endl;
		string PathToDB = receiver->ReadString();
		LocalDBOpener DBO;
		STDCL = DBO.OpenDbAndCreateSTDC(PathToDB);

	}

	void Server::GetAndSendData(DataSocketStream* Stream)
	{

		int processStart = Stream->ReadInt();
		int processEnd = Stream->ReadInt();
		double timeStart = Stream->ReadDouble();
		double timeEnd = Stream->ReadDouble();
		int verticalResolution = Stream->ReadInt();
		int horizontalResolution = Stream->ReadInt();
		ImageTraceAttributes correspondingAttributes;

		correspondingAttributes.begProcess = processStart;
		correspondingAttributes.endProcess = processEnd;
		correspondingAttributes.numPixelsH = horizontalResolution;
		correspondingAttributes.numPixelsV = verticalResolution;
		// Time start and Time end?? Should actually be longs instead of
		// doubles????
		double timeSpan = timeEnd - timeStart;
		correspondingAttributes.begTime = 0;
		correspondingAttributes.endTime = (long) timeSpan;
		correspondingAttributes.lineNum = 0;
		STDCL->Attributes = &correspondingAttributes;

		// TODO: Make this so that the Lines get sent as soon as they are
		// filled.

		STDCL->FillTraces(-1, true);

		Stream->WriteInt(HERE);

		for (int i = 0; i < STDCL->TracesLength; i++)
		{
			ProcessTimeline* T = STDCL->Traces[i];
			Stream->WriteInt(T->Line());
			vector<TimeCPID> data = T->Data->ListCPID;
			Stream->WriteInt(data.size());
			Stream->WriteDouble(data[0].Timestamp); // Begin time
			Stream->WriteDouble(data[data.size() - 1].Timestamp); //End time

			vector<TimeCPID>::iterator it;
			cout << "Sending process timeline with " << data.size() << " entries" << endl;
			for (it = data.begin(); it != data.end(); ++it)
			{
				Stream->WriteInt(it->CPID);
			}
			Stream->Flush();
		}
		cout << "Data sent" << endl;
	}
} /* namespace TraceviewerServer */
