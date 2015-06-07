package dos.common.protocols.paxos;

import java.io.IOException;

public class PaxosRunner {
    public Listener listener;//Includes both acceptors and learners
    public Proposer proposer;
    public void setProposer(Proposer proposer){
    	this.proposer=proposer;
    }
    public void setListener(Listener listener){
    	this.listener=listener;
    }
    public void startListener() throws Exception{
    	listener.boot();
    }
    public void stopListener(){
    	listener.shutdown();
    }
    
    public void startNewProposalRound(String proposal) throws IOException{
    	proposer.startNewProposalRound(proposal);
    }
    public boolean thereIsNewValueToLearn(){
    	return listener.thereIsNewValueToLearn();
    }
    
    public String lastCommittedRoundValue(){
    	return listener.lastCommittedRoundValue();
    }
    public int lastCommittedRound(){
    	return listener.lastCommittedRound();
    }
    public void getReadyForNextRound(){
    	listener.readyForNextRound();
    }
    public boolean proposalWasReplaced(){
    	return proposer.proposalWasReplaced();
    }
}
