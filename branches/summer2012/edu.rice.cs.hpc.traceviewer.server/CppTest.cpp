//#include "mpi.h"
#include "ServerLaunch.h"
#include <fstream>
#include "boost/asio.hpp"
using namespace std;
//using namespace MPI;

int main(int argc, char *argv[]) {

	cout << "Yes! It actually runs!"<<endl;

	TraceviewerServer::ServerLaunch::main(argc, argv);
	ofstream g;

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
void HelloWorld() {
	//int rank, size;
	//rank = MPI::COMM_WORLD.Get_rank();
	//size = MPI::COMM_WORLD.Get_size();
	//cout << "Hello World! (" << rank << ", " << size << ")\n";
}

