/*
 * Slave.h
 *
 *  Created on: Jul 19, 2012
 *      Author: pat2
 */

#ifndef SLAVE_H_
#define SLAVE_H_

#include <mpi.h>
#include <iostream>
#include <vector>
#include "TimeCPID.h"
#include "Constants.h"
#include "SpaceTimeDataControllerLocal.h"
#include "LocalDBOpener.h"
#include "ImageTraceAttributes.h"
#include "MPICommunication.h"
#include "CompressingDataSocketLayer.h"
#include "Server.h"

namespace TraceviewerServer
{

	class Slave
	{
	public:

		Slave();
		virtual ~Slave();
		void RunLoop();

	private:
		SpaceTimeDataControllerLocal* STDCL;
		int GetData(MPICommunication::CommandMessage*);
	};

} /* namespace TraceviewerServer */
#endif /* SLAVE_H_ */
