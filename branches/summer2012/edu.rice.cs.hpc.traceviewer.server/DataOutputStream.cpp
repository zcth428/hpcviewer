/*
 * DataOutputStream.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "DataOutputStream.h"
#include <iostream>
using namespace std;
namespace TraceviewerServer {

ostream* Backer;
const unsigned int MASK_0 = 0x000000FF, MASK_1=0x0000FF00, MASK_2=0x00FF0000, MASK_3 = 0xFF000000;//For an int
const unsigned long MASK_4 = 0x000000FF00000000, MASK_5 = 0x0000FF0000000000, MASK_6 = 0x00FF000000000000, MASK_7 = 0xFF00000000000000;//for a long
DataOutputStream::DataOutputStream(const char* filename)
:ofstream (filename, ios_base::binary|ios_base::out)
{

}

DataOutputStream::~DataOutputStream() {
	delete(*Backer);
	Backer = NULL;
}
void DataOutputStream::WriteInt(int toWrite){
	char arrayform[4] = {
			(toWrite& MASK_3)>>24,
			(toWrite & MASK_2)>>16,
			(toWrite & MASK_1)>>8,
			toWrite & MASK_0
	};
	write(arrayform, 4);
}
void DataOutputStream::WriteLong(long toWrite){
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
	write(arrayform, 4);
}

} /* namespace TraceviewerServer */
