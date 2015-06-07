package dos.dfs.farsite.filehost;

import java.net.InetSocketAddress;

import dos.common.util.Tools;
import dos.dfs.farsite.Config;

public class FileHost  {

	static InetSocketAddress address=Tools.generateAddress(Config.fileHostPort);
	static String blockStorageSpace=Config._FARSITE_FILE_HOST_STORAGE_;
	static long blockSize=Config.blockSize;
	static int fileHostListenerPort=Config.fileHostListenerPort;
	static FileServer fileServer;
	static int defaultFileTransferPort=Config.fileHostPort;

	public static void start(){
		try {
			fileServer=new FileServer(defaultFileTransferPort,blockStorageSpace,blockSize,fileHostListenerPort);
		} catch (Exception e) {
			Tools.print("Caught Exception while starting fileServer");
			e.printStackTrace();
		}
	}
	
}
