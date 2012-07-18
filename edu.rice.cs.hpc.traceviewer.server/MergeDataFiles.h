/*
 * MergeDataFiles.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef MERGEDATAFILES_H_
#define MERGEDATAFILES_H_

#include "DataOutputFileStream.h"
#include "ByteUtilities.h"
#include "Constants.h"
#include "boost/iterator.hpp"
#include "boost/algorithm/string.hpp"
#include "boost/filesystem.hpp"
#include <iostream>
#include <fstream>
#include <algorithm>
#include <vector>
#include <iterator>

using namespace boost::filesystem;
using namespace std;
namespace TraceviewerServer {

enum MergeDataAttribute {
	SUCCESS_MERGED, SUCCESS_ALREADY_CREATED, FAIL_NO_DATA, STATUS_UNKNOWN
};

class MergeDataFiles {
public:
	static MergeDataAttribute merge(path, string, path);
private:
	static const unsigned long MARKER_END_MERGED_FILE = 0xDEADF00D;
	static const int PAGE_SIZE_GUESS = 4096;
	static const int PROC_POS = 5;
	static const int THREAD_POS = 4;
	static void InsertMarker (DataOutputFileStream*);
	static bool IsMergedFileCorrect(path*);
	static bool RemoveFiles(vector<path>);
	//This was in Util.java in a modified form but is more useful here
	static bool AtLeastOneValidFile(path);
	//We need this because of the way atoi works.
	static bool StringActuallyZero(string);
};

} /* namespace TraceviewerServer */
#endif /* MERGEDATAFILES_H_ */
