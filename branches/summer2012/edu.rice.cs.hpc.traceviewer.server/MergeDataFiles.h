/*
 * MergeDataFiles.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef MERGEDATAFILES_H_
#define MERGEDATAFILES_H_
#include "MergeDataAttribute.h"
#include "boost/filesystem/path.hpp"
#include "DataOutputStream.h"
using namespace boost::filesystem;
using namespace std;
namespace TraceviewerServer {

class MergeDataFiles {
public:
	static MergeDataAttribute merge(path, string, path);
private:
	static const long MARKER_END_MERGED_FILE = 0xDEADF00D;
	static const int PAGE_SIZE_GUESS = 4096;
	static const int PROC_POS = 5;
	static const int THREAD_POS = 4;
	static void InsertMarker (DataOutputStream*);
	static bool IsMergedFileCorrect(path*);
	static bool RemoveFiles(vector<path>);
	//This was in Util.java in a modified form but is more useful here
	static bool AtLeastOneValidFile(path);
	//We need this because of the way atoi works.
	static bool StringActuallyZero(string);
};

} /* namespace TraceviewerServer */
#endif /* MERGEDATAFILES_H_ */
