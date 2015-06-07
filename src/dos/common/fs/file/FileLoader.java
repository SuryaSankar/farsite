package dos.common.fs.file;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import dos.common.util.*;
import dos.common.client.*;

public class FileLoader {
	long blockSize=8388608;
	int blockServerListeningPort=0;
	int blockServerTransferPort=0;
	InetSocketAddress fromAddress;
	String customHeader="";
	
	public FileLoader(int blockServerListeningPort,int blockServerTransferPort){
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
	public void addCustomHeaderToBlockHostRequest(String header){
		customHeader=header;
	}
	public Envelope requestToHostBlock(String fileName,int blockId){
		String message =customHeader.concat(FileTransferParams.HOST_BLOCK).concat(FileTransferParams.requestFileNameDelimiter).concat(fileName).concat(FileTransferParams.fileNameBlockIdDelimiter).concat(Integer.toString(blockId));
		return new Envelope(this.fromAddress,message);
	}
	public  void loadFile(String filePath,Vector<InetSocketAddress> hosts,String dfsFileId) throws ConnectionPendingException, TimeoutException, IOException, InterruptedException{
		Tools.print("Inside loadFile");
		int blockId=1;
		long offset=0;
		for(InetSocketAddress host:hosts){
			InetSocketAddress host1=Tools.toInetSocketAddress(host.toString());
			String hostReply=CommunicationTools.queryAndBlockForReply(host1, requestToHostBlock(dfsFileId, blockId).toString());
			String hostStatus=Envelope.extractMessage(hostReply);
			if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_HOST_BLOCK)){
				InetSocketAddress blockTransferAddress=new InetSocketAddress(host1.getHostName(),Envelope.extractPortNoFromMessage(hostReply));
				FileSender.sendFilePart(filePath, blockTransferAddress, offset, blockSize);
				offset+=blockSize;
				System.out.println("loading offset "+offset+" chunk is "+blockId);
				blockId++;
			}
			else if(hostStatus.equals(FileTransferParams.REFUSAL_TO_HOST_BLOCK))
				throw new ConnectionPendingException();
		}
	}
	
	
	
}
