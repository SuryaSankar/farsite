package dos.common.protocols.paxos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;


import dos.common.client.CommunicationTools;
import dos.common.client.CommunicationTools.CallableQueriesAllParticipantsAndMakesDecison;
import dos.common.util.Tools;

public class Proposer {

	  
	  long proposalNumber;//A unique proposal number that should be constantly increasing. System.currentTimeMillis() concatenated with nodeId is used
	  String proposalValue;
	  public boolean proposalReplaced;//A boolean indicating if the original proposal suggested by this node was replaced during a round. It is left to node's discretion to decide on further action.
	  
	  
	  String nodeId;
	  String parliamentId;
	  Vector<InetSocketAddress> Acceptors;
	  Vector<InetSocketAddress> Learners;
	  CommonBuffer commonBuffer;

	  int quorum;
	  
	  int randomWaitTimeBetweenPrepareTries=10000;
	  int randomWaitTimeBetweenAcceptTries=10000;
	  long announcementTimeOut=5000;
	  

	  Proposer(String nodeId,String parliamentId,Vector<InetSocketAddress> Acceptors,Vector<InetSocketAddress> Learners,CommonBuffer commonBuffer){
		  this.nodeId=nodeId;
		  this.parliamentId=parliamentId;
		  this.Acceptors=Acceptors;
		  this.Learners=Learners;
		  this.commonBuffer=commonBuffer;
		  quorum=Tools.ceilingOfHalf(Acceptors.size());
		  System.out.println("quorum is "+quorum);
	  }

	  public void startNewProposalRound(String proposal) throws IOException{
	      proposalValue=proposal;
	      commonBuffer.unsetRoundOver();
	      proposalReplaced=false;
	      propose();
	  }

