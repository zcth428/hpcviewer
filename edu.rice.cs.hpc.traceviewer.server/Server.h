/*
 * Server.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef Server_H_
#define Server_H_



#include "SpaceTimeDataControllerLocal.h"
#include "DataSocketStream.h"
#include "LocalDBOpener.h"
#include "Constants.h"
#include "MPICommunication.h"
//#include "ImageTraceAttributes.h"
//#include "ProcessTimeline.h"
//#include "TimeCPID.h"
#include <iostream>
#include <fstream>
#include <vector>
#include <mpi.h>
#include "boost/asio.hpp"

#ifdef UseBoost
#include "boost/iostreams/filtering_streambuf.hpp"
#include "boost/iostreams/copy.hpp"
#include "boost/iostreams/filter/gzip.hpp"
#endif

namespace TraceviewerServer
{
	namespace as = boost::asio;
	namespace ip = boost::asio::ip;
	class Server
	{
	public:
		Server();
		virtual ~Server();
		static int main(int argc, char *argv[]);

	private:
		static void ParseInfo(DataSocketStream*);
		static void SendDBOpenedSuccessfully(DataSocketStream*);
		static void ParseOpenDB(DataSocketStream*);
		static void GetAndSendData(DataSocketStream*);
		static void SendXML(ip::tcp::iostream*);

	};
}/* namespace TraceviewerServer */
#endif /* Server_H_ */
