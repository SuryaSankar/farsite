package dos.common.protocols.paxos;

import java.io.IOException;
import java.util.Hashtable;

import dos.common.util.TextFileHandle;
import dos.common.util.Tools;

public class CommonBuffer {
    String ParliamentId;

    
    TextFileHandle LastCommittedRoundLog;
    TextFileHandle RoundValueRegister;
    
    boolean roundOver;//indicates stability. A Paxos Round continues running on a node as long as this is false. When this is set to true, the round is complete.
    int currentRoundNumber;//Unique id for each round. Necessary to ensure that past round values don't conflict with current round
    int lastCommittedRound;//Knowing this is essential to initialise currentRound. We set currentRoundNumber=lastCommittedRound+1 always

	Hashtable<Integer,String> RoundsNValues;//Map Values To Rounds
	
	static String roundNumberValueSeparator=" ";
	
	public CommonBuffer(String ParliamentId){
		RoundsNValues=new Hashtable<Integer, String>();
		this.ParliamentId=ParliamentId;
		int i=0;
		LastCommittedRoundLog=new TextFileHandle(ProtocolParameters.__LAST_COMMITTED_ROUND_LOG__.concat(ParliamentId));
		RoundValueRegister=new TextFileHandle(ProtocolParameters.__REGISTER__.concat(ParliamentId));
		roundOver=false;
		Tools.print("Initialised common Buffer");
	}
	public void setStartUpParameters(){
		lastCommittedRound=Integer.parseInt(ProtocolParameters.LAST_COMMITTED_ROUND_AT_STARTUP);
	}
	
	public void initialiseLogs() throws IOException{
		LastCommittedRoundLog.writeline(ProtocolParameters.LAST_COMMITTED_ROUND_AT_STARTUP);
	}
	public void rememberLastCommittedRound() throws IOException{
		LastCommittedRoundLog.openForRead();
		lastCommittedRound=Integer.parseInt(LastCommittedRoundLog.readLine());
		LastCommittedRoundLog.closeIn();
	}
	
	public void logRoundValue(int round,String value) throws IOException{
		RoundValueRegister.openForWrite(true);
		RoundValueRegister.writeline(Integer.toString(round).concat(roundNumberValueSeparator).concat(value));
		RoundValueRegister.closeOut();
	}
	
	public void logLastCommittedRound(int round) throws IOException{
		LastCommittedRoundLog.openForWrite(false);
		LastCommittedRoundLog.writeline(Integer.toString(round));
		LastCommittedRoundLog.closeOut();
	}
	
	public synchronized void setRoundOver(){
		roundOver=true;
	}
	public synchronized void unsetRoundOver(){
		roundOver=false;
	}
	public synchronized void putRoundValue(int round,String value){
		RoundsNValues.put(round,value);
	}
	public synchronized boolean containsRoundValue(int round){
		return RoundsNValues.containsKey(round);
	}
	public synchronized String getRoundValue(int round){
		return RoundsNValues.get(round);
	}
	public synchronized String getLastCommittedRoundValue(){
		return RoundsNValues.get(lastCommittedRound);
	}
	public synchronized boolean isRoundOver(){
		return roundOver;
	}
	
	public synchronized void setCurrentRoundNumber(int round){
		currentRoundNumber=round;
	}
	public synchronized int getLastCommittedRound(){
		return lastCommittedRound;
	}
	public synchronized int getCurrentRoundNumber(){
		return currentRoundNumber;
	}
	public synchronized void setLastCommittedRound(int round){
		lastCommittedRound=round;
	}
}
