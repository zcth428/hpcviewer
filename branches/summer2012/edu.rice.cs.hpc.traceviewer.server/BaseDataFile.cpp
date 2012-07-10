/*
 * BaseDataFile.cpp
 *
 *  Created on: Jul 8, 2012
 *      Author: pat2
 */

#include "BaseDataFile.h"

using namespace std;
namespace TraceviewerServer {
//-----------------------------------------------------------
// Global variables
//-----------------------------------------------------------

BaseDataFile::BaseDataFile(string filename) {
	if (filename != "") {
		//---------------------------------------------
		// test file version
		//---------------------------------------------

		setData(filename);
	}

}

int BaseDataFile::getNumberOfFiles()
{
	return NumFiles;
}

long* BaseDataFile::getOffsets()
{
return Offsets;
}

LargeByteBuffer* BaseDataFile::getMasterBuffer()
{
	return MasterBuff;
}

/***
 * assign data
 * @param f: array of files
 * @throws IOException
 */
void BaseDataFile::setData(string filename)
{
	boost::filesystem::path Path(filename);
	MasterBuff = new LargeByteBuffer(Path);

	Type = MasterBuff->GetInt(0);
	NumFiles = MasterBuff->GetInt(Constants::SIZEOF_INT);

	ValuesX = new string[NumFiles];
	Offsets = new long[NumFiles];

	long current_pos = Constants::SIZEOF_INT*2;

	// get the procs and threads IDs
	for(int i=0; i<NumFiles; i++) {
		const int proc_id = MasterBuff->GetInt(current_pos);
		current_pos += Constants::SIZEOF_INT;
		const int thread_id = MasterBuff->GetInt(current_pos);
		current_pos += Constants::SIZEOF_INT;

		Offsets[i] = MasterBuff->GetLong(current_pos);
		current_pos += Constants::SIZEOF_LONG;

		//--------------------------------------------------------------------
		// adding list of x-axis
		//--------------------------------------------------------------------

		ostringstream stringBuilder;
		if (IsHybrid())
		{
			stringBuilder << proc_id << "." <<thread_id;
		}else if (IsMultiProcess())
		{
			stringBuilder << proc_id;
		} else
		{
			// temporary fix: if the application is neither hybrid nor multiproc nor multithreads,
			// we just print whatever the order of file name alphabetically
			// this is not the ideal solution, but we cannot trust the value of proc_id and thread_id
			stringBuilder<<i;
		}
		ValuesX[i] = stringBuilder.str();
	}
}

//Check if the application is a multi-processing program (like MPI)
bool BaseDataFile::IsMultiProcess()
{
	return (Type & Constants::MULTI_PROCESSES) != 0;
}
//Check if the application is a multi-threading program (OpenMP for instance)
bool BaseDataFile::IsMultiThreading()
{
	return (Type & Constants::MULTI_THREADING) != 0;
}
//Check if the application is a hybrid program (MPI+OpenMP)
bool BaseDataFile::IsHybrid()
{
	return (IsMultiProcess()&& IsMultiThreading());

}

BaseDataFile::~BaseDataFile() {
	// TODO Auto-generated destructor stub
}

} /* namespace TraceviewerServer */
