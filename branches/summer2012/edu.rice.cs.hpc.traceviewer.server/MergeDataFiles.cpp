/*
 * MergeDataFiles.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#include "MergeDataFiles.h"
#include <algorithm>
#include <vector>
#include "boost/algorithm/string.hpp"
#include <iostream>
#include <fstream>
#include "Constants.h"
using namespace std;
namespace TraceviewerServer {
	static MergeDataAttribute MergeDataFiles::merge(path Directory, string GlobInputFile, path OutputFile)
	{
		const int Last_dot = GlobInputFile.find_last_of('.');
		const string Suffix = GlobInputFile.substr(Last_dot);

		FILE* fout = fopen(OutputFile.string().c_str(), "r");
		// check if the file already exists
		if (fout != NULL)
		{
			if (IsMergedFileCorrect(&OutputFile))
				return SUCCESS_ALREADY_CREATED;
			// the file exists but corrupted. In this case, we have to remove and create a new one
			fout->_close();
			remove(OutputFile.string().c_str());
		}

		// check if the files in glob patterns is correct
		path Glob(GlobInputFile);

		if ((Glob.begin() == NULL) || !AtLeastOneValidFile(Glob))
		{
			return FAIL_NO_DATA;
		}

		DataOutputStream dos(OutputFile.string().c_str());

	//-----------------------------------------------------
	// 1. write the header:
	//  int type (0: unknown, 1: mpi, 2: openmp, 3: hybrid, ...
	//	int num_files
	//-----------------------------------------------------

		int type = 0;
		dos.WriteInt(type);
		path::iterator it;
		vector<path> AllPaths;
		// on linux, we have to sort the files
		//To sort them, we need a random access iterator, which means we need to load all of them into a vector
		for (it = Glob.begin(); it < Glob.end(); it++) {
			AllPaths.push_back(*it);
		}
		sort(AllPaths.begin(), AllPaths.end());

		dos.WriteInt(AllPaths.size());
		const long num_metric_header = 2 * Constants::SIZEOF_INT; // type of app (4 bytes) + num procs (4 bytes)
		const long num_metric_index  = AllPaths.size() * (Constants::SIZEOF_LONG + 2 * Constants::SIZEOF_INT );
		long offset = num_metric_header + num_metric_index;

		int name_format = 0; // FIXME hack:some hpcprof revisions have different format name !!
		//-----------------------------------------------------
		// 2. Record the process ID, thread ID and the offset
		//   It will also detect if the application is mp, mt, or hybrid
		//	 no accelator is supported
		//  for all files:
		//		int proc-id, int thread-id, long offset
		//-----------------------------------------------------
		vector<path>::iterator it2;
		for (it2 = AllPaths.begin(); it2 < AllPaths.end(); it2++)
		{
			path i = *it2;
			const string Filename = i.m_pathname;
			const int last_pos_basic_name = Filename.length()-Suffix.length();
			const string Basic_name = Filename.substr(0, last_pos_basic_name);
			vector<string> tokens;

			boost::split(tokens, Basic_name, boost::is_any_of("-"));
			const int num_tokens = tokens.size();
			if (num_tokens < PROC_POS)
				// if it is wrong file with the right extension, we skip
				continue;
			int proc;
			string Token_To_Parse = tokens[name_format + num_tokens - PROC_POS];
			proc = atoi(Token_To_Parse.c_str());
			if (proc == NULL && !StringActuallyZero(Token_To_Parse))//catch (NumberFormatException e)
			{
				// old version of name format
				name_format = 1;
				string Token_To_Parse = tokens[name_format + num_tokens - PROC_POS];
				proc = atoi(Token_To_Parse.c_str());
			}
			dos.WriteInt(proc);
			if (proc != 0)
				type |= Constants::MULTI_PROCESSES;
			const int Thread = atoi(tokens[name_format + num_tokens - THREAD_POS].c_str());
			dos.WriteInt(Thread);
			if (Thread != 0)
				type |= Constants::MULTI_THREADING;
			dos.WriteLong(offset);
			offset+= file_size(i);
		}
		//-----------------------------------------------------
		// 3. Copy all data from the multiple files into one file
		//-----------------------------------------------------
		for (it2 = AllPaths.begin(); it2 < AllPaths.end(); it2++) {
			path i = *it2;

			ifstream dis(i.m_pathname, ios_base::binary | ios_base::in);
			char data[PAGE_SIZE_GUESS];
			dis.read(data, PAGE_SIZE_GUESS);
			int NumRead = dis.gcount();
			while (NumRead > 0)
			{
				dos.write(data, NumRead);
				dis.read(data, PAGE_SIZE_GUESS);
				NumRead = dis.gcount();
			}
			dis.close();
		}
		InsertMarker(&dos);
		dos.close();
		//-----------------------------------------------------
		// 4. FIXME: write the type of the application
		//  	the type of the application is computed in step 2
		//		Ideally, this step has to be in the beginning !
		//-----------------------------------------------------
		DataOutputStream f(OutputFile.string().c_str());
		f.WriteInt(type);
		f.close();

		//-----------------------------------------------------
		// 5. remove old files
		//-----------------------------------------------------
		RemoveFiles(AllPaths);
		return SUCCESS_MERGED;
	}


		static bool MergeDataFiles::StringActuallyZero (string ToTest)
		{
			for (int var = 0; var < ToTest.length(); var++) {
				if (ToTest[var] != '0')
					return false;
			}
			return true;
		}

		static void MergeDataFiles::InsertMarker(DataOutputStream* dos)
		{
			dos->WriteLong(MARKER_END_MERGED_FILE);
		}
		static bool MergeDataFiles::IsMergedFileCorrect(path* filename)
		{
			ifstream f(filename, ios_base::binary|ios_base::in);
			bool IsCorrect = false;
			const long pos = boost::filesystem::file_size(*filename)-Constants::SIZEOF_LONG;
			if (pos>0){
				f.seekg(pos, ios_base::beg);
				char buffer[8];
				f.read(buffer, 8);
				const long Marker = (buffer[0]<<56)| (buffer[1]<<48)| (buffer[2]<<40)| (buffer[3]<<32)|
						(buffer[4]<<24)| (buffer[5]<<16)| (buffer[6]<<8)| (buffer[7]<<0);
				IsCorrect = (Marker == MARKER_END_MERGED_FILE);
			}
			f.close();
			return IsCorrect;
		}
		static bool MergeDataFiles::RemoveFiles(vector<path> vect)
		{
			bool success = true;
			vector<path>::iterator it;
			for (it = vect.begin(); it != vect.end(); ++it)
			{
				success &= boost::filesystem::remove(*it);
			}
			return success;
		}
} /* namespace TraceviewerServer */
