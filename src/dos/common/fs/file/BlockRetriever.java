package dos.common.fs.file;

import java.io.IOException;
import java.net.InetSocketAddress;
import dos.common.util.*;
public class BlockRetriever {
	String blockStoragePath;
	
	public BlockRetriever(String blockStoragePath) {
		this.blockStoragePath=blockStoragePath;
	}
	public String validSendRequest(){
		return FileTransferParams.SEND_BLOCK;
	}
	public boolean isValidSendRequest(String request){
		String requestHeader=request.substring(0, request.indexOf(FileTransferParams.requestFileNameDelimiter));
		return requestHeader.equals(FileTransferParams.SEND_BLOCK);
	}
	public void retrieveBlockAndSend(BlockId blockId,InetSocketAddress destination) throws IOException{
		FileSender.sendFile(blockStoragePath.concat(blockId.toString()), destination);
	}
	public String acceptToSend(){
		return FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK;
	}
	public  String refuseToSend(){
		return FileTransferParams.REFUSAL_TO_SEND_BLOCK;
	}
	
	public static void main(String args[]){

	}
}
