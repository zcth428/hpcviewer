/*
 * LargeByteBuffer.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef LARGEBYTEBUFFER_H_
#define LARGEBYTEBUFFER_H_

#include "boost/iostreams/device/mapped_file.hpp"
#include "boost/filesystem.hpp"
#include <vector>

namespace TraceviewerServer {

class LargeByteBuffer {
public:
	LargeByteBuffer(boost::filesystem::path);
	virtual ~LargeByteBuffer();
	long Size();
	long GetLong(long);
	int GetInt(long);
private:
	boost::iostreams::mapped_file** MasterBuffer;
	long Length;
	int NumPages;
	static const int PAGE_SIZE = INT_MAX;
};

} /* namespace TraceviewerServer */
#endif /* LARGEBYTEBUFFER_H_ */
