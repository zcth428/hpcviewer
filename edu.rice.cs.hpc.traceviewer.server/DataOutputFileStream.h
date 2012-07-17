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
#include "ByteUtilities.h"
namespace TraceviewerServer {
using namespace std;
class DataOutputFileStream : public ofstream{
public:
	DataOutputFileStream(const char*);
	virtual ~DataOutputFileStream();
	void WriteInt(int);
	void WriteLong(long);
private:
};

} /* namespace TraceviewerServer */
#endif /* DATAOUTPUTFILESTREAM_H_ */
