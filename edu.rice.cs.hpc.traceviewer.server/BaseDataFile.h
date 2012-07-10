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
#include "Constants.h"
#include "boost/filesystem/path.hpp"
#include <sstream>
namespace TraceviewerServer {

class BaseDataFile {
public:
	BaseDataFile (string);
	virtual ~BaseDataFile();
	int getNumberOfFiles();
	long* getOffsets();
	LargeByteBuffer* getMasterBuffer();
	void setData(string);

	bool IsMultiProcess();
	bool IsMultiThreading();
	bool IsHybrid();

private:
	int Type;// = Constants::MULTI_PROCESSES | Constants::MULTI_THREADING;
	LargeByteBuffer* MasterBuff;
	int NumFiles;// = 0;
	string* ValuesX;
	long* Offsets;
};

} /* namespace TraceviewerServer */
#endif /* BASEDATAFILE_H_ */
