/*
 * Communication.h
 *
 *  Created on: Jul 19, 2012
 *      Author: pat2
 */

#ifndef MPICOMMUNICATION_H_
#define MPICOMMUNICATION_H_

namespace TraceviewerServer
{

	class MPICommunication
	{
	public:
		static const int SOCKET_SERVER = 0; //The rank of the node that is doing all the socket comm
		static const int MAX_TRACE_LENGTH = 50000;
		typedef struct
		{
			char Path[1024];
		} open_file_command;
		typedef struct
		{
			int processStart;
			int processEnd;
			double timeStart;
			double timeEnd;
			int verticalResolution;
			int horizontalResolution;
		} get_data_command;
		typedef struct
		{
			long minBegTime;
			long maxEndTime;
			int headerSize;
		} more_info_command;

		typedef struct
		{
			int Command;
			union
			{

				open_file_command ofile;
				get_data_command gdata;
				more_info_command minfo;
			};
		} CommandMessage;

		typedef struct
		{
			int Line;
			int Size;
			double Begtime;
			double Endtime;
			union
			{
			int Data[MAX_TRACE_LENGTH];
			char RawBytes[4*MAX_TRACE_LENGTH];
			};
		} DataMessage;

		typedef struct
		{
			int RankID;
			int TraceLinesSent;
		} DoneMessage;

		typedef struct
		{
			int Tag;
			union
			{
				DataMessage Data;
				DoneMessage Done;
			};
		} ResultMessage;
	};

} /* namespace TraceviewerServer */
#endif /* MPICOMMUNICATION_H_ */
