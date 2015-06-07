package dos.common.protocols.paxos;
import dos.common.server.*;
import dos.common.util.TextFileHandle;
import dos.common.util.Tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.StringTokenizer;
public class Listener extends ReplyingServer{

	String ParliamentId;
    String lastAcceptedProposalValue;
    long lastAcceptedProposalNumber;
    long latestProposalNumberSeen;
    public boolean thereIsNewValueToLearn;//A flag for the user to know that a value has been learnt and should be used
    String roundNumberOfLastAcceptedValue;
    CommonBuffer commonBuffer;
    
	TextFileHandle LastAcceptedValueLog;
	TextFileHandle LatestKnownProposalLog;
    
    void logLastAcceptedValueDetails(String roundNumber, long proposalNumber,  String value) throws IOException{
    	LastAcceptedValueLog.openForWrite(false);
        LastAcceptedValueLog.writeline(roundNumber.concat(" ").concat(Long.toString(proposalNumber)).concat(" ").concat(value));
        LastAcceptedValueLog.closeOut();
    }
     void logLatestProposalNumberSeen(long proposalNumber) throws IOException{
    	 LatestKnownProposalLog.openForWrite(false);
        LatestKnownProposalLog.writeline(Long.toString(proposalNumber));
        LatestKnownProposalLog.closeOut();
    }
     public void readyForNextRound(){//user calls this after learning a value
         thereIsNewValueToLearn=false;
     }
     public void initialiseLogs() throws IOException{
 		LastAcceptedValueLog.writeline(ProtocolParameters.LAST_ACCEPTED_VALUE_AT_STARTUP);
 		LatestKnownProposalLog.writeline(ProtocolParameters.LATEST_PROPOSAL_AT_STARTUP);
     }
     public void setStartupParameters(){
 		StringTokenizer tokenizer=new StringTokenizer(ProtocolParameters.LAST_ACCEPTED_VALUE_AT_STARTUP);
		roundNumberOfLastAcceptedValue=tokenizer.nextToken();
		lastAcceptedProposalNumber=Long.parseLong(tokenizer.nextToken());
		lastAcceptedProposalValue=tokenizer.nextToken();
     }
 	public void rememberLastAcceptedValueDetails() throws IOException{
 		LastAcceptedValueLog.openForRead();
		StringTokenizer tokenizer=new StringTokenizer(LastAcceptedValueLog.readLine());
		roundNumberOfLastAcceptedValue=tokenizer.nextToken();
		lastAcceptedProposalNumber=Long.parseLong(tokenizer.nextToken());
		lastAcceptedProposalValue=tokenizer.nextToken();
		LastAcceptedValueLog.closeIn();
	}
	public void rememberLatestProposalNumberSeen() throws NumberFormatException, IOException{
		LatestKnownProposalLog.openForRead();
		latestProposalNumberSeen=Long.parseLong(LatestKnownProposalLog.readLine());
		LatestKnownProposalLog.closeIn();
	}
	public boolean thereIsNewValueToLearn(){
		return thereIsNewValueToLearn;
	}
	public int lastCommittedRound(){
		return commonBuffer.getLastCommittedRound();
	}
    public String lastCommittedRoundValue(){
    	return commonBuffer.getLastCommittedRoundValue();
    }
	public Listener(int port,CommonBuffer commonBuffer,String ParliamentId){
		super(port);
        thereIsNewValueToLearn=false;
        this.commonBuffer=commonBuffer;
        this.ParliamentId=ParliamentId;
        LastAcceptedValueLog=new TextFileHandle(ProtocolParameters.__LAST_ACCEPTED_VALUE_LOG__.concat(ParliamentId));
        LatestKnownProposalLog=new TextFileHandle(ProtocolParameters.__LATEST_PROPOSAL__.concat(ParliamentId));
	}
	public ListenerRequestHandler provideRequestHandler(Socket connection){
		return new ListenerRequestHandler(connection);
	}
	
