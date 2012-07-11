/*
 * DataSocketStream.h
 *
 *  Created on: Jul 11, 2012
 *      Author: pat2
 */

#ifndef DATASOCKETSTREAM_H_
#define DATASOCKETSTREAM_H_
#include "boost/asio.hpp"
#include <vector>
namespace TraceviewerServer {
using namespace std;
namespace as = boost::asio;
namespace ip = boost::asio::ip;
class DataSocketStream : public ip::tcp::socket{
public:
	DataSocketStream(as::io_service);
	virtual ~DataSocketStream();
	void WriteInt(int);
	void WriteLong(long);
	int ReadInt(boost::system::error_code);
	long ReadLong(boost::system::error_code);
	void Flush (boost::system::error_code);
	string ReadString(boost::system::error_code);
	double ReadDouble(boost::system::error_code);
	void WriteDouble(double);
private:
	ip::tcp::socket* socketFormPtr;
	std::vector<char> Message;
	static const unsigned int MASK_0 = 0x000000FF, MASK_1=0x0000FF00, MASK_2=0x00FF0000, MASK_3 = 0xFF000000;//For an int
	static const unsigned long MASK_4 = 0x000000FF00000000, MASK_5 = 0x0000FF0000000000, MASK_6 = 0x00FF000000000000, MASK_7 = 0xFF00000000000000;//for a long
};

} /* namespace TraceviewerServer */
#endif /* DATASOCKETSTREAM_H_ */
