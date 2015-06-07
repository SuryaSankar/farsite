package dos.common.fs.file;

import dos.common.Parameters;
import dos.common.server.ReplyingServerRequestHandler;
import dos.common.util.Envelope;
import dos.common.util.Tools;

import java.net.*;
import java.io.IOException;


public abstract class BlockRequestHandler extends ReplyingServerRequestHandler {
	BlockStorer blockStorer;
	BlockRetriever blockRetriever;
    int simultaneity;
	int timeToWaitForBlockTransfer=500;
	int lowestBlockStoringPort;
	int highestBlockStoringPort;
	String blockStorageSpace;
	long blockSize;
	
	public BlockRequestHandler(int defaultBlockStoringPort,String blockStorageSpace,long blockSize,int simultaneity,Socket connection) throws InterruptedException{
		super(connection);
		this.blockStorageSpace=blockStorageSpace;
		this.lowestBlockStoringPort=defaultBlockStoringPort;
		this.blockSize=blockSize;
		this.simultaneity=simultaneity;
		this.highestBlockStoringPort=lowestBlockStoringPort+simultaneity-1;
		blockRetriever=new BlockRetriever(blockStorageSpace);
		}
	public String extractDFSFileIdFromSendRequest(String request){
		return request.substring(request.indexOf(FileTransferParams.requestFileNameDelimiter)+FileTransferParams.requestFileNameDelimiter.length(),request.indexOf(FileTransferParams.fileNameBlockIdDelimiter));
	}
	public int extractBlockIndexFromSendRequest(String request){
		return Integer.parseInt(request.substring(request.indexOf(FileTransferParams.fileNameBlockIdDelimiter)+FileTransferParams.fileNameBlockIdDelimiter.length()));
	}
	public String extractDFSFileIdFromHostRequest(String request){
		return request.substring(request.indexOf(FileTransferParams.requestFileNameDelimiter)+FileTransferParams.requestFileNameDelimiter.length(),request.indexOf(FileTransferParams.fileNameBlockIdDelimiter));
	}
	public int extractBlockIdFromHostRequest(String request){
		return Integer.parseInt(request.substring(request.indexOf(FileTransferParams.fileNameBlockIdDelimiter)+FileTransferParams.fileNameBlockIdDelimiter.length(),request.indexOf(FileTransferParams.blockIdblockSizeDelimiter)));
	}
	public long extractBlockSizeFromHostRequest(String request){
		return Long.parseLong(request.substring(request.indexOf(FileTransferParams.blockIdblockSizeDelimiter)+1,request.indexOf(FileTransferParams.fileLoaderCustomFooterDelim)));
	}
	public String extractRequestHeader(String request){
		return request.substring(0, request.indexOf(FileTransferParams.requestFileNameDelimiter));
	}
	public String extractCustomFooter(String request){
		return request.substring(request.indexOf(FileTransferParams.fileLoaderCustomFooterDelim)+1);
	}
	public boolean isValidHostRequest(String request){
		String requestHeader=request.substring(0, request.indexOf(FileTransferParams.requestFileNameDelimiter));
		return requestHeader.equals(FileTransferParams.HOST_BLOCK);
	}
	public boolean isValidSendRequest(String request){
		String requestHeader=request.substring(0, request.indexOf(FileTransferParams.requestFileNameDelimiter));
		System.out.println("Got "+requestHeader);
		System.out.println("Expected "+FileTransferParams.SEND_BLOCK);
		return requestHeader.equals(FileTransferParams.SEND_BLOCK);
	}
	public String acceptToHost(){
		return FileTransferParams.ACCEPTANCE_TO_HOST_BLOCK;
	}
	public String refuseToHost(){
		return FileTransferParams.REFUSAL_TO_HOST_BLOCK;
	}
	public String acceptToHostIfPortAvailable() throws InterruptedException{
		int blockStoringPort=lowestBlockStoringPort;
		while(blockStoringPort<=highestBlockStoringPort)
			try {
				blockStorer=new BlockStorer(blockStoringPort,blockStorageSpace,blockSize);
				return Envelope.concatPortNoToMessage(blockStoringPort, acceptToHost());
			} catch (IOException e) {
				Tools.print("caught IO Exception in BlockRequestHandler");
				Tools.print(e);
				blockStoringPort++;
			}
		return refuseToHost();
	}
	
	public String acceptToSend(){
		return FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK;
	}
	public  String refuseToSend(){
		return FileTransferParams.REFUSAL_TO_SEND_BLOCK;
	}
	public void setSimultaneity(int simultaneity){
		this.simultaneity=simultaneity;
	}
	
	public long storeBlock(BlockId blockId,long blockSize) throws IOException{
		return blockStorer.storeBlock(blockId,blockSize);
	}
	public void retrieveBlockAndSend(BlockId blockId,InetSocketAddress requestor) throws IOException{
		blockRetriever.retrieveBlockAndSend(blockId, requestor);
	}



		
}
