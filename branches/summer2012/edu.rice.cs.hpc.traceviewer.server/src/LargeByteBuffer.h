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
#include <sys/types.h>
#include <sys/sysctl.h>


namespace TraceviewerServer
{
	typedef uint64_t ULong;
	class LargeByteBuffer
	{
	public:
		LargeByteBuffer(std::string, int);
		virtual ~LargeByteBuffer();
		ULong Size();
		Long GetLong(ULong);
		int GetInt(ULong);
	private:
		static ULong lcm(ULong, ULong);
		static ULong GetRamSize();
		vector<VersatileMemoryPage> MasterBuffer;
		ULong Length;
		int NumPages;

	};

} /* namespace TraceviewerServer */
#endif /* LARGEBYTEBUFFER_H_ */
