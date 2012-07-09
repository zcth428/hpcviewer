/*
 * LocalDBOpener.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "LocalDBOpener.h"
#include "boost/filesystem/path.hpp"
#include "boost/filesystem/operations.hpp"
#include "Constants.h"
#include <iostream>
#include "MergeDataFiles.h"

namespace bfs = boost::filesystem;
namespace TraceviewerServer {

LocalDBOpener::LocalDBOpener() {
	MIN_TRACE_SIZE = 32 + 8 + 24
			+ TraceDataByRankLocal.SIZE_OF_TRACE_RECORD * 2;
}

LocalDBOpener::~LocalDBOpener() {
	// TODO Auto-generated destructor stub
}
SpaceTimeDataControllerLocal* LocalDBOpener::OpenDbAndCreateSTDC(string PathToDB) {
	FileData location;
	FileData* ptrLocation = &location;
	bool HasDatabase = false;
	HasDatabase = IsCorrectDatabse(PathToDB, ptrLocation);

	// If it still doesn't have a database, we assume that the user doesn't
	// want to open a database, so we return null, which makes the calling method return false.
	if (!HasDatabase)
		return NULL;
	SpaceTimeDataControllerLocal stdcl = new SpaceTimeDataControllerLocal(
			ptrLocation);
	return &stdcl;
}
/****
 * Check if the directory is correct or not. If it is correct, it returns
 * the XML file and the trace file
 *
 * @param directory
 *            (in): the input directory
 * @param statusMgr
 *            (in): status bar
 * @param experimentFile
 *            (out): XML file
 * @param traceFile
 *            (out): trace file
 * @return true if the directory is valid, false otherwise
 *
 */
bool IsCorrectDatabse(string directory, FileData* location) {
	bfs::path DirFile(directory);
	if (bfs::exists(DirFile) && bfs::is_directory(DirFile)) {
		location->fileXML = (new path(directory) /=
				Constants::DATABASE_FILENAME());
		FILE* XMLfile = fopen(location->fileXML->string().c_str(), "r");
		//Equivalent of canRead, I believe.
		if (XMLfile != NULL) {
			try {
				bfs::path OutputFile = DirFile / "experiment.mt";
				MergeDataAttribute att = MergeDataFiles.merge(DirFile,
						"*.hpctrace", OutputFile);
				if (att != FAIL_NO_DATA) {
					location->fileTrace = OutputFile;
					if (bfs::file_size(*(location->fileTrace))
							> MIN_TRACE_SIZE) {
						return true;
					} else {
						cerr << "Warning! Trace file "
								<< location->fileTrace->m_pathname
								<< "is too small: "
								<< bfs::file_size(*(location->fileTrace))
								<< " bytes." << endl;
						return false;
					}
				} else {
					cerr
							<< "Error: trace file(s) does not exist or fail to open "
							<< OutputFile.m_pathname << endl;
				}

			} catch (int err) {
				cerr << "Error code: " << err << endl;
			}

			}

		}
		return false;
	}
} /* namespace TraceviewerServer */
