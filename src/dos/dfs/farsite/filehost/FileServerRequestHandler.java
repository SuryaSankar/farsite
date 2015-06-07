package dos.dfs.farsite.filehost;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

import dos.common.client.CommunicationTools;
import dos.common.fs.file.BlockId;
import dos.common.fs.file.BlockRequestHandler;
import dos.common.util.Envelope;
import dos.common.util.Tools;
import dos.dfs.farsite.Config;
import dos.dfs.farsite.Messages;

public class FileServerRequestHandler extends BlockRequestHandler {
	public FileServerRequestHandler(int defaultFileTransferPort,String blockStorageSpace,long blockSize,Socket connection) throws InterruptedException{
		super(defaultFileTransferPort,blockStorageSpace,blockSize,Config._NO_OF_SIMULTANEOUS_BLOCK_ACCEPTANCES_,connection);
	}
	InetSocketAddress requestor;
	String request;
	String dfsFileName;
	int blockIndex;
	BlockId blockId;
	Vector<InetSocketAddress> nameserversToReportTo;
	long blockSize;
	public String replyToRequest(String serializedEnvelope){
		Tools.print(serializedEnvelope);
		requestor=Envelope.extractAddress(serializedEnvelope);
		request=Envelope.extractMessage(serializedEnvelope);;
		Tools.print(extractRequestHeader(request));
		String reply="";
		if(isValidHostRequest(request)){
			Tools.print("Is valid host request");
			nameserversToReportTo=Tools.getVectorInetSocketAddress(extractCustomFooter(request));
			for(InetSocketAddress server:nameserversToReportTo)
				Tools.print(server);
			dfsFileName=extractDFSFileIdFromHostRequest(request);
			blockIndex=extractBlockIdFromHostRequest(request);
			blockSize=extractBlockSizeFromHostRequest(request);
			blockId=BlockId.generateBlockIdentifier(dfsFileName, blockIndex);
			System.out.println("dfsFile "+dfsFileName+" blockIndex "+blockIndex+" blockSize "+blockSize);

			try {
				Tools.print(request);
				reply=acceptToHostIfPortAvailable();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				reply=refuseToHost();
			}
			System.out.println("replying with "+reply);
		}
		else if(isValidSendRequest(request)){
			
			
			Tools.print("isValidSendRequest");
			reply=acceptToSend();
		}
		else{
			
		}
		Tools.print(reply);
		return reply;
	}
	public void postReplyAction(){
		Tools.print("post reply");
		if(isValidHostRequest(request)){
			try {
				
				Tools.print(blockId.toString());
				long tb=storeBlock(blockId,blockSize);
				Tools.print(tb);
				System.out.println("Informing ns with "+Messages.INFORMATION_TO_NAMESERVER_OF_STORED_BLOCK(dfsFileName,blockId,blockIndex,blockSize));
				CommunicationTools.tryToInformOne(nameserversToReportTo, Messages.INFORMATION_TO_NAMESERVER_OF_STORED_BLOCK(dfsFileName,blockId,blockIndex,blockSize));
			} catch (IOException e) {
				Tools.print("Caught exception while storing");
				Tools.print(e);
				e.printStackTrace();
			}
		}
		else if(isValidSendRequest(request)){
			try {
				Tools.print("Inside sending area");
				dfsFileName=extractDFSFileIdFromSendRequest(request);
				
				blockIndex=extractBlockIndexFromSendRequest(request);
				blockId=BlockId.generateBlockIdentifier(dfsFileName, blockIndex);
				System.out.println("dfsFile "+dfsFileName+" blockIndex "+blockIndex+" blockSize "+blockId);
				System.out.println("trying to send "+blockId.toString()+" to "+requestor.toString());
				retrieveBlockAndSend(blockId, requestor);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public void postStorageAction(String dfsFileName,BlockId blockId,int blockIndex){
		System.out.println("stored"+blockId.toString()+blockIndex);
	}

}
