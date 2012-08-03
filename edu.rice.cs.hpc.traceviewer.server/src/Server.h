/*
 * Server.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef Server_H_
#define Server_H_

#define UseMPI

#include "SpaceTimeDataControllerLocal.h"
#include "DataSocketStream.h"
#include "LocalDBOpener.h"
#include "Constants.h"
#include "MPICommunication.h"
#include "CompressingDataSocketLayer.h"
//#include "ImageTraceAttributes.h"
//#include "ProcessTimeline.h"
//#include "TimeCPID.h"
#include <iostream>
#include <fstream>
#include <vector>
#include <mpi.h>

#include "zlib.h"

namespace TraceviewerServer
{
	extern bool Compression;
	extern int MainPort;
	extern int XMLPort;
	class Server
	{

	public:
		Server();
		virtual ~Server();
		static int main(int argc, char *argv[]);

	private:
		static int RunConnection(DataSocketStream*);
		static void ParseInfo(DataSocketStream*);
		static void SendDBOpenedSuccessfully(DataSocketStream*);
		static void ParseOpenDB(DataSocketStream*);
		static void GetAndSendData(DataSocketStream*);
		static void SendXML(int);
		static void SendDBOpenFailed(DataSocketStream*);

	};
}/* namespace TraceviewerServer */
#endif /* Server_H_ */
