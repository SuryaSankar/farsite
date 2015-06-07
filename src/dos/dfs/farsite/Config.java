package dos.dfs.farsite;
import java.util.*;
import java.net.*;
public class Config {
	public static int directoryGroupListeningPort=10000;
	public static int fileHostListenerPort=10010;
	public static int fileHostPort=10050;
	public static int fileDownloaderPort=10100;
	public static long blockSize=33554432;
	public static int clusterConsistencyPort=10090;
	
	public static Vector<InetSocketAddress> clusterMates=new Vector<InetSocketAddress>();
	public static Vector<InetSocketAddress> allOtherHosts=new Vector<InetSocketAddress>();
	public static int _NO_OF_SIMULTANEOUS_BLOCK_ACCEPTANCES_=5;
	public static int _NO_OF_SIMULTANEOUS_BLOCK_SENDINGS_=5;
	
	public static int _NO_OF_SIMULTANEOUS_CHUNK_LOADS_=5;
	public static int _NO_OF_SIMULTANEOUS_CHUNK_DOWNLOADS_=5;
	public static int _REPLICATION_FACTOR_=1;
	
	public static boolean singleNodeMode=false;
	
	public static int MAX_HITS_PER_SUBTREE=2;
	
	public static String _INTRA_CLUSTER_ID_="000";
	
	public static String __NO_SUCH_NAMESPACE_ERROR__="__NO_SUCH_NAMESPACE_EXISTS__";
	public static String _PARSE_ERROR_="_ERROR_WHILE_PARSING_REQUEST_";
	
	public static String _USER_NAME_="user";
	public static String _FARSITE_INSTANCE_NAME_="farsite";
	public static String _COMMAND_PROMPT_="[".concat(_USER_NAME_).concat("@").concat(_FARSITE_INSTANCE_NAME_).concat("]");
	
	public static String CLUSTER_ROOT_NAMESPACE_="farsite/root";
	public static String SYSTEM_ROOT_NAMESPACE_="farsite";
	public static boolean inStandByMode=false;
	public static boolean loadingACluster=false;
	public static String _FARSITE_PAXOS_HOME="/tmp/paxos/";
	public static String _FARSITE_TEMPORARY_DOWNLOAD_LOCATION="/tmp/farsite/temp/";
	public static String _FARSITE_DEFAULT_DOWNLOAD_LOCATION_="/tmp/farsite/downloads/";
	public static String _FARSITE_FILE_HOST_STORAGE_="/tmp/farsite/blocks/";
	public static String _FARSITE_RESOURCE_PATH_="/tmp/farsite/resources/";
	public static String __NAMESPACE_MAP__=_FARSITE_RESOURCE_PATH_.concat("namespace_map");
	public static String __FILE_HOSTS__=_FARSITE_RESOURCE_PATH_.concat("file_hosts");
	public static String __CLUSTER_MATES__=_FARSITE_RESOURCE_PATH_.concat("cluster_mates_").concat(CLUSTER_ROOT_NAMESPACE_.replace('/', '_'));
	public static String __ALL_NODES__=_FARSITE_RESOURCE_PATH_.concat("hosts");
	
	public static void refreshClusterMatesFile(){
		__CLUSTER_MATES__=_FARSITE_RESOURCE_PATH_.concat("cluster_mates_").concat(CLUSTER_ROOT_NAMESPACE_.replace('/', '_'));
	}
	
	

	
	public static void setRoot(String root){
		CLUSTER_ROOT_NAMESPACE_=root;
	}
	
	
	

}
