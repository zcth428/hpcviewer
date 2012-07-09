/*
 * FileData.cpp
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */
#ifndef FILEDATA_H_
#define FILEDATA_H_
#include <stdio.h>
#include <boost/filesystem/path.hpp>
using namespace boost::filesystem;
namespace TraceviewerServer {
struct FileData {
	path* fileXML;
	path* fileTrace;
};
}
#endif

