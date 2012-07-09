/*
 * DataOutputStream.h
 *
 *	The equivalent of the Java type DataOutputStream
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef DATAOUTPUTSTREAM_H_
#define DATAOUTPUTSTREAM_H_

namespace TraceviewerServer {
using namespace std;
class DataOutputStream : ofstream{
public:
	DataOutputStream(const char*);
	virtual ~DataOutputStream();
	void WriteInt(int);
	void WriteLong(long);
};

} /* namespace TraceviewerServer */
#endif /* DATAOUTPUTSTREAM_H_ */
