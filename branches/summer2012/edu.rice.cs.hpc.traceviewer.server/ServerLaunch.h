/*
 * ServerLaunch.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef SERVERLAUNCH_H_
#define SERVERLAUNCH_H_
#include "SpaceTimeDataControllerLocal.h"
#include "DataSocketStream.h"
#include "LocalDBOpener.h"
//#include "ImageTraceAttributes.h"
//#include "ProcessTimeline.h"
//#include "TimeCPID.h"
#include <iostream>
#include <fstream>
#include <vector>
#include "boost/asio.hpp"
#include "boost/filesystem.hpp"
#include "boost/iostreams/filtering_streambuf.hpp"
#include "boost/iostreams/copy.hpp"
#include "boost/iostreams/filter/gzip.hpp"

namespace TraceviewerServer {
namespace as = boost::asio;
namespace ip = boost::asio::ip;
class ServerLaunch {
public:
	ServerLaunch();
	virtual ~ServerLaunch();
	static int main(int argc, char *argv[]);

private:
	static void ParseInfo (DataSocketStream*);
	static void SendDBOpenedSuccessfully(DataSocketStream*);
	static void ParseOpenDB(DataSocketStream*);
	static void GetAndSendData(DataSocketStream*);
	static void SendXML(ip::tcp::iostream*);

	static const int DATA = 0x44415441;
	static const int OPEN = 0x4F50454E;
	static const int HERE = 0x48455245;
	static const int DONE = 0x444F4E45;
	static const int DBOK = 0x44424F4B;
	static const int INFO = 0x494E464F;

};
}/* namespace TraceviewerServer */
#endif /* SERVERLAUNCH_H_ */
