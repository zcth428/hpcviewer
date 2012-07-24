/*
 * DataSocketStream.h
 *
 *  Created on: Jul 11, 2012
 *      Author: pat2
 */

#ifndef DATASOCKETSTREAM_H_
#define DATASOCKETSTREAM_H_

#include <vector>
#include <iostream>
#include "ByteUtilities.h"
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <errno.h>

namespace TraceviewerServer
{
	using namespace std;

	typedef int SocketFD;
	class DataSocketStream
	{
	public:
		DataSocketStream(int);

		virtual ~DataSocketStream();

		void WriteInt(int);
		void WriteLong(Long);
		void WriteDouble(double);
		void WriteRawData(char*, int);
		void Flush();

		int ReadInt();
		Long ReadLong();
		string ReadString();
		double ReadDouble();
		short ReadShort();

		SocketFD GetDescriptor();
	private:
		SocketFD socketDesc;
		std::vector<char> Message;
		void CheckForErrors(int);
		FILE* file;
#ifdef ManualBuffer
		char* Buffer;
#endif
	};

} /* namespace TraceviewerServer */
#endif /* DATASOCKETSTREAM_H_ */
