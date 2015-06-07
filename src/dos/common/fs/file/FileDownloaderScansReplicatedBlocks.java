package dos.common.fs.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.util.*;

import com.sun.org.apache.bcel.internal.generic.NEW;

import dos.common.client.CommunicationTools;
import dos.common.util.*;

public class FileDownloaderScansReplicatedBlocks {

	InetSocketAddress fromAddress;
	FileReceiver fileReceiver;
	long blockSize;
	int noOfSimultaneousDownloads=1;
	int downloadPort;
	Hashtable<Integer,Integer> runningDownloads=new Hashtable<Integer, Integer>();
	public FileDownloaderScansReplicatedBlocks(int FileDownloaderPort,long blockSize,int noOfSimultaneousDownloads) throws IOException{
		this.fromAddress=Tools.generateAddress(FileDownloaderPort);
		this.blockSize=blockSize;
		this.downloadPort=FileDownloaderPort;
		//fileReceiver=new FileReceiver(FileDownloaderPort);
		this.noOfSimultaneousDownloads=noOfSimultaneousDownloads;
		for(int i=0;i<noOfSimultaneousDownloads;i++)
			runningDownloads.put(downloadPort+i, 0);
	}
	public FileDownloaderScansReplicatedBlocks(int FileDownloaderPort,long blockSize) throws IOException{
		this.fromAddress=Tools.generateAddress(FileDownloaderPort);
		this.blockSize=blockSize;
		this.downloadPort=FileDownloaderPort;
		fileReceiver=new FileReceiver(FileDownloaderPort);
	}
	public Envelope requestForBlock(String fileName,int blockId){
		String message= FileTransferParams.SEND_BLOCK.concat(FileTransferParams.requestFileNameDelimiter).concat(fileName).concat(FileTransferParams.fileNameBlockIdDelimiter).concat(Integer.toString(blockId));
		return new Envelope(this.fromAddress,message);
	}
	public Envelope requestForBlock(String fileName,int blockId,int downloadPort){
		String message= FileTransferParams.SEND_BLOCK.concat(FileTransferParams.requestFileNameDelimiter).concat(fileName).concat(FileTransferParams.fileNameBlockIdDelimiter).concat(Integer.toString(blockId));
		return new Envelope(Tools.generateAddress(downloadPort),message);
	}
	public void setNoOfSimultaneousDownloads(int simultaneousPorts){
		noOfSimultaneousDownloads=simultaneousPorts;
	}
	public synchronized void setRunningDownload(int port){
		runningDownloads.put(port, 1);
	}
	public synchronized void unsetRunningDownload(int port){
		runningDownloads.put(port, 0);
	}
	/*
	public long downloadFileFromHosts(String dfsFileName,Vector< Vector<InetSocketAddress> > hosts,String localFilePath) throws Exception {
		long offset=0;
		int blockIndex=1;
		int currentPort=downloadPort;
		FileChannel localFileChannel=new FileOutputStream(localFilePath).getChannel();

		for(Vector<InetSocketAddress> blockhosts:hosts){
			Enumeration<Integer> ports=runningDownloads.keys();
			for(int i=0;i<noOfSimultaneousDownloads;i++){
				if(runningDownloads.get(downloadPort+i)==1){
					Tools.print(downloadPort+i+" running");
					continue;
				}
				else{
					setRunningDownload(downloadPort+i);
					currentPort=downloadPort+i;
					break;
				}
			}
			String hostStatus=CommunicationTools.tryToGetReplyFromOne(blockhosts, requestForBlock(dfsFileName, blockIndex,currentPort).toString());
			if(hostStatus.equals(FileTransferParams.REFUSAL_TO_SEND_BLOCK)){
				continue;
			}
			else if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK)){
					new BlockDownloader(localFileChannel,offset,blockSize,currentPort,this).start();
					currentPort++;
					blockIndex++;
					//offset=TextFileHandle.size(localFilePath);
					offset+=blockSize;
					Tools.print("changed offset to "+offset);

			}
		}
		return offset;
	}
	
	static class BlockDownloader extends Thread{
		FileChannel localFileChannel;
		long offset, blockSize;
		int downloadPort;
		FileDownloaderScansReplicatedBlocks caller;
		BlockDownloader(FileChannel localFile,long offset,long blockSize,int downloadPort,FileDownloaderScansReplicatedBlocks caller){
			this.localFileChannel=localFile;
			this.offset=offset;
			this.blockSize=blockSize;
			this.downloadPort=downloadPort;
			this.caller=caller;
		}
		public void run(){
			try {
				FileReceiver fileReceiver=new FileReceiver(downloadPort);
				Tools.print("downloaded "+fileReceiver.receiveFilePart(localFileChannel, offset, blockSize));
				fileReceiver.close();
				caller.unsetRunningDownload(downloadPort);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
*/

 //Sequential download. Downloads blocks one by one. Very slow
	public long downloadFileFromHosts(String dfsFileName,Vector< Vector<InetSocketAddress> > hosts,String localFilePath) throws Exception {
		FileChannel localFileChannel=new FileOutputStream(localFilePath).getChannel();
		long offset=0;
		int blockIndex=1;
		for(Vector<InetSocketAddress> blockhosts:hosts){
			String hostStatus=CommunicationTools.tryToGetReplyFromOne(blockhosts, requestForBlock(dfsFileName, blockIndex).toString());
			if(hostStatus.equals(FileTransferParams.REFUSAL_TO_SEND_BLOCK)){
				continue;
			}
			else if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK)){
				try {
					fileReceiver.receiveFilePart(localFileChannel, offset, blockSize);
					blockIndex++;
					offset=TextFileHandle.size(localFilePath);
				} catch (IOException e) {
					continue;
				}
			}
		}
		return offset;
	}
	
	/*
	public long downloadFileFromHosts(Vector< Vector<BlockHandle> > fileblocks,String localFilePath) throws Exception {
		FileChannel localFileChannel=new FileOutputStream(localFilePath).getChannel();
		long offset=0;
		int blockIndex=1;
		for(Vector<BlockHandle> blocks:fileblocks){
			Vector<InetSocketAddress> blockhosts=new Vector<InetSocketAddress>();
			for(BlockHandle block:blocks)
				blockhosts.add(block.address());
			Envelope envelope=CommunicationTools.tryToGetEnvelopeFromOne(blockhosts, requestForBlock(dfsFileName, blockIndex).toString());
			InetSocketAddress blockHostToContact=envelope.fromAddress();
			String hostStatus=envelope.message();
			if(hostStatus.equals(FileTransferParams.REFUSAL_TO_SEND_BLOCK)){
				blockhosts.remove(blockHostToContact);
				continue;
			}
			else if(hostStatus.equals(FileTransferParams.ACCEPTANCE_TO_SEND_BLOCK)){
				try {
					fileReceiver.receiveFilePart(localFileChannel, offset, blockSize);
					blockIndex++;
					offset=TextFileHandle.size(localFilePath);
				} catch (IOException e) {
					blockhosts.remove(blockHostToContact);
					continue;
				}
			}
		}
		return offset;
	}
*/
	public static void main(String args[]){

	}


}
