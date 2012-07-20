/*
 * LargeByteBuffer.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef LARGEBYTEBUFFER_H_
#define LARGEBYTEBUFFER_H_

#include "sys/mman.h"
#include "sys/stat.h"
#include <fcntl.h>
#include <vector>
#include <string>
#include <iostream>
#include <errno.h>
#include "ByteUtilities.h"

namespace TraceviewerServer
{
	typedef uint64_t ULong;
	class LargeByteBuffer
	{
	public:
		LargeByteBuffer(std::string);
		virtual ~LargeByteBuffer();
		ULong Size();
		Long GetLong(ULong);
		int GetInt(ULong);
	private:
		char** MasterBuffer;
		ULong Length;
		int NumPages;
		static const int PAGE_SIZE = INT_MAX;
	};

} /* namespace TraceviewerServer */
#endif /* LARGEBYTEBUFFER_H_ */
