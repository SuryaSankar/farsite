package dos.common.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import dos.common.util.Tools;

public class FileSender {
	static SocketChannel writeStream;
	public static void sendFilePart(String localFilePath,InetSocketAddress destination,long offset,long count) throws IOException{
		FileChannel fileChannel=new FileInputStream(localFilePath).getChannel();
		sendFilePart(fileChannel, destination, offset, count);
		fileChannel.close();
	}
	public static void sendFilePart(FileChannel fileChannel,InetSocketAddress destination,long offset,long count) throws IOException{
		writeStream=SocketChannel.open();
		try {
			writeStream.connect(destination);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tools.print("starting transfer from "+offset+" for "+count);
		fileChannel.transferTo(offset, count, writeStream);
		Tools.print("Transfer over");
		writeStream.close();
	}
	public static void sendFile(String localFilePath,InetSocketAddress destination)throws IOException{
			writeStream=SocketChannel.open();
			FileChannel fileChannel=new FileInputStream(localFilePath).getChannel();
			writeStream.connect(destination);
			Tools.print("Starting transfer");
			fileChannel.transferTo(0, fileChannel.size(), writeStream);
			Tools.print("Transfer over");
			writeStream.close();
			fileChannel.close();
	}
	
	public static void main(String args[]) throws IOException, InterruptedException{
		InetSocketAddress dest=Tools.toInetSocketAddress("10.6.9.18:11000");
		//sendFile(args[0], dest);
		
		sendFilePart(args[0], dest, 0, 10000);
		sendFilePart(args[0], dest, 10000, 10000);
		sendFilePart(args[0], dest, 20000, 10000);
		
	}
}
