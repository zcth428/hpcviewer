/*
 * DataSocketStream.cpp
 *
 *  Created on: Jul 11, 2012
 *      Author: pat2
 */

#include "DataSocketStream.h"

namespace TraceviewerServer
{
	using namespace std;

	DataSocketStream::DataSocketStream(int Port)
	{
		//create
		SocketFD socketFD = socket(PF_INET, SOCK_STREAM, 0);
		if (socketFD == -1)
			cerr << "Could not create socket" << endl;
		//bind
		sockaddr_in Address;
		Address.sin_family = AF_INET;
		Address.sin_port = htons(Port);
		Address.sin_addr.s_addr = INADDR_ANY;
		int err = bind(socketFD, (sockaddr*) &Address, sizeof(Address));
		if (err)
			cerr << "Could not bind socket" << endl;
		//listen
		listen(socketFD, 5);
		//accept
		sockaddr_in client;
		unsigned int len = sizeof(client);
		cout<<"Waiting for connection"<<endl;
		socketDesc = accept(socketFD, (sockaddr*) &client, &len);
		if (socketDesc < 0)
			cerr << "Error on accept" << endl;

	}

	DataSocketStream::~DataSocketStream()
	{
		close(socketDesc);

	}

	void DataSocketStream::WriteInt(int toWrite)
	{
		/*char arrayform[4] = {
		 (toWrite& MASK_3)>>24,
		 (toWrite & MASK_2)>>16,
		 (toWrite & MASK_1)>>8,
		 toWrite & MASK_0
		 };

		 as::write(*socketFormPtr, as::buffer(arrayform), as::transfer_all(),e);*/
		char Buffer[4];
		ByteUtilities::WriteInt(Buffer, toWrite);
		Message.insert(Message.end(), Buffer, Buffer + 4);
	}
	void DataSocketStream::WriteLong(long toWrite)
	{
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
		char Buffer[8];
		ByteUtilities::WriteLong(Buffer, toWrite);
		Message.insert(Message.end(), Buffer, Buffer + 8);
	}

	void DataSocketStream::Flush()
	{
		cout << "Sending " << Message.size() << " bytes." << endl;
		int e = write(socketDesc, &Message[0], Message.size());
		if (e == -1)
			cerr<<"Error on sending"<<endl;
		Message.clear();
	}

	int DataSocketStream::ReadInt()
	{
		char Af[4];
		int e = read(socketDesc, &Af, 4);
		CheckForErrors(e);
		return ByteUtilities::ReadInt(Af);

	}

	long DataSocketStream::ReadLong()
	{
		char Af[8];
		int e = read(socketDesc, &Af,8);
		CheckForErrors(e);
		return ByteUtilities::ReadLong(Af);

	}

	string DataSocketStream::ReadString()
	{
		char len[2];
		int e = read(socketDesc, &len, 2);
		CheckForErrors(e);
		short Len = ByteUtilities::ReadShort(len);
		char* Msg = new char[Len];
		e = read(socketDesc, Msg, Len);
		CheckForErrors(e);
		//TODO: This is wrong for any path that requires special characters
		string SF(Msg);
		return SF;
	}

	double DataSocketStream::ReadDouble()
	{
		long BytesAsLong = ReadLong();
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

	void DataSocketStream::CheckForErrors(int e)
	{
		if (e == 0)
			cout << "Connection closed" << endl; // EOF
		else if (e == -1)
			throw e; // Some other error.
	}
} /* namespace TraceviewerServer */