	class ListenerRequestHandler extends ReplyingServerRequestHandler{
		public ListenerRequestHandler(Socket connection){
			super(connection);
		}
		public String replyToRequest(String request) throws IOException{
			System.out.println("Got request "+request);

            StringTokenizer requestTokenizer=new StringTokenizer(request);
            String requestRoundNumber=requestTokenizer.nextToken();
            String reply;
            if(Integer.parseInt(requestRoundNumber)<=commonBuffer.getLastCommittedRound()){//A message from the past
                if(requestTokenizer.nextToken().equals("LEARN")&&!commonBuffer.containsRoundValue(Integer.parseInt(requestRoundNumber))){//if it is a LEARN msg for a round we missed use it else reply with a ROundOutdated msg and the value of that round
                            reply="Learnt";
                            String valueLearnt=requestTokenizer.nextToken();
                            System.out.println("Learnt "+valueLearnt); 
                            commonBuffer.putRoundValue(Integer.valueOf(requestRoundNumber),valueLearnt);
                            commonBuffer.logRoundValue(Integer.parseInt(requestRoundNumber), valueLearnt);
                            commonBuffer.setRoundOver();
                            thereIsNewValueToLearn=true;
                }
                else
                   reply="RoundOutdated "+requestRoundNumber+" "+commonBuffer.getRoundValue(Integer.valueOf(requestRoundNumber));
            }
            else{
                String requestHeader=requestTokenizer.nextToken();
                if(requestHeader.equals("PREPARE")){
                	int i=0;
                	Tools.print(1);
                       long thisProposalNumber=Long.parseLong(requestTokenizer.nextToken());
                       Tools.print(2);
                       if(thisProposalNumber<latestProposalNumberSeen){ reply="NewerProposalReported";Tools.print(3);}
                       else{
                    	   Tools.print(4);
                           latestProposalNumberSeen=thisProposalNumber;
                           logLatestProposalNumberSeen(thisProposalNumber);
                           Tools.print(5);
                           if(lastAcceptedProposalNumber==0)
                               reply="Ready 0";
                           else
                            reply="Ready ".concat(" ").concat(roundNumberOfLastAcceptedValue).concat(" ").concat(Long.toString(lastAcceptedProposalNumber)).concat(" ").concat(lastAcceptedProposalValue);
                   }
                }
                else if(requestHeader.equals("ACCEPT")){
                       long thisProposalNumber=Long.parseLong(requestTokenizer.nextToken());
                       String thisProposalValue=requestTokenizer.nextToken();
                       if(thisProposalNumber<latestProposalNumberSeen){
                           reply="ProposalOutdated";
                       }
                       else{
                           reply="Accepted";
                           roundNumberOfLastAcceptedValue=requestRoundNumber;
                           lastAcceptedProposalNumber=thisProposalNumber;
                           lastAcceptedProposalValue=thisProposalValue;
                           logLastAcceptedValueDetails(roundNumberOfLastAcceptedValue,lastAcceptedProposalNumber,lastAcceptedProposalValue);
                       }
                }
                else if(requestHeader.equals("LEARN")){
                        reply="Learnt";
                        String valueLearnt=requestTokenizer.nextToken();
                        commonBuffer.setLastCommittedRound(Integer.parseInt(requestRoundNumber));
                       
                        commonBuffer.logLastCommittedRound(commonBuffer.getLastCommittedRound());
                        Tools.print("logged last committed round");
                        commonBuffer.putRoundValue(Integer.valueOf(requestRoundNumber),valueLearnt);
                        commonBuffer.logRoundValue(Integer.parseInt(requestRoundNumber), valueLearnt);
                        Tools.print("logged value register");
                        commonBuffer.setRoundOver();
                        thereIsNewValueToLearn=true;
                        lastAcceptedProposalValue=null;
                        lastAcceptedProposalNumber=0;
                        latestProposalNumberSeen=0;
                        logLastAcceptedValueDetails("0",0,"NULL");//Record in the log that we have learnt all accepted values and no pending learning remains
                        Tools.print("logged last accepted value");
                }
                else reply="CorruptedHeader";
              }
            System.out.println("replying to "+request+" with "+reply);
            return reply;
        	
        
		}
	}
}
