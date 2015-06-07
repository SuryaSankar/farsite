package dos.dfs.farsite.filehost;

import java.io.IOException;
import java.net.*;

import dos.common.fs.file.*;
import dos.common.util.Tools;
import dos.dfs.farsite.Config;

public class FileServer extends BlockServer {
	InetSocketAddress myAddress=Tools.generateAddress(Config.fileHostListenerPort);
	int defaultFileTransferPort;
	String blockStorageSpace;
	long blockSize;
	public FileServer(int defaultFileTransferPort,String blockStorageSpace,long blockSize,int requestListeningPort) throws IOException{
		super (requestListeningPort);
		this.defaultFileTransferPort=defaultFileTransferPort;
		this.blockSize=blockSize;
		this.blockStorageSpace=blockStorageSpace;
		try {
			super.boot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public FileServerRequestHandler provideRequestHandler(Socket connection) throws IOException, InterruptedException{
		return new FileServerRequestHandler(defaultFileTransferPort,blockStorageSpace,blockSize,connection);
	}
	

}
