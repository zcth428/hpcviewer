/*
 * BaseDataFile.cpp
 *
 *  Created on: Jul 8, 2012
 *      Author: pat2
 */

#include "BaseDataFile.h"
#include "Constants.h"
#include "SpaceTimeDataControllerLocal.h"
#include "LargeByteBuffer.h"
using namespace std;
namespace TraceviewerServer {
//-----------------------------------------------------------
// Global variables
//-----------------------------------------------------------

int type = Constants::MULTI_PROCESSES | Constants::MULTI_THREADING; // default is hybrid


int numFiles = 0;
string* valuesX;
long* offsets;
LargeByteBuffer* masterBuff;
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
	return numFiles;
}

long* BaseDataFile::getOffsets()
{
return offsets;
}

LargeByteBuffer* BaseDataFile::getMasterBuffer()
{
	return masterBuff;
}

/***
 * assign data
 * @param f: array of files
 * @throws IOException
 */
void BaseDataFile::setData(string filename)
{
	//Create masterBuff
	//TODO Write this method
}

BaseDataFile::~BaseDataFile() {
	// TODO Auto-generated destructor stub
}

} /* namespace TraceviewerServer */
