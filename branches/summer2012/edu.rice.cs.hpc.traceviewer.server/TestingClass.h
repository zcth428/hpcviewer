/*
 * TestingClass.h
 *
 *  Created on: Jul 17, 2012
 *      Author: pat2
 */

#ifndef TESTINGCLASS_H_
#define TESTINGCLASS_H_

#include <fstream>
#include <iostream>

namespace TraceviewerServer {

class TestingClass {
public:
	TestingClass();
	virtual ~TestingClass();
	int PublicVar;
	void SetAll(int);
	void TestAll();
private:
	int PrivateVar;
};

} /* namespace TraceviewerServer */
#endif /* TESTINGCLASS_H_ */
