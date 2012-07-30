#include "DataSocketStream.h"
#include "zlib.h"
/*
 * CompressingDataSocketLayer.h
 *
 *  Created on: Jul 25, 2012
 *      Author: pat2
 */

#ifndef COMPRESSINGDATASOCKETLAYER_H_
#define COMPRESSINGDATASOCKETLAYER_H_

namespace TraceviewerServer
{
#define BUFFER_SIZE 0x200000 //128KB
	class CompressingDataSocketLayer : public DataSocketStream
	{
	public:
		CompressingDataSocketLayer(DataSocketStream*);
		virtual ~CompressingDataSocketLayer();
		void WriteInt(int);
		void WriteLong(Long);
		void WriteDouble(double);
		void Flush();
	private:
		void SoftFlush();
		int BufferIndex;
		DataSocketStream* BackingSocket;
		z_stream Compressor;
		char inBuf[BUFFER_SIZE];
		unsigned char outBuf[BUFFER_SIZE];
		FILE* SocketFile;

	};

} /* namespace TraceviewerServer */
#endif /* COMPRESSINGDATASOCKETLAYER_H_ */
