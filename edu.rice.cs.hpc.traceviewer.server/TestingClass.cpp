/*
 * TestingClass.cpp
 *
 *  Created on: Jul 17, 2012
 *      Author: pat2
 */

#include "TestingClass.h"

namespace TraceviewerServer {
int CppVar;
TestingClass::TestingClass() {
	// TODO Auto-generated constructor stub

}

TestingClass::~TestingClass() {
	// TODO Auto-generated destructor stub
}
void TestingClass::SetAll(int v)
{
	PrivateVar = v;
	PublicVar = v;
	CppVar = v;
}
void TestingClass::TestAll()
{
	std::cout << "Private: " << PrivateVar << " Public: " << PublicVar << " Cpp: "<< CppVar <<std::endl;
}

} /* namespace TraceviewerServer */
