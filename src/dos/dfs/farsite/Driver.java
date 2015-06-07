package dos.dfs.farsite;

import dos.common.util.*;
import dos.dfs.farsite.client.*;
import dos.dfs.farsite.directorygroup.*;
import dos.dfs.farsite.filehost.*;
public class Driver {
	public static void main(String args[]){
		/**
		 *Driver is expected to be called with 2/3 arguments
		 *The first argument indicates the type of instance we are about to
		 *run - client or server
		 *The second argument is the operation - start/stop for server and start for client
		 *
		 *The server starting script should therefore call 
		 *	java dos.dfs.farsite.Driver server start/stop
		 *This will start and stop a server listening at a port number specified by
		 *the parameter Config.directoryGroupListeningPort
		 *
		 *client machine too should run as a daemon. Future calls to server from
		 *client must be routed through this daemon. client has been implemented as 
		 *a flexible STDIODaemon for this purpose. 
		 *	java dos.dfs.farsite.Driver client start interactive/daemon/test
		 * interactive - expects input from the command line - run this from a new xterm
		 * daemon      - does not start any new interactive shell. To use this create a pipe
		 * 			and redirect pipe input to this daemon's input. All future client tasks must
		 *          be piped into this pipe
		 * 			mkfifo daemon_in
		 *          java dfs,farsite.Driver client start daemon < daemon_in
		 *          Make a script that would pipe in user commands into daemon_in
		 *          Eg: if[ $1 == "ls" ];then echo "ls" > daemon_in;fi;
		 *          
		 */
		String machineType=args[0];
		String operation=args[1];
		SystemInitializer.initializeNamespaceMap();
		SystemInitializer.initializeFileHosts();
		Namespace.setRoot(Config.SYSTEM_ROOT_NAMESPACE_);
		//SystemInitializer.knowAllHosts();
		if(machineType.equals("client")){
			Client client=new Client();
			if(operation.equals("start")){
				String mode=args[2];
				if(mode.equals("daemon")){
					client.start();
					Tools.print("client started");	
				}
				else if(mode.equals("interactive")){
					client.startInteractiveSession();
				}
				else if(mode.equals("test")){
					String testFileName=args[3];
					client.startInTestingMode(testFileName);
				}
			}
		}
		else if(machineType.equals("server")){
			Tools.print(args[2]);
			SystemInitializer.knowNonClusterHosts();
			SystemInitializer.initializeFileHosts();

			if(args[2].equals("standby")){
				if(args.length>3){
					Config.directoryGroupListeningPort=Integer.parseInt(args[3]);
					Config.clusterConsistencyPort=Integer.parseInt(args[4]);
			}
				Tools.print("booting");
				NameServerDaemon nameServerDaemon=new NameServerDaemon();
				nameServerDaemon.boot();
				Config.inStandByMode=true;

			}
			else{
				Config._INTRA_CLUSTER_ID_=args[2];
				Config.CLUSTER_ROOT_NAMESPACE_=args[3];
				Namespace.setRoot(Config.CLUSTER_ROOT_NAMESPACE_);
				if(args.length>4){
						Config.directoryGroupListeningPort=Integer.parseInt(args[4]);
						Config.clusterConsistencyPort=Integer.parseInt(args[5]);
				}
	
				Config.refreshClusterMatesFile();
				SystemInitializer.knowClusterMates();
				SystemInitializer.knowNonClusterHosts();
				Tools.print("starting ns");
				NameServerDaemon nameServerDaemon=new NameServerDaemon();
				Tools.print("starting cm");
				if(Config.clusterMates.size()>0){
					Tools.print("starting clusterMaintainer");
					NameServer.startConsistencyMaintainer();
				}
				else{
					Config.singleNodeMode=true;
				}
				if(operation.equals("start")){
					Tools.print("booting");
					nameServerDaemon.boot();
					Tools.print("nameServer started");
				}
				else if(operation.equals("stop")){
					nameServerDaemon.shutdown();
					Tools.print("nameServer stopped");
				}
			}
		}
		else if(machineType.equals("filehost")){
			if(args.length>2){
				Config.fileHostPort=Integer.parseInt(args[2]);
				Config.fileHostListenerPort=Integer.parseInt(args[3]);
				Config._FARSITE_FILE_HOST_STORAGE_=args[3];
			}
				
			if(operation.equals("start")){
				FileHost.start();
				Tools.print("fileHost started");
			}
		}
		
		
	}
}
