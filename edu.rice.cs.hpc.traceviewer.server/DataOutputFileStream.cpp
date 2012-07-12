/*
 * DataOutputFileStream.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "DataOutputFileStream.h"

using namespace std;
namespace TraceviewerServer {

//ostream* Backer;
DataOutputFileStream::DataOutputFileStream(const char* filename)
:ofstream (filename, ios_base::binary|ios_base::out)
{

}

DataOutputFileStream::~DataOutputFileStream() {

}
void DataOutputFileStream::WriteInt(int toWrite){
	char arrayform[4] = {
			(toWrite& MASK_3)>>24,
			(toWrite & MASK_2)>>16,
			(toWrite & MASK_1)>>8,
			toWrite & MASK_0
	};
	write(arrayform, 4);
}
void DataOutputFileStream::WriteLong(long toWrite){
	char arrayform[8] = {
			(toWrite& MASK_7)>>56,
			(toWrite & MASK_6)>>48,
			(toWrite & MASK_5)>>40,
			(toWrite& MASK_4)>>32,
			(toWrite& MASK_3)>>24,
			(toWrite & MASK_2)>>16,
			(toWrite & MASK_1)>>8,
			toWrite & MASK_0
	};
	write(arrayform, 8);
}

} /* namespace TraceviewerServer */
