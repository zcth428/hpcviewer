/*
 * DataSocketStream.cpp
 *
 *  Created on: Jul 11, 2012
 *      Author: pat2
 */

#include "DataSocketStream.h"

namespace TraceviewerServer {
namespace as = boost::asio;
namespace ip = boost::asio::ip;
using namespace std;
DataSocketStream::DataSocketStream(as::io_service& ios)
: ip::tcp::socket(ios)
{
	socketFormPtr = this;

}

DataSocketStream::~DataSocketStream()
{

}

void DataSocketStream::WriteInt(int toWrite){
	/*char arrayform[4] = {
			(toWrite& MASK_3)>>24,
			(toWrite & MASK_2)>>16,
			(toWrite & MASK_1)>>8,
			toWrite & MASK_0
	};

	as::write(*socketFormPtr, as::buffer(arrayform), as::transfer_all(),e);*/
	Message.push_back((toWrite & MASK_3) >> 24);
	Message.push_back((toWrite & MASK_2) >> 16);
	Message.push_back((toWrite & MASK_1) >> 8);
	Message.push_back(toWrite & MASK_0);
}
void DataSocketStream::WriteLong(long toWrite){
	/*char arrayform[8] = {
			(toWrite& MASK_7)>>56,
			(toWrite & MASK_6)>>48,
			(toWrite & MASK_5)>>40,
			(toWrite& MASK_4)>>32,
			(toWrite& MASK_3)>>24,
			(toWrite & MASK_2)>>16,
			(toWrite & MASK_1)>>8,
			toWrite & MASK_0
	};

	as::write(*socketFormPtr, as::buffer(arrayform), as::transfer_all(),e);*/
	Message.push_back((toWrite & MASK_7) >> 56);
	Message.push_back((toWrite & MASK_6) >> 48);
	Message.push_back((toWrite & MASK_5) >> 40);
	Message.push_back((toWrite & MASK_4) >> 32);
	Message.push_back((toWrite & MASK_3) >> 24);
	Message.push_back((toWrite & MASK_2) >> 16);
	Message.push_back((toWrite & MASK_1) >> 8);
	Message.push_back(toWrite & MASK_0);
}

void DataSocketStream::Flush(boost::system::error_code e)
{
	as::write(*socketFormPtr, as::buffer(Message), as::transfer_all(),e);
	delete (&Message);
	std::vector<char> Message;
}

int DataSocketStream::ReadInt(boost::system::error_code e)
{
	vector<char> Af(4);
	//char Af[4];

	int len = as::read(*socketFormPtr, as::buffer(Af), e);
	cout<<"Read " << len <<"/4"<<endl;
	if (e == boost::asio::error::eof)
		cout<<"Connection closed"<<endl; // Connection closed cleanly by peer.
	else if (e)
		throw boost::system::system_error(e); // Some other error.
	return ((Af[0]<<24)| (Af[1]<<16) | (Af[2]<<8) | (Af[3]));

}

long DataSocketStream::ReadLong(boost::system::error_code e)
{
	char Af[8];
	as::read(*socketFormPtr, as::buffer(Af), e);
	return (((long)Af[0]<<56)| ((long)Af[1]<<48) | ((long)Af[2]<<40) | ((long)Af[3]<<32) |
	 (Af[4]<<24)| (Af[5]<<16) | (Af[6]<<8) | (Af[7]));
}

string DataSocketStream::ReadString(boost::system::error_code e)
{
	char len[2];
	as::read(*socketFormPtr, as::buffer(len), e);
	int Len = len[0]<<8 | len[1];
	vector<char> Msg(Len);
	as::read(*socketFormPtr, as::buffer(Msg), e);
	//TODO: This is wrong for any path that requires special characters
	string SF (Msg.begin(), Msg.end());
	return SF;
}

double DataSocketStream::ReadDouble(boost::system::error_code e)
{
	long BytesAsLong = ReadLong(e);
	long* ptrToLong = &BytesAsLong;
	double* ptrToDoubleForm = (double*) ptrToLong;
	return *ptrToDoubleForm;
}
void DataSocketStream::WriteDouble(double val)
{
	double* ptrToD = &val;
	long* ptrToLongForm = (long*) ptrToD;
	WriteLong(*ptrToLongForm);
}
} /* namespace TraceviewerServer */
