/*
 * ServerLaunch.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "ServerLaunch.h"
//#include "SpaceTimeDataControllerLocal.h"
namespace as = boost::asio;
namespace ip = boost::asio::ip;
using namespace std;
namespace TraceviewerServer {

static SpaceTimeDataControllerLocal* STDCL;

ServerLaunch::ServerLaunch() {
	// TODO Auto-generated constructor stub

}

ServerLaunch::~ServerLaunch() {
	// TODO Auto-generated destructor stub
}
int ServerLaunch::main(int argc, char *argv[]) {
	DataSocketStream* socketptr;
	as::io_service io_service;
	DataSocketStream CLsocket(io_service);
	try {

		ip::tcp::acceptor acceptor(io_service,
				ip::tcp::endpoint(ip::tcp::v4(), 21590));
		cout << "Waiting for connection" << endl;
		/*
		ip::tcp::socket CLsocket(io_service);
		//CLsocket.open(ip::tcp::v4());
		acceptor.accept(CLsocket);
		socketptr = (DataSocketStream*) &CLsocket;
		*/
		acceptor.accept(CLsocket);
		socketptr = &CLsocket;
	} catch (std::exception& e) {
		std::cerr << e.what() << std::endl;
		return -3;
	}
	cout << "Received connection" << endl;
	//vector<char> test(4);
	//as::read(*socketptr, as::buffer(test));

	ParseOpenDB(socketptr);

	if (STDCL == NULL) {
		cout << "Could not open database" << endl;
		//Send no DB
	} else {
		cout << "Database opened" << endl;
		SendDBOpenedSuccessfully(socketptr);
	}

	boost::system::error_code error;
	int Message = socketptr->ReadInt(error);
	if (Message == INFO)
		ParseInfo(socketptr);
	else
		cerr << "Did not receive info packet" << endl;

	bool EndingConnection = false;
	while (!EndingConnection) {
		int NextCommand = socketptr->ReadInt(error);
		switch (NextCommand) {
		case DATA:
			GetAndSendData(socketptr);
			break;
		case DONE:
			EndingConnection = true;
			break;
		default:
			cerr << "Unknown command received" << endl;
			break;
		}
	}

	return 0;
}
void ServerLaunch::ParseInfo(DataSocketStream* socket) {
	boost::system::error_code e1, e2, e3;
	STDCL->SetInfo(socket->ReadLong(e1), socket->ReadLong(e2),
			socket->ReadInt(e3));
}
void ServerLaunch::SendDBOpenedSuccessfully(DataSocketStream* socket) {
	boost::system::error_code e1, e2, e3;
	socket->WriteInt(DBOK);

	as::io_service XMLio_service;
	ip::tcp::acceptor XMLacceptor(XMLio_service,
			ip::tcp::endpoint(ip::tcp::v4(), 0));
	int port = XMLacceptor.local_endpoint().port();

	socket->WriteInt(port);
	socket->WriteInt(STDCL->Height);
	socket->Flush(e1);

	cout << "Waiting to send XML on port " << port << endl;

	ip::tcp::iostream XMLstr;
	XMLacceptor.accept(*XMLstr.rdbuf());
	SendXML(&XMLstr);

	cout << "XML Sent";
}

void ServerLaunch::SendXML(ip::tcp::iostream* XMLSocket) {
	std::ifstream XMLFile(STDCL->ExperimentXML.string().c_str(),
			std::ios_base::in | std::ios_base::binary);

	boost::iostreams::filtering_streambuf<boost::iostreams::output> out;
	out.push(*XMLSocket);
	out.push(boost::iostreams::gzip_compressor());
	boost::iostreams::copy(XMLFile, out);
}

void ServerLaunch::ParseOpenDB(DataSocketStream* receiver) {
	boost::system::error_code e1, e2;
	if (!receiver->is_open())
		cout<<"Socket not open!"<<endl;
	int Command = receiver->ReadInt(e1);
	if (Command != OPEN)
		cerr << "Expected an open command, got " << Command << endl;
	string PathToDB = receiver->ReadString(e2);
	LocalDBOpener DBO;
	STDCL = DBO.OpenDbAndCreateSTDC(PathToDB);
}

void ServerLaunch::GetAndSendData(DataSocketStream* Stream) {
	boost::system::error_code e1, e2;
	int processStart = Stream->ReadInt(e1);
	int processEnd = Stream->ReadInt(e1);
	double timeStart = Stream->ReadDouble(e1);
	double timeEnd = Stream->ReadDouble(e1);
	int verticalResolution = Stream->ReadInt(e1);
	int horizontalResolution = Stream->ReadInt(e1);
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
	STDCL->Attributes = &correspondingAttributes;

	// TODO: Make this so that the Lines get sent as soon as they are
	// filled.

	STDCL->FillTraces(-1, true);
	for (int i = 0; i < STDCL->TracesLength; i++) {
		ProcessTimeline* T = STDCL->Traces[i];
		Stream->WriteInt(T->Line());
		vector<TimeCPID> data = T->Data->ListCPID;
		Stream->WriteInt(data.size());
		Stream->WriteDouble(data[0].Timestamp); // Begin time
		Stream->WriteDouble(data[data.size() - 1].Timestamp); //End time

		vector<TimeCPID>::iterator it;
		for (it = data.begin(); it != data.end(); ++it) {
			Stream->WriteInt(it->CPID);
		}
		Stream->Flush(e2);
	}
	cout << "Data sent" << endl;
}
} /* namespace TraceviewerServer */
