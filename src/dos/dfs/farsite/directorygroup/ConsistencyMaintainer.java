package dos.dfs.farsite.directorygroup;
import dos.common.protocols.paxos.*;
import java.util.*;
import java.io.IOException;
import java.net.*;
import dos.common.util.*;
public class ConsistencyMaintainer extends Thread {
	PaxosRunner paxosRunner=null;
	public static String delim="#";
	public static String CREATE_DIR="createDir";
	public ConsistencyMaintainer(String ourIdInCluster,String clusterReplicationParliamentName,Vector<InetSocketAddress> clusterMates,int port) throws IOException{
		paxosRunner=PaxosFactory.providePaxosRunner(ourIdInCluster, clusterReplicationParliamentName, clusterMates, clusterMates, port);
		try {
			paxosRunner.startListener();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		start();
	}
	public void run(){
		while(true){
				while(!paxosRunner.thereIsNewValueToLearn());
				Tools.print("Got new consistency value");
				String value=paxosRunner.lastCommittedRoundValue();
				StringTokenizer tokenizer=new StringTokenizer(value,delim);
				String command=tokenizer.nextToken();
				if(command.equals(CREATE_DIR)){
					String namespace=tokenizer.nextToken();
					System.out.println("For consistency created "+DirectoryManager.createDirectory(new Namespace(namespace)));
				}
			
			System.out.println("Round "+paxosRunner.lastCommittedRound()+" Value "+paxosRunner.lastCommittedRoundValue());
			paxosRunner.getReadyForNextRound();
		}
	}
	public static String dirCreationMsg(Namespace namespace){
		return CREATE_DIR.concat(delim).concat(namespace.toString());
	}

	public boolean proposeDirectoryCreation(Namespace namespace) throws IOException{
		String value=dirCreationMsg(namespace);
		paxosRunner.startNewProposalRound(value);
		return !paxosRunner.proposalWasReplaced();
	}
	

	
}
