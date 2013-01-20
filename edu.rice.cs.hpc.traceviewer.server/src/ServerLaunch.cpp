#include <fstream>
#include <vector>
#include <map>
#include <sstream>


#include "FileUtils.h"
#include "Server.h"
#include "Slave.h"
#include "MPICommunication.h"
#include "Constants.h"
//#include "optionparser.h"

#ifdef USE_MPI
#include "mpi.h"
using namespace MPI;
#endif

using namespace std;


bool ParseCommandLineArgs(int, char*[]);
int GetIntFromString(string st, bool& valid);

int main(int argc, char *argv[])
{
	bool ActuallyRun = ParseCommandLineArgs(argc, argv);
	if (!ActuallyRun)
		return 0;

#ifdef USE_MPI
	MPI::Init(argc, argv);
	int rank, size;
	rank = MPI::COMM_WORLD.Get_rank();
	size = MPI::COMM_WORLD.Get_size();

	if (size == 1)
		cout<<"In order to use the MPI implementation of this server, you need more than one node. "
		<<"Please try running again, or run the non-MPI version. See the docs for more information."<<endl;
	else if (rank == TraceviewerServer::MPICommunication::SOCKET_SERVER)
	{
		try
		{
			TraceviewerServer::Server::Begin();
		}
		catch (int e)
		{
			cerr<<"Error code " << e<<endl;
		}
		cout<<"Server done, closing..."<<endl;
		TraceviewerServer::MPICommunication::CommandMessage serverShutdown;
		serverShutdown.Command = DONE;
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
#ifdef USE_MPI
	MPI::Finalize();
#endif
	return 0;
}

enum CLoption
{
	null, help, compression, com_port, xml_port, yes, no
};
#define EXECUTABLE_NAME "edu.rice.cs.hpc.traceviewer.server"

#ifdef USE_MPI
#define USAGE_TEXT "USAGE: mpirun -n {nodes to use} " EXECUTABLE_NAME " [options]\n" \
"mpiexec -n {nodes to use} " EXECUTABLE_NAME " [options]\n\nOptions:"
#else
#define USAGE_TEXT "USAGE: " EXECUTABLE_NAME " [options]\n\nOptions:"
#endif
void PrintHelp()
{
	cout << "This is a server for hpctraceviewer. "<<endl<< USAGE_TEXT<<endl
			<<"  -h, --help	\n\t\tPrint help then exit."<<endl
			<<"  -c, --compression [on/off, yes/no, true/false] \n\t\tTurns data compression on or off"<<endl
			<<"  -p [port number], --port [port number]\n\t\tSpecifies the port number to use for communication."
			<<"Should be at least 1024. Use 0 to autoselect an open port, or do not specify option for default port (21590)."<<endl
			<<"  --xml_port [port number], --xmlport [port number]\n\t\tSpecifies the port on which the xml data will be sent. Also should be at least 1024."
			"Use -1 to force using the main port (typically used with --port 0). Use 0 to autoselect an open port."
						<< endl;
}
/*http://optionparser.sourceforge.net/index.html
 * bool ParseCommandLineArgs2(int argc, char *argv[])
{
	const option::Descriptor usage[] = {
			//Index,			type (for index conflicts),	Short form,		Long form, 		Arg checker, 					Help text
			{null,				0, 													"", 					"", 					option::Arg::None, 		USAGE_TEXT},
			{help,				0,													"hH", 				"help",				option::Arg::None,		"  -h, --help	\tPrint help then exit."},
			{compression, 0,													"c",					"compression",option::Arg::Optional,"  -c, --compression [on/off, yes/no, true/false] \tTurns data compression on or off"},
			{compression, 0, 													"C", 					"compr",			option::Arg::Optional,"  --compr \tA synonym for --compression"},
			{com_port,		0,													"pP",					"port",				option::Arg::Optional,"  -p [port number], --port [port number]\tSpecifies the port number to use for communication."
																																																	"Should be at least 1024. Use 0 to autoselect an open port, or do not specify option for default port (21590)."},
			{xml_port,		0,													"",						"xml_port",		option::Arg::Optional,"  --xml_port [port number]\tSpecifies the port on which the xml data will be sent. Also should be at least 1024."
																																																	"Use -1 to force using the main port (typically used with --port 0). Use 0 to autoselect an open port."}
	};
	 option::Stats  stats(usage, argc, argv);
	 option::Option options[stats.options_max], buffer[stats.buffer_max];
	 option::Parser parse(usage, argc, argv, options, buffer);
	 if (parse.error())
		 return false;
   if (options[help] || argc == 0) {
     option::printUsage(std::cout, usage);
     return 0;
    string Compresion(options[compression].last()->arg);

   }
}*/

bool ParseCommandLineArgs(int argc, char *argv[])
{

	typedef map<string, CLoption> Map;
	const Map::value_type vals[] =
	{
	make_pair( "", null),
	make_pair( "-h", help ),
	make_pair( "--help", help ),
	make_pair( "-c", compression ),
	make_pair( "--compression", compression ),
	make_pair( "--comp", compression ),
	make_pair( "-p", com_port ),
	make_pair( "--port", com_port ),
	make_pair( "--xmlport", xml_port ),
	make_pair( "--xml_port", xml_port),
	make_pair( "true", yes ),
	make_pair( "t", yes ),
	make_pair( "yes", yes ),
	make_pair( "y", yes ),
	make_pair( "on", yes),
	make_pair( "false", no ),
	make_pair( "f", no ),
	make_pair( "no", no ),
	make_pair( "n", no ),
	make_pair( "off", no)
	};
	const Map Parser (vals, vals + sizeof(vals) / sizeof (vals[0]));

	int c  = 0;
	while (++c < argc)
	{
		char* strOpt = argv[c];
		int i;
		//Convert to lower case
		while (strOpt[i]) {strOpt[i] = tolower(strOpt[i]); i++;}

		CLoption op = Parser.find(strOpt)->second;
		switch (op)
		{
			case compression:
				c++;//compression takes two arguments, so advance. Also a pun :)
				if (c < argc)
				{ //Possibly passed a true/false
					CLoption OnOrOff = Parser.find(argv[c])->second;
					if (OnOrOff == yes)
						TraceviewerServer::Compression = true;
					else if (OnOrOff == no)
						TraceviewerServer::Compression = false;
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
				break;
			case com_port:
				c++;//See similar line above
				if (c < argc)
				{ //Possibly a number follows
					bool valid = false;
					int val = GetIntFromString(argv[c], valid);
					if (!valid)
					{
						cout<<"Could not parse port number"<<endl;
						PrintHelp();
						return false;
					}
					if ((val < 1024) && (val != 0))
					{
						cout << "Port cannot be less than 1024"<<endl;
						PrintHelp();
						return false;
					}
					else
						TraceviewerServer::MainPort = val;
				}
				else //--port was the last argument
				{
					PrintHelp();
					return false;
				}
				break;
			case xml_port:
				c++;//See similar line above.
				if (c < argc)
				{ //Possibly a number follows
					bool valid = false;
					int val = GetIntFromString(argv[c], valid);
					if (!valid)
					{
						cout << "Could not parse xml port number" << endl;
						PrintHelp();
						return false;
					}
					if ((val < 1024) &&(val != 0) && (val != -1))
					{
						cout<<"Invalid XML port number."<<endl;
						PrintHelp();
						return false;
					}
					else
					{
						TraceviewerServer::XMLPort = val;
						break;
					}
				}
				else //--xml_port was the last argument
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
#define STRING_NOT_A_NUMBER -1
int GetIntFromString(string st, bool& valid)
{
	int result;
	if(stringstream(st) >> result)
		{
		valid = true;
		return result;}
	else
	{
		valid = false; return STRING_NOT_A_NUMBER;
		}
}

/*void WriteVector(vector<string> t)
 {
 cout << "Writing vector of length "<< t.size()<<endl;
 for (int var = 0; var < t.size(); var++) {
 cout<<"\t["<<var<<"] = " << t[var]<<endl;
 }

 }*/

