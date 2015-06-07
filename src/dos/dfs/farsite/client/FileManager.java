package dos.dfs.farsite.client;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import dos.common.fs.file.*;
import dos.common.util.*;
import dos.dfs.farsite.Config;
import dos.dfs.farsite.Messages;

public class FileManager {
	FileDownloaderScansReplicatedBlocks fileDownloader;
	FileLoaderReplicatesBlocksAtLoadTime  fileLoader;
	String FileStoragePath;
	public FileManager(int fileHostListenerPort,int fileHostPort,int fileDownloaderPort,String FileStoragePath,long blockSize) {
		try {
			fileDownloader=new FileDownloaderScansReplicatedBlocks(fileDownloaderPort,blockSize);
			fileLoader = new FileLoaderReplicatesBlocksAtLoadTime(fileHostListenerPort,fileHostPort);
			fileLoader.setBlockSize(blockSize);
			this.FileStoragePath=FileStoragePath;
		} catch (IOException e) {
			Tools.print("Caught Exception while instantiating FileManager.fileDownloader ");
			e.printStackTrace();
		}
	}
/*
	public void load(String filePath, Vector<InetSocketAddress> fileHosts, String nameToStoreAs,Vector<InetSocketAddress> nameServers) throws InterruptedException{
		
			fileLoader.addCustomHeaderToBlockHostRequest(Messages.CLUSTER_HOSTS_TO_APPEND_TO_BLOCK_HOST_REQUEST(nameServers));
			while(true)
			try {
				fileLoader.loadFile(filePath, fileHosts,nameToStoreAs);
				break;
			} catch (ConnectionPendingException e) {
				Tools.print("BlockHosting refused. Trying again");
				Thread.sleep(1);
			} catch (TimeoutException e) {
				Tools.print("Could not contact block server. Trying again");
				break;
			} catch (IOException e) {
				break;
			} catch (InterruptedException e) {
				break;
			}
	
	}*/
	public void loadWithReplication(String filePath, Vector<Vector<InetSocketAddress> > fileHosts, String nameToStoreAs,Vector<InetSocketAddress> nameServers) throws InterruptedException{
		
		fileLoader.addCustomFooterToBlockHostRequest(Tools.serializeVectorAddress(nameServers));
		while(true)
		try {
			fileLoader.loadFile(filePath, fileHosts,nameToStoreAs);
			break;
		} catch (ConnectionPendingException e) {
			Tools.print("BlockHosting refused. Trying again");
			Thread.sleep(1);
		} catch (TimeoutException e) {
			Tools.print("Could not contact block server. Trying again");
			break;
		} catch (IOException e) {
			break;
		} catch (InterruptedException e) {
			break;
		} catch (Exception e) {
			Tools.print("Unknown exception at FileManager.load "+e);
			e.printStackTrace();
		}

}
	
	public void download(String dfsFileName,Vector<Vector<InetSocketAddress>> hosts,String localFilePath){
		try {
			if(localFilePath.equals(null))
				localFilePath=this.FileStoragePath.concat(dfsFileName);
			fileDownloader.downloadFileFromHosts(dfsFileName, hosts,localFilePath);
		} catch (Exception e) {
			Tools.print("Caught exception FileManager.download()");
			Tools.print(e);
			e.printStackTrace();
		}
	}
	
	/*
	public void download(Vector<Vector<BlockHandle>> blocks,String localFilePath){
		try {

			fileDownloader.downloadFileFromHosts(blocks,localFilePath);
		} catch (Exception e) {
			Tools.print("Caught exception FileManager.download()");
			Tools.print(e);
			e.printStackTrace();
		}
	}
	*/
	

	

}
