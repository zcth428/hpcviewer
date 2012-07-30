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

bool ParseCommandLineArgs(int, char*[]);

int main(int argc, char *argv[])
{
	bool ActuallyRun = ParseCommandLineArgs(argc, argv);
	if (!ActuallyRun)
		return 0;

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
		}
		catch (int e)
		{

		}
		cout<<"Server done, closing..."<<endl;
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
		}
		catch (int e)
		{

		}
	}
#else
	try
	{
		TraceviewerServer::Server::main(argc, argv);
	} catch (int e)
	{
		cout << "Closing with error code " << e << endl;
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
void PrintHelp()
{
	cout << "This is a server for the HPCTraceviewer. Usage: INSERT THE USAGE HERE"
			<< endl;
}
typedef enum
{
	null, help, compression, com_port, xml_port, yes, no
} CLoption;

bool ParseCommandLineArgs(int argc, char *argv[])
{

	typedef map<string, CLoption> Map;
	const Map::value_type vals[] =
	{
	make_pair( "", null),
	make_pair( "-h", help ),
	make_pair( "-help", help ),
	make_pair( "--help", help ),
	make_pair( "--Help", help ),
	make_pair( "-c", compression ),
	make_pair( "--compression", compression ),
	make_pair( "--Compression", compression ),
	make_pair( "--comp", compression ),
	make_pair( "--Comp", compression ),
	make_pair( "-p", com_port ),
	make_pair( "--port", com_port ),
	make_pair( "--Port", com_port ),
	make_pair( "--xmlport", xml_port ),//This should be undocumented, because the server should auto-negotiate it
	make_pair( "true", yes ),
	make_pair( "t", yes ),
	make_pair( "True", yes ),
	make_pair( "yes", yes ),
	make_pair( "Yes", yes ),
	make_pair( "y", yes ),
	make_pair( "false", no ),
	make_pair( "f", no ),
	make_pair( "False", no ),
	make_pair( "no", no ),
	make_pair( "No", no ),
	make_pair( "n", no )};
	const Map Parser (vals, vals + sizeof(vals) / sizeof (vals[0]));


	for (int c = 1; c < argc; c += 2)
	{
		CLoption op = Parser.find(argv[c])->second;
		switch (op)
		{
			case compression:
				if (c + 1 < argc)
				{ //Possibly passed a true/false
					CLoption OnOrOff = Parser.find(argv[c+1])->second;
					if (OnOrOff == yes)
					{
						TraceviewerServer::Compression = true;
						break;
					}
					else if (OnOrOff == no)
					{
						TraceviewerServer::Compression = false;
						break;
					}
					else
					{
						PrintHelp();
						return false;
					}
				}
				else
				{
					PrintHelp();
					return false;
				}
			case com_port:
				if (c + 1 < argc)
				{ //Possibly a number following
					int val = atoi(argv[c + 1]);
					if (val < 1024)
					{
						PrintHelp();
						return false;
					}
					else
					{

						TraceviewerServer::MainPort = val;
						break;
					}
				}
				else //--port was the last argument
				{
					PrintHelp();
					return false;
				}
				break;
			case xml_port:
				if (c + 1 < argc)
				{ //Possibly a number following
					int val = atoi(argv[c + 1]);
					if (val < 1024)
					{
						PrintHelp();
						return false;
					}
					else
					{
						TraceviewerServer::XMLPort = val;
						break;
					}
				}
				else //--port was the last argument
				{
					PrintHelp();
					return false;
				}
				break;
			default:
			case help:
				PrintHelp();
				return false;
		}
	}
	return true;
}

/*void WriteVector(vector<string> t)
 {
 cout << "Writing vector of length "<< t.size()<<endl;
 for (int var = 0; var < t.size(); var++) {
 cout<<"\t["<<var<<"] = " << t[var]<<endl;
 }

 }*/

