package dos.common.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class FileReceiver {
	   ServerSocketChannel receiver;
	   FileChannel localFileChannel;
	   long fileChannelSize;
	   SocketChannel binaryIn;
	   public FileReceiver(int port) throws IOException{
				   receiver=ServerSocketChannel.open();
				   receiver.socket().bind(Tools.generateAddress(port));
	   }
	   
	   public long receiveFilePart(FileChannel fileChannel,long offset,long count) throws IOException{
		   binaryIn=receiver.accept();
		   Tools.print("got a new file connection");
		   return fileChannel.transferFrom(binaryIn, offset, count);
	   }
	   public void close() throws IOException{
		   receiver.close();
	   }
	   
	   public static void main(String args[]) throws IOException{
		   
	   }
}

