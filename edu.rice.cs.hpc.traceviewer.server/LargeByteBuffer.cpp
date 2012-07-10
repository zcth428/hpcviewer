/*
 * LargeByteBuffer.cpp
 *
 *  Created on: Jul 10, 2012
 *      Author: pat2
 */
#include "LargeByteBuffer.h"

using namespace std;
namespace fs = boost::filesystem;
namespace mm = boost::iostreams;

namespace TraceviewerServer {

	LargeByteBuffer::LargeByteBuffer(fs::path Path)
	{
		if (PAGE_SIZE%mm::mapped_file::alignment() != 0)
			cerr<< "PAGE_SIZE isn't a multiple of the OS granularity!!";
		long FileSize = fs::file_size(Path);
		int FullPages = FileSize/PAGE_SIZE;
		int PartialPageSize = FileSize%PAGE_SIZE;
		NumPages = FullPages + (PartialPageSize==0? 0 : 1);
		MasterBuffer = new mm::mapped_file*[NumPages];
		for (int i = 0; i < FullPages; i++) {

			MasterBuffer[i] = new mm::mapped_file(Path, mm::mapped_file::readonly, PAGE_SIZE, PAGE_SIZE*i);
		}

	}

	int LargeByteBuffer::GetInt(long pos) {
		int Page = pos / PAGE_SIZE;
		int loc = pos % PAGE_SIZE;
		char* p2D = MasterBuffer[Page]->data() + loc;
		int val = (*(p2D) <<24) | (*(p2D+1) <<16) | (*(p2D+2) <<8) | *(p2D+3);
		return val;
	}
	long LargeByteBuffer::GetLong(long pos)
	{
		int Page = pos / PAGE_SIZE;
		int loc = pos % PAGE_SIZE;
		char* p2D = MasterBuffer[Page]->data() + loc;
		long val =((long)*(p2D) <<56) | ((long)*(p2D+1) <<48) | ((long)*(p2D+2) <<40) | ((long)*(p2D+3) <<32)|
				(*(p2D+4) <<24) | (*(p2D+5) <<16) | (*(p2D+6) <<8) | *(p2D+7);
		return val;

	}
	long LargeByteBuffer::Size()
	{
		return Length;
	}
	LargeByteBuffer::~LargeByteBuffer()
	{
		for (int i = 0; i < NumPages; i++) {
			delete(MasterBuffer[i]);
		}
		delete(MasterBuffer);
	}
}

