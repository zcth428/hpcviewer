/*
 * Slave.h
 *
 *  Created on: Jul 19, 2012
 *      Author: pat2
 */

#ifndef SLAVE_H_
#define SLAVE_H_

#ifndef NO_MPI//I don't want to include Server.h, which is where USE_MPI is declared

#include "SpaceTimeDataController.h"
#include "MPICommunication.h"
namespace TraceviewerServer
{

	class Slave
	{
	public:

		Slave();
		virtual ~Slave();
		void RunLoop();

	private:
		SpaceTimeDataController* STDCL;
		int GetData(MPICommunication::CommandMessage*);
		bool STDCLNeedsDeleting;
	};

} /* namespace TraceviewerServer */

#endif//NO_MPI
#endif /* SLAVE_H_ */
