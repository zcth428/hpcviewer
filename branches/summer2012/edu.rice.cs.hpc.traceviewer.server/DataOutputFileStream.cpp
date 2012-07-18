/*
 * DataOutputFileStream.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "DataOutputFileStream.h"

using namespace std;
namespace TraceviewerServer
{

//ostream* Backer;
	DataOutputFileStream::DataOutputFileStream(const char* filename) :
			ofstream(filename, ios_base::binary | ios_base::out)
	{

	}

	DataOutputFileStream::~DataOutputFileStream()
	{

	}
	void DataOutputFileStream::WriteInt(int toWrite)
	{

		char arrayform[4];
		ByteUtilities::WriteInt(arrayform, toWrite);
		write(arrayform, 4);
	}
	void DataOutputFileStream::WriteLong(long toWrite)
	{
		char arrayform[8];
		ByteUtilities::WriteLong(arrayform, toWrite);
		write(arrayform, 8);
	}

} /* namespace TraceviewerServer */
