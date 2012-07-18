/*
 * Constants.h
 *
 *  Created on: Jul 7, 2012
 *      Author: pat2
 */

#ifndef CONSTANTS_H_
#define CONSTANTS_H_
namespace TraceviewerServer
{
	class Constants
	{
	public:
		static const int MULTI_PROCESSES = 1;
		static const int MULTI_THREADING = 2;

		static const int SIZEOF_LONG = 8;
		static const int SIZEOF_INT = 4;

		static const char* XML_FILENAME()
		{
			return "experiment.xml";
		}
		static const char* TRACE_FILENAME()
		{
			return "experiment.mt";
		}
	};
}
#endif /* CONSTANTS_H_ */
