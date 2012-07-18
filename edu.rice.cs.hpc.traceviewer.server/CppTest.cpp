//#include "mpi.h"
#include "FileUtils.h"
#include "Server.h"
#include <fstream>
#include <vector>

using namespace std;
//using namespace MPI;

void FSTest();
int main(int argc, char *argv[])
{

	//FSTest();

	TraceviewerServer::Server::main(argc, argv);
	//ofstream g;

	/*MPI::Init(argc, argv);

	 int rank, size;
	 rank = MPI::COMM_WORLD.Get_rank();
	 size = MPI::COMM_WORLD.Get_size();

	 int* val = new int[10];
	 if (rank == 0) {
	 srand(time(NULL));
	 for (int var = 0; var < 10; var++) {
	 val[var] = rand() % 100;
	 }
	 }
	 MPI_Bcast(val, 10, MPI_INT, 0, COMM_WORLD);
	 cout << "Rank: " << rank << " val: " << val[0] << ".." << val[9] << "\n";

	 MPI::Finalize();*/
	return 0;
}

void FSTest()
{
	string d1 = "/FakeDirectory/";
	string d2 = "/FakeDirectory";
	string f1 = "/FakeFile.ext";
	string f2 = "FakeFile.ext";
	cout << TraceviewerServer::FileUtils::CombinePaths(d1,f1)<<endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d1,f2)<<endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d2,f1)<<endl;
	cout << TraceviewerServer::FileUtils::CombinePaths(d2,f2)<<endl;
}

void GzipTest()
{
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

