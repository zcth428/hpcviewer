/*
 * TraceDataByRankLocal.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef TRACEDATABYRANKLOCAL_H_
#define TRACEDATABYRANKLOCAL_H_

#include <vector>
#include "TimeCPID.h"
#include "BaseDataFile.h"
#include <cmath>
#include "Constants.h"

namespace TraceviewerServer
{

	class TraceDataByRankLocal
	{
	public:
		//TraceDataByRankLocal();
		TraceDataByRankLocal(BaseDataFile*, int, int, int);
		virtual ~TraceDataByRankLocal();

		void GetData(double, double, double);
		int SampleTimeLine(long, long, int, int, int, double, double);
		long FindTimeInInterval(double, long, long);

		/**The size of one trace record in bytes (cpid (= 4 bytes) + timeStamp (= 8 bytes)).*/
		static const int SIZE_OF_TRACE_RECORD = 12;
		vector<TimeCPID> ListCPID;
	private:
		BaseDataFile* Data;
		int Rank;
		long Minloc;
		long Maxloc;
		int NumPixelsH;

		long GetAbsoluteLocation(long);
		long GetRelativeLocation(long);
		void AddSample(int, TimeCPID);
		TimeCPID GetData(long);
		long GetNumberOfRecords(long, long);
		void PostProcess();
	};

} /* namespace TraceviewerServer */
#endif /* TRACEDATABYRANKLOCAL_H_ */
