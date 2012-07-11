/*
 * DataOutputStream.h
 *
 *	The equivalent of the Java type DataOutputStream
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef DATAOUTPUTFILESTREAM_H_
#define DATAOUTPUTFILESTREAM_H_
#include <fstream>
namespace TraceviewerServer {
using namespace std;
class DataOutputFileStream : public ofstream{
public:
	DataOutputFileStream(const char*);
	virtual ~DataOutputFileStream();
	void WriteInt(int);
	void WriteLong(long);
private:
	static const unsigned int MASK_0 = 0x000000FF, MASK_1=0x0000FF00, MASK_2=0x00FF0000, MASK_3 = 0xFF000000;//For an int
	static const unsigned long MASK_4 = 0x000000FF00000000, MASK_5 = 0x0000FF0000000000, MASK_6 = 0x00FF000000000000, MASK_7 = 0xFF00000000000000;//for a long
};

} /* namespace TraceviewerServer */
#endif /* DATAOUTPUTFILESTREAM_H_ */
