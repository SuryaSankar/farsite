package dos.dfs.farsite;

import java.net.InetSocketAddress;
import java.util.StringTokenizer;

import dos.common.fs.file.BlockId;
import dos.common.fs.file.FileTransferParams;
import dos.common.util.Namespace;
import dos.common.util.Tools;
import dos.common.fs.*;
import java.util.*;
public class Messages {
	public static String Command_Params_Separator="|";
	public static String Params_Separator=",";
	public static String READ_METADATA="ls";
	public static String CREATE_DIRECTORY="mkdir";
	public static String REMOVE_DIRECTORY="rmdir";
	public static String LOAD_RESOURCE="put";
	public static String LOADED_RESOURCE="loaded";
	public static String DOWNLOAD_RESOURCE="get";
	public static String PERFORM_COMPUTATION="do";
	public static String CREATE_AND_GIVE_HOSTS_AND_ID="createFileAndGiveHosts";
	
	public static String LOADED_BLOCK="loadedblock";

	public static String REMAP_NAMESPACE="remap";
	public static String CREATED_DIRECTORY="created";
	public static String PARENT_DIRECTORY_DOES_NOT_EXIST="parent directory does not exist";
	public static String DIRECTORY_ALREADY_EXISTS="directory already exists";
	
	public static String NO_SUCH_FILE_EXCEPTION="No such file exists";
	public static String FILE_BLOCKS_MISSING_EXCEPTION="File blocks missing";
	
	public static String START_CLUSTER="startCluster";
	public static String ALREADY_RUNNING_A_CLUSTER="alreadyRunning";
	public static String STARTED_CLUSTER="started";
	public static String FAILED_TO_START_CLUSTER="cluster_start_failed";
	

	public static String fromAddressMessageSeparator="%%";
	public static String clusterHostsFileServerRequestSeparator="~";
	/*
	 * The following methods specify the format of requests to the name server
	 */
	public static String LS(Namespace namespace){
		return READ_METADATA.concat(Command_Params_Separator).concat(namespace.toString());
	}
	public static String MKDIR(Namespace namespace){
		return CREATE_DIRECTORY.concat(Command_Params_Separator).concat(namespace.toString());
	}
	public static String REQUEST_HOSTS_TO_LOAD_TO(Namespace namespace,long fileSize){
		return LOAD_RESOURCE.concat(Command_Params_Separator).concat(namespace.toString()).concat(Params_Separator).concat(Long.toString(fileSize));
	}
	public static String REQUEST_HOSTS_TO_DOWNLOAD_FROM(Namespace namespace){
		return DOWNLOAD_RESOURCE.concat(Command_Params_Separator).concat(namespace.toString());
	}
	
	
	
	public static String CREATE_FILE_AND_GIVE_HOSTS_AND_ID(Namespace namespace,long fileSize){
		return CREATE_AND_GIVE_HOSTS_AND_ID.concat(Command_Params_Separator).concat(namespace.toString()).concat(Params_Separator).concat(Long.toString(fileSize));
	}
	public static String CLUSTER_HOSTS_TO_APPEND_TO_BLOCK_HOST_REQUEST(Vector<InetSocketAddress> hosts){
		return Tools.serializeVectorAddress(hosts);
	}


	public static Vector<InetSocketAddress> getClusterHostsFromBlockHostRequest(String request){
		return getVectorHostsFromReply(request.substring(0, request.indexOf(clusterHostsFileServerRequestSeparator)));
	}
	public static String extractRequestAloneFromHostRequest(String request){
		return request.substring(request.indexOf(clusterHostsFileServerRequestSeparator)+clusterHostsFileServerRequestSeparator.length());
	}
	
	/**
	 * 
	 * @param dfsFileId
	 * @param hosts
	 * @return
	 */
	
