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
		//cout<<"In socket constructor"<<endl;
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
		{
			cerr << "Could not bind socket. Error was " << errno << endl;
			throw 1111;
		}
		//listen
		listen(socketFD, 5);
		//accept
		sockaddr_in client;
		unsigned int len = sizeof(client);
		cout << "Waiting for connection" << endl;
		socketDesc = accept(socketFD, (sockaddr*) &client, &len);
		if (socketDesc < 0)
			cerr << "Error on accept" << endl;
		file = fdopen(socketDesc, "r+b"); //read, write, binary
#ifdef ManualBuffer
				buffer = new char[2048];
				setvbuf(file, buffer, _IOFBF, 2048);*/
#endif
	}

	DataSocketStream::~DataSocketStream()
	{
		cout << "In socket destructor" << endl;
#ifdef ManualBuffer
		delete(buffer)
#endif
		close(socketDesc);

	}
	/*DataSocketStream& DataSocketStream::operator=(const DataSocketStream& rhs)
	 {
	 return *this;
	 }*/

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
		//Message.insert(Message.end(), Buffer, Buffer + 4);
		fwrite(Buffer, 4, 1, file);
	}
	void DataSocketStream::WriteLong(Long toWrite)
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
		//Message.insert(Message.end(), Buffer, Buffer + 8);
		fwrite(Buffer, 8, 1, file);
	}

	void DataSocketStream::WriteRawData(char* Data, int Length)
	{
		fwrite(Data, Length, 1, file);
	}

	void DataSocketStream::Flush()
	{
		/*cout << "Sending " << Message.size() << " bytes." << endl;
		 int e = write(socketDesc, &Message[0], Message.size());
		 if (e == -1)
		 cerr<<"Error on sending"<<endl;
		 Message.clear();*/
		int e = fflush(file);
		if (e == EOF)
			cerr << "Error on sending" << endl;
	}

	int DataSocketStream::ReadInt()
	{
		char Af[4];
		int err = fread(Af, 1, 4, file);
		if (err != 4)
			throw 0x5249;
		return ByteUtilities::ReadInt(Af);

	}

	Long DataSocketStream::ReadLong()
	{
		char Af[8];
		int err = fread(Af, 1, 8, file);
		if (err != 8)
			throw 0x524C;
		return ByteUtilities::ReadLong(Af);

	}

	short DataSocketStream::ReadShort()
	{
		char Af[2];
		int err = fread(Af, 1, 2, file);
		if (err != 2)
			throw 0x5253;
		return ByteUtilities::ReadShort(Af);
	}

	string DataSocketStream::ReadString()
	{

		short Len =ReadShort();

		char* Msg = new char[Len+1];
		int err = fread(Msg, 1, Len, file);
		if (err != Len)
			throw 0x5254;

		Msg[Len] = '\0';

		//TODO: This is wrong for any path that requires special characters
		string SF(Msg);
		return SF;
	}

	double DataSocketStream::ReadDouble()
	{
		Long BytesAsLong = ReadLong();
		Long* ptrToLong = &BytesAsLong;
		double* ptrToDoubleForm = (double*) ptrToLong;
		return *ptrToDoubleForm;
	}
	void DataSocketStream::WriteDouble(double val)
	{
		double* ptrToD = &val;
		Long* ptrToLongForm = (Long*) ptrToD;
		WriteLong(*ptrToLongForm);
	}

	void DataSocketStream::CheckForErrors(int e)
	{
		if (e == 0)
		{
			cout << "Connection closed" << endl; // EOF
			throw -7;
		}
		else if (e == -1)
			throw e; // Some other error.
	}

	SocketFD DataSocketStream::GetDescriptor()
	{
		return socketDesc;
	}
} /* namespace TraceviewerServer */
