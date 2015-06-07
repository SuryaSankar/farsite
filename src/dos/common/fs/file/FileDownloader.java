package dos.common.fs.file;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.util.*;
import dos.common.util.*;

import dos.common.client.*;
import dos.common.util.TextFileHandle;
public class FileDownloader {
	InetSocketAddress fromAddress;
	FileReceiver fileReceiver;
	String FileStoragePath;
	long blockSize;
	String customHeader="";
	public FileDownloader(int port,String FileStoragePath,long blockSize) throws IOException{
		this.fromAddress=Tools.generateAddress(port);
		this.FileStoragePath=FileStoragePath;
		this.blockSize=blockSize;
		fileReceiver=new FileReceiver(port);
	}
	public Envelope requestForBlock(String fileName,int blockId){
		String message= customHeader.concat(FileTransferParams.SEND_BLOCK).concat(FileTransferParams.requestFileNameDelimiter).concat(fileName).concat(FileTransferParams.fileNameBlockIdDelimiter).concat(Integer.toString(blockId));
		return new Envelope(this.fromAddress,message);
	}
	public void addCustomHeaderToSendBlockRequest(String header){
		customHeader=header;
	}

	public long downloadFileFromHosts(String fileName,Vector<InetSocketAddress> fileHosts,String localNameForFile) throws Exception{
			String file=FileStoragePath.concat(localNameForFile);
			FileChannel localFileChannel=new FileOutputStream(file).getChannel();
			long offset=0;
			int blockId=1;
			for(InetSocketAddress fileHost:fileHosts){
				String hostStatus=CommunicationTools.queryAndBlockForReply(fileHost, requestForBlock(fileName, blockId).toString());
				if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK)){
					blockId++;
					fileReceiver.receiveFilePart(localFileChannel, offset, blockSize);
					offset=TextFileHandle.size(file);
				}
				else if(hostStatus.equals(FileTransferParams.REFUSAL_TO_SEND_BLOCK))
					throw new Exception("BLOCK TRANSFER ACTIVELY REFUSED");
			}
			return offset;
	}
	public static void main(String args[]){
		String c="";
		Tools.print(c.concat("hi"));
		String str="fg";
		String b=c.concat(str);
		if(b.equals(str))
			Tools.print("yes");
		
	}
}
