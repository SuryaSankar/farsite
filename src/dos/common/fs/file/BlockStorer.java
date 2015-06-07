package dos.common.fs.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;

import dos.common.util.*;

public class BlockStorer {
	String blockStoragePath;
	long blockSize;
	FileReceiver blockReceiver;
	public BlockStorer(int port,String blockStoragePath,long blockSize) throws IOException{
		this.blockStoragePath=blockStoragePath;
		this.blockSize=blockSize;
		blockReceiver=new FileReceiver(port);
	}
	public String validHostRequest(){
		return FileTransferParams.HOST_BLOCK;
	}



	
	public long storeBlock(BlockId blockId,long blockSize) throws IOException{
		String file=blockStoragePath.concat(blockId.toString());
		FileChannel localFileChannel=new FileOutputStream(file).getChannel();
		Tools.print("opened filechannel in storeBlock to store "+blockId);
		long transferredBytes=0;
		try {
			transferredBytes = blockReceiver.receiveFilePart(localFileChannel, 0,blockSize);
		} catch (Exception e) {
			System.out.println("Caught exception in storeBlock");
	e.printStackTrace();
		}
		System.out.println("stored"+blockId);
		blockReceiver.close();
		System.out.println("closed"+blockId);
		return transferredBytes;
	}
	
}
