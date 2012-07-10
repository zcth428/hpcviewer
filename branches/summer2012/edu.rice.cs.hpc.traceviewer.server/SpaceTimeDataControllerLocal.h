/*
 * SpaceTimeDataControllerLocal.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef SPACETIMEDATACONTROLLERLOCAL_H_
#define SPACETIMEDATACONTROLLERLOCAL_H_
#include "FileData.h"
#include "ImageTraceAttributes.h"
#include "BaseDataFile.h"
#include "ProcessTimeline.h"
#include "boost/filesystem/path.hpp"
namespace TraceviewerServer {

class SpaceTimeDataControllerLocal {
public:
	SpaceTimeDataControllerLocal();
	SpaceTimeDataControllerLocal(FileData*);
	virtual ~SpaceTimeDataControllerLocal();
	void SetInfo(long, long, int);
	ProcessTimeline* GetNextTrace(bool);
	void AddNextTrace(ProcessTimeline*);
	void FillTraces(int, bool);
	void PrepareViewportPainting(bool);

	//The number of processes in the database, independent of the current display size
	int Height;
	boost::filesystem::path ExperimentXML;
private:
	int LineToPaint(int);

	ImageTraceAttributes* OldAttributes;
	ImageTraceAttributes* Attributes;
	BaseDataFile* DataTrace;
	int HEADER_SIZE;

	 // The minimum beginning and maximum ending time stamp across all traces (in microseconds).
	long MaxEndTime, MinBegTime;
	ProcessTimeline** Traces;
	int TracesLength;
	ProcessTimeline* DepthTrace;

};

} /* namespace TraceviewerServer */
#endif /* SPACETIMEDATACONTROLLERLOCAL_H_ */
