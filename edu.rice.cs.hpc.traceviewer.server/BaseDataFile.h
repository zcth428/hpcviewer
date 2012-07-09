/*
 * BaseDataFile.h
 *
 *  Created on: Jul 8, 2012
 *      Author: pat2
 */

#ifndef BASEDATAFILE_H_
#define BASEDATAFILE_H_
using namespace std;
#include <string>;
#include "LargeByteBuffer.h"
namespace TraceviewerServer {

class BaseDataFile {
public:
	BaseDataFile (string);
	virtual ~BaseDataFile();
	int getNumberOfFiles();
	long* getOffsets();
	LargeByteBuffer* getMasterBuffer();
	void setData(string);
};

} /* namespace TraceviewerServer */
#endif /* BASEDATAFILE_H_ */