	public static String PROVIDE_FILE_ID_AND_REPLICATING_HOSTS(String dfsFileId,Vector<Vector<InetSocketAddress>> hosts){;
		return dfsFileId.concat(" ").concat(Tools.serializeVectorVectorAddress(hosts));
	}
	public static Vector<Vector<InetSocketAddress>> extractAddressesForDownloadFromReply(String str){
		String hostStr=str.substring(str.indexOf(" ")+1);
		return Tools.getVectorVectorInetSocketAddress(hostStr);
	}
	public static String extractDFSFileIdForDownloadFromReply(String str){
		return str.substring(0, str.indexOf(" "));
	}
	
	
	/**
	 * For use by FileHost
	 * @param dfsFileId
	 * @param blockId
	 * @param chunkIndex
	 * @return
	 */
	public static String INFORMATION_TO_NAMESERVER_OF_STORED_BLOCK(String dfsFileId,BlockId blockId,int chunkIndex,long blockSize){
		return  LOADED_BLOCK.concat(Command_Params_Separator).concat(Tools.generateAddress(Config.fileHostListenerPort).toString()).concat(Params_Separator).concat(dfsFileId).concat(Params_Separator).concat(blockId.toString()).concat(Params_Separator).concat(Integer.toString(chunkIndex)).concat(Params_Separator).concat(Long.toString(blockSize));
	}
	
	public static String COMMAND_TO_CREATE_NEW_CLUSTER(String rootNamespace,String serializedDirectory,long blockSize){
		return START_CLUSTER.concat(Command_Params_Separator).concat(rootNamespace).concat(Params_Separator).concat(serializedDirectory).concat(Params_Separator).concat(Long.toString(blockSize));
	}
	public static String INFORM_CLIENT_OF_REMAPPED_NAMESPACE(Vector<InetSocketAddress> clusterHosts){
		return REMAP_NAMESPACE.concat(" ").concat(Tools.serializeVectorAddress(clusterHosts));
	}
	public static Vector<InetSocketAddress> getNewClusterHostsFromRemapCommand(String str){
		return Tools.getVectorInetSocketAddress(str.substring(str.indexOf(" ")+1));
	}

	/*
	public static String getDfsFileIdFromBlockHostMessage(String message){
		return message.substring(0, message.indexOf("|"));
	}
	public static BlockId getBlockIdFromBlockHostMessage(String message){
		return new BlockId(message.substring(message.indexOf('!')+1, message.lastIndexOf('!')));
	}
	public static int getInodeIndexFromBlockHostMessage(String message){
		return Integer.parseInt(message.substring(message.lastIndexOf('!')+1));
	}
	*/
	
	public static String serializeFileIdAndVectorVectorHosts(String dfsFileId,Vector<Vector<InetSocketAddress>> hosts){
		return dfsFileId.concat("|").concat(Tools.serializeVectorVectorAddress(hosts));
	}
	public static String serializeFileIdAndVectorHosts(String dfsFileId,Vector<InetSocketAddress> hosts){
		return dfsFileId.concat("|").concat(Tools.serializeVectorAddress(hosts));
	}
	public static Vector<Vector<InetSocketAddress>> getVectorVectorHostsFromReply(String reply){
		return Tools.getVectorVectorInetSocketAddress(reply.substring(reply.indexOf("|")+1));
	}
	public static Vector<InetSocketAddress> getVectorHostsFromReply(String reply){
		return Tools.getVectorInetSocketAddress(reply.substring(reply.indexOf("|")+1));
	}
	
	public static String getDFSFileIdFromReply(String reply){
		return reply.substring(0, reply.indexOf("|"));
	}
	public static String fileManagerAddress(){
		return Tools.generateAddress(Config.fileDownloaderPort).toString().concat(fromAddressMessageSeparator);
	}
	public static String extractCommand(String request){
		return request.substring(0, request.indexOf(Command_Params_Separator));
	}
	public static String extractParams(String request){
		return request.substring(request.indexOf(Command_Params_Separator)+1);
	}
	public static StringTokenizer paramsTokenizer(String params){
		return new StringTokenizer(params,Params_Separator);
	}

}
