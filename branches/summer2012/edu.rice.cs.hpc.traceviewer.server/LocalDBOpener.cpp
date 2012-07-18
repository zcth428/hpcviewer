/*
 * LocalDBOpener.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "LocalDBOpener.h"

namespace TraceviewerServer
{

	LocalDBOpener::LocalDBOpener()
	{

	}

	LocalDBOpener::~LocalDBOpener()
	{
		// TODO Auto-generated destructor stub
	}
	SpaceTimeDataControllerLocal* stdcl;
	SpaceTimeDataControllerLocal* LocalDBOpener::OpenDbAndCreateSTDC(string PathToDB)
	{
		FileData location;
		FileData* ptrLocation = &location;
		bool HasDatabase = false;
		HasDatabase = IsCorrectDatabase(PathToDB, ptrLocation);

		// If it still doesn't have a database, we assume that the user doesn't
		// want to open a database, so we return null, which makes the calling method return false.
		if (!HasDatabase)
			return NULL;

		stdcl = new SpaceTimeDataControllerLocal(ptrLocation); //I think this should allocate it on the heap
		return stdcl;
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
	bool LocalDBOpener::IsCorrectDatabase(string directory, FileData* location)
	{

		if (FileUtils::ExistsAndIsDir(directory))
		{

			location->fileXML = FileUtils::CombinePaths(directory, Constants::XML_FILENAME());
			FILE* XMLfile = fopen(location->fileXML.c_str(), "r");
			//Equivalent of canRead, I believe.
			if (XMLfile != NULL)
			{
				try
				{
					std::string OutputFile = FileUtils::CombinePaths(directory, Constants::TRACE_FILENAME());
					MergeDataAttribute att = MergeDataFiles::merge(directory, "*.hpctrace",
							OutputFile);
					if (att != FAIL_NO_DATA)
					{
						location->fileTrace = OutputFile;
						if (FileUtils::GetFileSize(location->fileTrace) > MIN_TRACE_SIZE)
						{
							return true;
						}
						else
						{
							cerr << "Warning! Trace file " << location->fileTrace << "is too small: "
									<< FileUtils::GetFileSize(location->fileTrace) << " bytes." << endl;
							return false;
						}
					}
					else
					{
						cerr << "Error: trace file(s) does not exist or fail to open "
								<< OutputFile << endl;
					}

				} catch (int err)
				{
					cerr << "Error code: " << err << endl;
				}

			}

		}
		return false;
	}

} /* namespace TraceviewerServer */
