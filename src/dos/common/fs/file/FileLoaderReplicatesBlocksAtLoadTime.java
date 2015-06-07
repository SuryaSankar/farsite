package dos.common.fs.file;

import java.net.InetSocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.Hashtable;
import java.util.Vector;

import dos.common.client.CommunicationTools;
import dos.common.fs.FSParams;
import dos.common.util.*;
/**
 * 
 * This will send each file chunk to multiple hosts during load time itself. ie
 * replication is handled by the client instead of a background process on
 * nameserver. 
 *
 */
public class FileLoaderReplicatesBlocksAtLoadTime {
	long blockSize=8388608;
	int blockServerListeningPort=0;
	int blockServerTransferPort=0;
	InetSocketAddress fromAddress;
	String customFooter="";
String customFooterDelim=FileTransferParams.fileLoaderCustomFooterDelim;
	public FileLoaderReplicatesBlocksAtLoadTime(int blockServerListeningPort,int blockServerTransferPort){
		this.blockServerListeningPort=blockServerListeningPort;
		this.blockServerTransferPort=blockServerTransferPort;
		fromAddress=Tools.generateAddress(0);
	}
	
	public void setBlockSize(long blocksize){
		blockSize=blocksize;
	}
	public void setBlockServerListeningPort(int port){
		this.blockServerListeningPort=port;
	}
	public void setBlockServerTransferPort(int port){
		this.blockServerTransferPort=port;
	}
	public void addCustomFooterToBlockHostRequest(String footer){
		customFooter=footer;
	}
	public Envelope requestToHostBlock(String fileName,int blockId,long blockSize){
		String message =FileTransferParams.HOST_BLOCK.concat(FileTransferParams.requestFileNameDelimiter).concat(fileName).concat(FileTransferParams.fileNameBlockIdDelimiter).concat(Integer.toString(blockId).concat(FileTransferParams.blockIdblockSizeDelimiter).concat(Long.toString(blockSize))).concat(customFooterDelim).concat(customFooter);
		return new Envelope(this.fromAddress,message);
	}
	public static class SimultaneousLoader extends CommunicationTools.CallableQueriesEachParticipantUniquelyAndMakesDecison{
		public SimultaneousLoader(Hashtable<InetSocketAddress,String> serversNQueries) {
			super(serversNQueries);
		}
		public void handleReply(InetSocketAddress server,String reply){
			
		}
		public void evaluateRepliesAndMakeDecision(){
			
		}
	}
	
	public  void loadFile(String filePath,Vector< Vector<InetSocketAddress> > hosts,String dfsFileId) throws Exception{
		int noOfReplicas=hosts.elementAt(0).size();
		Tools.print("Entered loadFile");
		Tools.print(noOfReplicas);
		for(int i=0;i<noOfReplicas;i++){
			int blockId=1;
			long offset=0;
			for(Vector<InetSocketAddress> blockhosts:hosts){
				
				Tools.print(Tools.serializeVectorAddress(blockhosts));
				InetSocketAddress host1=Tools.toInetSocketAddress(blockhosts.elementAt(i).toString());
				long bs;
				if(blockId<hosts.size()) {
					System.out.println(blockId+ " is < than "+hosts.size());
					bs=blockSize;
				}
				else{
					bs=(Tools.fileSize(filePath)-offset);
					System.out.println(blockId+ " is > than "+hosts.size());
					
				}
				System.out.println("Telling block size as "+bs);
				String hostReply=CommunicationTools.queryAndBlockForReply(host1, requestToHostBlock(dfsFileId, blockId,bs).toString());
				String hostStatus=Envelope.extractMessage(hostReply);
				if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_HOST_BLOCK)){
					String hostName=host1.getHostName();
					InetSocketAddress blockTransferAddress=new InetSocketAddress(hostName,Envelope.extractPortNoFromMessage(hostReply));
					System.out.println("sending "+filePath+" from "+offset+" for "+bs+"to "+blockTransferAddress.toString());
					FileSender.sendFilePart(filePath, blockTransferAddress, offset, bs);
					offset+=blockSize;
					System.out.println("loading offset "+offset+" chunk is "+blockId);
					blockId++;
				}
				else if(hostStatus.equals(FileTransferParams.REFUSAL_TO_HOST_BLOCK)){
					Tools.print("hosting refused for "+offset);
					throw new ConnectionPendingException();
				}
			}
		}
			
		}
		
	/*
	
	public  void loadFile(String filePath,Vector< Vector<InetSocketAddress> > hosts,String dfsFileId) throws Exception{
		int blockId=1;
		String fileName=Tools.baseName(filePath);
		long offset=0;
		for(Vector<InetSocketAddress> blockhosts:hosts){
			Hashtable<InetSocketAddress, String> replies=CommunicationTools.getRepliesOneByOne(blockhosts, requestToHostBlock(fileName, blockId).toString());
			int failedBlockReplicas=0;
			for (InetSocketAddress blockHost:blockhosts){
				if(replies.containsKey(blockHost)){
					String hostStatus=replies.get(blockHost);
					if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_HOST_BLOCK)){
						FileSender.sendFilePart(filePath, blockHost, offset, blockSize);
						offset+=blockSize;
						break;
					}
					else if(hostStatus.equals(FileTransferParams.REFUSAL_TO_HOST_BLOCK))
						failedBlockReplicas++;
				}
				else
					failedBlockReplicas++;
			}
			if(failedBlockReplicas==blockhosts.size())
				throw new Exception("BLOCK HOSTING ACTIVELY REFUSED");
		}
	}
	*/
}