	  public boolean proposalWasReplaced(){
		  return proposalReplaced;
	  }
	  private void propose() throws IOException{
	             
	                 //An instance of proposer finisher when commonBuffer.roundOver is set to true
	                    while(!commonBuffer.isRoundOver()){
	                        String uniqueProposalNumberString=Long.toString(System.currentTimeMillis()).concat(nodeId);
	                        Tools.print(uniqueProposalNumberString);
	                        proposalNumber=Long.parseLong(uniqueProposalNumberString);
	                        commonBuffer.setCurrentRoundNumber(commonBuffer.getLastCommittedRound()+1);
	                        String reply=null;
							try {
								reply = (new PreparePhaseExecutor()).call();
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							if(reply==null)
								Tools.print("null prepare reply");
	                        StringTokenizer prepareReplyTokenizer=new StringTokenizer(reply);
	                        String result=prepareReplyTokenizer.nextToken();
	                        if(result.equals("InsufficientReplies")){//Abort this round
	                        	Tools.print("Got reply InsufficientReplies");
	                            proposalReplaced=true;
	                            commonBuffer.setRoundOver();
	                        }
	                        else if(result.equals("NewerProposalReported"))  {//Let it try. Try again after sometime
	                            try {
	                            	Tools.print("NewerProposal was Reported");
									Thread.sleep(randomWaitTimeBetweenPrepareTries+=(new Random().nextInt(5)*1000));
								} catch (InterruptedException e) {
									Tools.print("Exception while randomWaitTimeBetweenPrepareTries");
									e.printStackTrace();
								}
	                        }
	                        else if(result.equals("RoundOutdated")){//We are stuck in an already finished round. Should update
	                            Integer outdatedRoundNumber=Integer.valueOf(prepareReplyTokenizer.nextToken());//Redundant value. commonBuffer.currentRoundNumber holds the same value. Can be removed from message
	                            String outdatedRoundValue=prepareReplyTokenizer.nextToken();
	                            if(!commonBuffer.containsRoundValue(Integer.valueOf(outdatedRoundNumber))){//If this node's listener hadn't learnt of this round already do updation
	                                commonBuffer.putRoundValue(outdatedRoundNumber,outdatedRoundValue);
	                                commonBuffer.logRoundValue(outdatedRoundNumber, proposalValue);
	                                commonBuffer.setLastCommittedRound(outdatedRoundNumber);
	                                commonBuffer.logLastCommittedRound(outdatedRoundNumber);
	                                }
	                            commonBuffer.setRoundOver();
	                        }
	                        else if(result.equals("Success")){
	                            randomWaitTimeBetweenPrepareTries=10000;//re initialising for next round
	                            String consensusProposalValue=prepareReplyTokenizer.nextToken();
	                            if(!consensusProposalValue.equals(proposalValue)) {//Proposal value was modified as specified by Phase 2(a) of Paxos
	                                    proposalValue=consensusProposalValue;
	                                    proposalReplaced=true;
	                             }
	                            String acceptance=null;
								try {
									acceptance = (new AcceptPhaseExecutor()).call();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								if(acceptance==null){
									Tools.print("acceptance is null");
								}
	                            StringTokenizer acceptanceStatusTokenizer=new StringTokenizer(acceptance);
	                            String acceptanceStatus=acceptanceStatusTokenizer.nextToken();
	                            if(acceptanceStatus.equals("Success")){
	                                 
										(new CommunicationTools.Teacher(Learners, Integer.toString(commonBuffer.getCurrentRoundNumber()).concat(" LEARN ").concat(proposalValue), announcementTimeOut)).teach();

	                                 if(!commonBuffer.containsRoundValue(Integer.valueOf(commonBuffer.getCurrentRoundNumber()))){//This node would have learnt this value if it were a learner. Otherwise log it
	                                    commonBuffer.putRoundValue(Integer.valueOf(commonBuffer.getCurrentRoundNumber()),proposalValue);
	                                    commonBuffer.logRoundValue(commonBuffer.getCurrentRoundNumber(), proposalValue);
	                                    commonBuffer.setLastCommittedRound(commonBuffer.getCurrentRoundNumber());
	                                    commonBuffer.logLastCommittedRound(commonBuffer.getCurrentRoundNumber());
	                                    }
	                                commonBuffer.setRoundOver();
	                             }
	                            else if(acceptanceStatus.equals("ProposalOutdated")){
	                                    try {
											Thread.sleep(randomWaitTimeBetweenAcceptTries+=((new Random().nextInt(5))*1000));
										} catch (InterruptedException e) {
											Tools.print("Exception while randomWaitTimeBetweenPrepareTries");
											e.printStackTrace();
										}
	                             }
	                            else if(acceptanceStatus.equals("RoundOutdated")){//Similar to what we did with such a reply to PREPARE
	                                 Integer outdatedRoundNumber=Integer.valueOf(acceptanceStatusTokenizer.nextToken());
	                                 String outdatedRoundValue=acceptanceStatusTokenizer.nextToken();
	                                 if(!commonBuffer.containsRoundValue(Integer.valueOf(outdatedRoundNumber))){
	                                    commonBuffer.putRoundValue(Integer.valueOf(outdatedRoundNumber),outdatedRoundValue);
	                                    commonBuffer.logRoundValue(outdatedRoundNumber, result);
	                                    commonBuffer.setLastCommittedRound(commonBuffer.getCurrentRoundNumber());
	                                    commonBuffer.logLastCommittedRound(commonBuffer.getCurrentRoundNumber());
	                                    }
	                                commonBuffer.setRoundOver();
	                                }
	                       }
	                    }//end while
	                   System.out.println("Round is Over\n");

	    }

	  
	  class PreparePhaseExecutor extends CallableQueriesAllParticipantsAndMakesDecison{
       int repliesToPrepare=0;
       long highestProposalNumberReportedInReplies=0;
       String latestReportedProposalValue=proposalValue;
   	  public PreparePhaseExecutor() {
			super(Acceptors,Integer.toString(commonBuffer.getCurrentRoundNumber()).concat(" PREPARE ").concat(Long.toString(proposalNumber)));
		}
		  public void handleReply(String reply){
              StringTokenizer replyTokenizer=new StringTokenizer(reply);
              String acceptance=replyTokenizer.nextToken();
              System.out.println("Got reply to prepare request "+acceptance);
              if(acceptance.equals("Ready")){
                  repliesToPrepare++;
                  int roundOfReplier=Integer.parseInt(replyTokenizer.nextToken());
                  if(roundOfReplier==commonBuffer.getCurrentRoundNumber()){//consider only the replies from this round to avoid getting corrupted by past data
                  long lastAcceptedProposalNumberOfThisAcceptor=Long.parseLong(replyTokenizer.nextToken());
                      if(lastAcceptedProposalNumberOfThisAcceptor!=0){
                          String lastAcceptedValueOfThisAcceptor=replyTokenizer.nextToken();
                          if(lastAcceptedProposalNumberOfThisAcceptor>highestProposalNumberReportedInReplies){
                             highestProposalNumberReportedInReplies= lastAcceptedProposalNumberOfThisAcceptor;
                             latestReportedProposalValue=lastAcceptedValueOfThisAcceptor;
                          }
                      }
                  }
              }
              else if(acceptance.equals("NewerProposalReported")||acceptance.equals("RoundOutdated"))
                  makeDecisionHalfWay(acceptance);
		  }
		  public void evaluateRepliesAndMakeDecision(){
	            if(repliesToPrepare>(quorum))
	                makeDecision( "Success ".concat(latestReportedProposalValue));
	            else
	                makeDecision("InsufficientReplies");
		  }
		  
	  }

	  class AcceptPhaseExecutor extends CallableQueriesAllParticipantsAndMakesDecison{
		  int repliesToAccept=0;
		  public AcceptPhaseExecutor(){
			  super(Acceptors,Integer.toString(commonBuffer.getLastCommittedRound()+1).concat(" ACCEPT ").concat(Long.toString(proposalNumber)).concat(" ").concat(proposalValue));
		  }
		  public void handleReply(String reply){
              StringTokenizer replyTokenizer=new StringTokenizer(reply);
              String acceptance=replyTokenizer.nextToken();
              if(acceptance.equals("Accepted"))   repliesToAccept++;
              else if(acceptance.equals("ProposalOutdated")) makeDecisionHalfWay("ProposalOutdated");
              else if(acceptance.equals("RoundOutdated"))   makeDecisionHalfWay(reply);
		  }
		  public void evaluateRepliesAndMakeDecision(){
	            if(repliesToAccept>=(quorum))  makeDecision("Success");
	            else makeDecision("TryAgain");
		  }
	  }
	  

}
