/*
 * LargeByteBuffer.cpp
 *
 *  Created on: Jul 10, 2012
 *      Author: pat2
 */
#include "LargeByteBuffer.h"
#include <unistd.h>

using namespace std;

namespace TraceviewerServer
{

	ULong FileSize;
	LargeByteBuffer::LargeByteBuffer(string SPath)
	{
		//string SPath = Path.string();

		int MapFlags = MAP_PRIVATE;
		int MapProt = PROT_READ;

		struct stat Finfo;
		stat(SPath.c_str(), &Finfo);

		FileSize = Finfo.st_size;

//		if (PAGE_SIZE % mm::mapped_file::alignment() != 0)
//			cerr<< "PAGE_SIZE isn't a multiple of the OS granularity!!";
//		long FileSize = fs::file_size(Path);
		int FullPages = FileSize / PAGE_SIZE;
		int PartialPageSize = FileSize % PAGE_SIZE;
		NumPages = FullPages + (PartialPageSize == 0 ? 0 : 1);

		typedef int FileDescriptor;
		FileDescriptor fd = open(SPath.c_str(), O_RDONLY);

		ULong SizeRemaining = FileSize;

		//MasterBuffer = new mm::mapped_file*[NumPages];
		MasterBuffer = new char*[NumPages];
		for (int i = 0; i < NumPages; i++)
		{
			unsigned long mapping_len = min((ULong) PAGE_SIZE, SizeRemaining); 

			//MasterBuffer[i] = new mm::mapped_file(Path, mm::mapped_file::readonly, PAGE_SIZE, PAGE_SIZE*i);
			//This is done to make the Blue Gene Q easier
			void* AllocatedRegion = mmap(0, mapping_len, MapProt,
					MapFlags, dup(fd), PAGE_SIZE * i);
			if (AllocatedRegion == MAP_FAILED)
			{
				cerr << "Mapping returned error " << strerror(errno) << endl;
				cerr << "off_t size =" << sizeof(off_t) << "mapping size=" << mapping_len << " PAGE_SIZE=" << PAGE_SIZE << " SizeRemaining=" <<SizeRemaining << " MapProt=" <<MapProt
					<< " MapFlags=" << MapFlags << " fd=" << fd << " PAGE_SIZE*I=" << PAGE_SIZE * i << endl;
				fflush(NULL);
				exit(-1);
			}

			char* temp = (char*) AllocatedRegion;
			MasterBuffer[i] = temp;
			//cout << "Allocated a page: " << AllocatedRegion << endl;
			SizeRemaining -= mapping_len;
			//cerr << "pid=" << getpid() << " i=" << i << " first test read=" << (int) *MasterBuffer[i] << endl;
			//cerr << "pid=" << getpid() << " i=" << i << " second test read=" << (int) *(MasterBuffer[i] + mapping_len-1) << endl;
		}

	}

	int LargeByteBuffer::GetInt(ULong pos)
	{
		int Page = pos / PAGE_SIZE;
		int loc = pos % PAGE_SIZE;
		char* p2D = MasterBuffer[Page] + loc;
		int val = ByteUtilities::ReadInt(p2D);
		return val;
	}
	Long LargeByteBuffer::GetLong(ULong pos)
	{
		int Page = pos / PAGE_SIZE;
		int loc = pos % PAGE_SIZE;
		char* p2D = MasterBuffer[Page] + loc;
		Long val = ByteUtilities::ReadLong(p2D);
		return val;

	}
	ULong LargeByteBuffer::Size()
	{
		return FileSize;
	}
	LargeByteBuffer::~LargeByteBuffer()
	{
		for (int i = 0; i < NumPages; i++)
		{
			munmap(MasterBuffer[i], i + 1 == NumPages ? FileSize % PAGE_SIZE : PAGE_SIZE);
			delete (MasterBuffer[i]);
		}
		delete (MasterBuffer);
	}
}

