#define UseMPI

#include "mpi.h"
#include "FileUtils.h"
#include "Server.h"
#include "Slave.h"
#include "MPICommunication.h"
#include "zlib.h"
#include <fstream>
#include <vector>

using namespace std;
using namespace MPI;

void FSTest();
void GzipTest();
int main(int argc, char *argv[])
{
	//GzipTest();
	//FSTest();

#ifdef UseMPI
	MPI::Init(argc, argv);
	int rank, size;
	rank = MPI::COMM_WORLD.Get_rank();
	size = MPI::COMM_WORLD.Get_size();


	if (rank == TraceviewerServer::MPICommunication::SOCKET_SERVER)
	{
		try
		{
			TraceviewerServer::Server::main(argc, argv);
		} catch (int e)
		{

		}
		TraceviewerServer::MPICommunication::CommandMessage serverShutdown;
		serverShutdown.Command = TraceviewerServer::Constants::DONE;
		COMM_WORLD.Bcast(&serverShutdown, sizeof(serverShutdown), MPI_PACKED,
				TraceviewerServer::MPICommunication::SOCKET_SERVER);
	}
	else
	{
		try
		{
			TraceviewerServer::Slave();
		} catch (int e)
		{

		}
	}
#else
	try
	{
	TraceviewerServer::Server::main(argc, argv);
	}
	catch (int e)
	{
		cout << "Closing with error code "<<e<<endl;
	}
#endif

	/*



	 int* val = new int[10];
	 if (rank == 0) {
	 srand(time(NULL));
	 for (int var = 0; var < 10; var++) {
	 val[var] = rand() % 100;
	 }
	 }
	 MPI_Bcast(val, 10, MPI_INT, 0, COMM_WORLD);
	 cout << "Rank: " << rank << " val: " << val[0] << ".." << val[9] << "\n";
	 */
#ifdef UseMPI
	MPI::Finalize();
#endif
	return 0;
}

void WriteVector(vector<string> t)
{
	cout << "Writing vector of length "<< t.size()<<endl;
	for (int var = 0; var < t.size(); var++) {
		cout<<"\t["<<var<<"] = " << t[var]<<endl;
	}

}

void FSTest()
{
/*	string d1 = "/FakeDirectory/";
	string d2 = "/FakeDirectory";
	string f1 = "/FakeFile.ext";
	string f2 = "FakeFile.ext";
	cout << TraceviewerServer::FileUtils::CombinePaths(d1, f1) << endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d1, f2) << endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d2, f1) << endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d2, f2) << endl;*/

	/*string t1 = "This string contains no delimiters";
	string t2 = "a-b-c-d";
	string t3 = "-";
	string t4 = "--- -";
	string t5 = "aa-aa-berty--d";
	string t6 = "";
	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t1, '-'));

	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t2, '-'));
	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t3, '-'));
	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t4, '-'));
	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t5, '-'));
	WriteVector(TraceviewerServer::MergeDataFiles::SplitString(t6, '-'));
	cout<<"Done"<<endl;*/
}

void GzipTest()
{
	#define CHUNK 0x40000//256k

	FILE* in = fopen("/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.xml", "r+");
	gzFile out = gzopen("/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.gz", "w");

	int size = TraceviewerServer::FileUtils::GetFileSize("/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.xml");
	int BytesProcessed = 0;
	char Buffer[CHUNK];
	while(BytesProcessed < size)
	{
		int br = fread(Buffer, 1, CHUNK, in);
		gzwrite(out, Buffer, br);
		BytesProcessed += br;
	}
	gzflush(out, Z_FINISH);
	gzclose(out);
	fclose(in);

	/*std::ifstream XMLFile("/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.xml",
	 std::ios_base::in | std::ios_base::binary);

	 std::ofstream GZFile("/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.gz",
	 ios_base::out | ios_base::trunc | ios_base::binary);
	 if (!GZFile.good())
	 cout<<"B: "<< GZFile.bad()<<" EO: "<<GZFile.eof() << " F:" << GZFile.fail()<<endl;
	 boost::iostreams::filtering_streambuf<boost::iostreams::output> out;

	 out.push(boost::iostreams::gzip_compressor());
	 out.push(GZFile);
	 boost::iostreams::copy(XMLFile, out);
	 XMLFile.close();
	 GZFile.close();*/

}
void HelloWorld()
{
	//int rank, size;
	//rank = MPI::COMM_WORLD.Get_rank();
	//size = MPI::COMM_WORLD.Get_size();
	//cout << "Hello World! (" << rank << ", " << size << ")\n";
}

