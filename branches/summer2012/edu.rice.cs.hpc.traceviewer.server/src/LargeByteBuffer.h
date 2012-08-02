/*
 * LargeByteBuffer.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef LARGEBYTEBUFFER_H_
#define LARGEBYTEBUFFER_H_


#include "sys/stat.h"
#include <fcntl.h>
#include <vector>
#include <string>
#include <iostream>
#include <errno.h>
#include "ByteUtilities.h"
#include "VersatileMemoryPage.h"
#include <unistd.h>

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
		vector<VersatileMemoryPage> MasterBuffer;
		ULong Length;
		int NumPages;
		//TODO: Restore back to 30, at least!
		static const long PAGE_SIZE = 1<<23;//1 << 30;
	};

} /* namespace TraceviewerServer */
#endif /* LARGEBYTEBUFFER_H_ */
