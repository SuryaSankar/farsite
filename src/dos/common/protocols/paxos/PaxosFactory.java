package dos.common.protocols.paxos;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import dos.common.util.*;

public class PaxosFactory {

	public static PaxosRunner providePaxosRunner(String UniqueNodeId,String ParliamentId,Vector<InetSocketAddress> Acceptors,Vector<InetSocketAddress> Learners,int ListeningPort,String PaxosFilePath) throws IOException{
		ProtocolParameters.reconfigurePaxosHome(PaxosFilePath);
		PaxosRunner paxosRunner=new PaxosRunner();
		CommonBuffer commonBuffer=new CommonBuffer(ParliamentId);
		boolean isFreshRun=commonBuffer.LastCommittedRoundLog.notFound()||commonBuffer.LastCommittedRoundLog.isEmpty();
		if(isFreshRun)
			commonBuffer.setStartUpParameters();
		else
			commonBuffer.rememberLastCommittedRound();
		Listener listener=new Listener(ListeningPort,commonBuffer,ParliamentId);
		if(isFreshRun)
			listener.setStartupParameters();
		else{
			listener.rememberLastAcceptedValueDetails();
			listener.rememberLatestProposalNumberSeen();
		}
		Proposer proposer=new Proposer(UniqueNodeId,ParliamentId,Acceptors,Learners,commonBuffer);
		paxosRunner.setProposer(proposer);
		paxosRunner.setListener(listener);
		return paxosRunner;
	}
	public static PaxosRunner providePaxosRunner(String UniqueNodeId,String ParliamentId,Vector<InetSocketAddress> Acceptors,Vector<InetSocketAddress> Learners,int ListeningPort) throws IOException{
		PaxosRunner paxosRunner=new PaxosRunner();
		CommonBuffer commonBuffer=new CommonBuffer(ParliamentId);
		boolean isFreshRun=commonBuffer.LastCommittedRoundLog.notFound()||commonBuffer.LastCommittedRoundLog.isEmpty();
		if(isFreshRun)
			commonBuffer.setStartUpParameters();
		else
			commonBuffer.rememberLastCommittedRound();
		Listener listener=new Listener(ListeningPort,commonBuffer,ParliamentId);
		if(isFreshRun)
			listener.setStartupParameters();
		else{
			listener.rememberLastAcceptedValueDetails();
			listener.rememberLatestProposalNumberSeen();
		}
		Proposer proposer=new Proposer(UniqueNodeId,ParliamentId,Acceptors,Learners,commonBuffer);
		paxosRunner.setProposer(proposer);
		paxosRunner.setListener(listener);
		return paxosRunner;
	}
	
	public static void main(String args[]){
	    try {
			final PaxosRunner paxosRunner=PaxosFactory.providePaxosRunner(args[0], args[1], Tools.readHostsFromFile(args[2]), Tools.readHostsFromFile(args[3]), Integer.parseInt(args[4]),"/tmp/paxos/");
			try {
				paxosRunner.startListener();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new Thread(
					new Runnable(){
							public void run(){
								String proposalValue;
								while(true){
			                        try {
										Thread.sleep((new Random().nextInt(10)+10)*1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			                        proposalValue=Integer.toString(new Random().nextInt(10000));
			                        System.out.println("Proposing "+proposalValue);
			                        try {
										paxosRunner.startNewProposalRound(proposalValue);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			                        if(paxosRunner.proposalWasReplaced())
			                        	Tools.print("Proposal Was Replaced");
								}
							}
						}
					).start();
			new Thread(
					new Runnable(){
							public void run(){
								while(!paxosRunner.thereIsNewValueToLearn());
								System.out.println("Round "+paxosRunner.lastCommittedRound()+" Value "+paxosRunner.lastCommittedRoundValue());
								paxosRunner.getReadyForNextRound();
							}
						}
					).start();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
