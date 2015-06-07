package dos.common.protocols.paxos;
import dos.common.*;


public class ProtocolParameters {
	static String _PAXOS_HOME_="/tmp/paxos/";
    static String __LAST_ACCEPTED_VALUE_LOG__="/tmp/paxos/lav";
    static String __LAST_COMMITTED_ROUND_LOG__="/tmp/paxos/lcr";
    static String __REGISTER__="/tmp/paxos/register";
    static String __LATEST_PROPOSAL__="/tmp/paxos/lp";
    static String __ACCEPTORS__="acceptors";
    static String __LEARNERS__="learners";
    
    static void reconfigurePaxosHome(String PaxosFilePath){
		if(PaxosFilePath.endsWith("/"))
			_PAXOS_HOME_=PaxosFilePath;
		else _PAXOS_HOME_=PaxosFilePath.concat("/");
		
		__LAST_ACCEPTED_VALUE_LOG__=_PAXOS_HOME_.concat("lav");
		__LAST_COMMITTED_ROUND_LOG__=_PAXOS_HOME_.concat("lcr");
		__LATEST_PROPOSAL__=_PAXOS_HOME_.concat("lp");
		__REGISTER__=_PAXOS_HOME_.concat("register");
		}
    static int RANDOM_WAIT_BETWEEN_PREPARE_TRIES=3000;
    static int RANDOM_WAIT_BETWEEN_ACCEPT_TRIES=3000;
    static int THREAD_SLEEP_TIME=Parameters.THREAD_SLEEP_TIME;
    static long RESPONSE_TIMEOUT=Parameters.RESPONSE_TIMEOUT;
    static int MAX_CONN_ATTEMPTS=Parameters.MAX_CONN_ATTEMPTS;
    
    static int CONNECTION_POOL_SIZE=30;
    
    static String LAST_COMMITTED_ROUND_AT_STARTUP="0";
    static String LAST_ACCEPTED_VALUE_AT_STARTUP="0 0 null";
    static String LATEST_PROPOSAL_AT_STARTUP="0";
}
