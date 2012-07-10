/*
 * ServerLaunch.h
 *
 *  Created on: Jul 9, 2012
 *      Author: pat2
 */

#ifndef SERVERLAUNCH_H_
#define SERVERLAUNCH_H_

namespace TraceviewerServer {

class ServerLaunch {
public:
	ServerLaunch();
	virtual ~ServerLaunch();
	int main(int argc, char *argv[]);

};
}/* namespace TraceviewerServer */
#endif /* SERVERLAUNCH_H_ */
