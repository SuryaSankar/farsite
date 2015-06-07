package dos.common.util;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;

import dos.common.fs.file.BlockHandle;


public class Tools {
	/**
	 * Contains all miscellaneous tools used throughout dfs
	 */

	public static void print(String string){
			System.out.println(string);
	}
	public static void print(Object o){
		System.out.println(o.toString());
	}

	public static InetSocketAddress generateAddress(int port){
		InetAddress myInetAddress=null;
		InetSocketAddress myAddress=null;
		try {
			myInetAddress=InetAddress.getLocalHost();
			myAddress=new InetSocketAddress(myInetAddress,port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return myAddress;	
	}
	
	public static String baseName(String filePath){
		return filePath.substring(filePath.lastIndexOf("/")+1);
	}
	public static String getParent(String filePath){
		return filePath.substring(0, filePath.lastIndexOf("/"));
	}
	public static String readNext()
	{
		try
		{
			byte[] data = new byte[500];
			int i = 0;
			int temp;

			System.in.mark(1000);
			temp = System.in.read();
			while((temp != (int)' ') && (temp != (int)'\n') && (temp != -1))
			{
				data[i] = (byte)temp;
				i++;
				System.in.mark(1000);
				temp = System.in.read();
				if(temp == (int)'\n' || (temp == -1))
				System.in.reset();
			}
			return new String(data, 0, i);
		}
		catch(Exception e)
		{
			System.out.println(" An exception has occurred in readNext: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public static String readNextLine()
	{
		try
		{
			byte[] data = new byte[800];
			int i = 0;
			int temp;

			temp = System.in.read();
			while((temp != (int)'\n') && (temp != -1))
			{
				data[i] = (byte)temp;
				i++;
				temp = System.in.read();
			}
			return new String(data, 0, i);
		}
		catch(Exception e)
		{
			System.out.println(" An exception has occurred in readNextLine: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public static String  toString(Enumeration<String> enumeration){
		String result="";
		String delim=" ";
		while(enumeration.hasMoreElements())
			result=result.concat(delim).concat(enumeration.nextElement());
		return result;
	}
	
	public Vector<String> vectorizeTokens(String enumString){
		String delim=" ";
		Vector<String> strings=new Vector<String>();
		StringTokenizer tokenizer=new StringTokenizer(enumString,delim);
		while(tokenizer.hasMoreTokens()){
			strings.add(tokenizer.nextToken());
		}
		return strings;
	}

	public static Vector<Vector<InetSocketAddress>> getVectorVectorInetSocketAddress(String string){
		String delim="$";
		Vector<Vector<InetSocketAddress>> V=new Vector<Vector<InetSocketAddress>>();
		StringTokenizer tokenizer=new StringTokenizer(string,delim);
		while(tokenizer.hasMoreTokens())
			V.add(getVectorInetSocketAddress(tokenizer.nextToken()));
		return V;
	}
	public static Vector<InetSocketAddress> getVectorInetSocketAddress(String string){
		String delim="%";
		Vector<InetSocketAddress> v=new Vector<InetSocketAddress>();
		StringTokenizer tokenizer=new StringTokenizer(string,delim);
		while(tokenizer.hasMoreTokens())
			v.add(Tools.toInetSocketAddress(tokenizer.nextToken()));
		return v;
	}
	public static String serializeVectorVectorAddress(Vector<Vector<InetSocketAddress>> V){
		String firstLevelDelim="$";
		String secondLevelDelim="%";
		String result="";
		for(Vector<InetSocketAddress> innerV:V){
			for(InetSocketAddress address:innerV)
				result=result.concat(address.toString()).concat(secondLevelDelim);
			result=result.concat(firstLevelDelim);
		}
		return result;
	}
	public static String serializeVectorAddress(Vector<InetSocketAddress> v){
		String delim="%";
		String result="";
		for(InetSocketAddress address:v)
			result=result.concat(address.toString()).concat(delim);
		return result;
	}
	public static String serializeVectorVectorBlocks(Vector<Vector<BlockHandle>> vBlocks){
		String delim="$";
		String result="";
		for(Vector<BlockHandle> blocks:vBlocks)
			result=result.concat(serializeVectorBlocks(blocks)).concat(delim);
		return result;
	}
	
	public static String serializeVectorBlocks(Vector<BlockHandle> blocks){
		String delim="%";
		String result="";
		for(BlockHandle block:blocks)
			result=result.concat(block.toString()).concat(delim);
		return result;
	}

	public static Vector<BlockHandle> getVectorBlocks(String str){
		StringTokenizer tokenizer=new StringTokenizer(str,"%");
		Vector<BlockHandle> blocks=new Vector<BlockHandle>();
		while(tokenizer.hasMoreTokens())
			blocks.add(BlockHandle.buildBlockHandleFromString(tokenizer.nextToken()));
		return blocks;
	}
	public static Vector<Vector<BlockHandle>> getVectorVectorBlocks(String str){
		StringTokenizer tokenizer=new StringTokenizer(str,"$");
		Vector<Vector<BlockHandle>> blocks=new Vector<Vector<BlockHandle>>();
		while(tokenizer.hasMoreTokens())
			blocks.add(getVectorBlocks(tokenizer.nextToken()));
		return blocks;
	}
	public static  InetSocketAddress toInetSocketAddress(String address){
		StringTokenizer tokenizer=new StringTokenizer(address,":");
		String hostName=tokenizer.nextToken();
		if(hostName.contains("/"))
			hostName=hostName.substring(hostName.indexOf("/")+1);
		int port=Integer.parseInt(tokenizer.nextToken());
		return new InetSocketAddress(hostName,port);
	}
	
	public static long fileSize(String filename) throws FileNotFoundException, IOException{
		return new FileInputStream(filename).getChannel().size();
	}
	public static int ceilingOfHalf(int number){
		if(number%2==0)
			return number/2;
		else return number/2+1;
	}
	public static Vector<InetSocketAddress> readHostsFromFile(String filePath) throws IOException{
		Vector<InetSocketAddress> hosts=new Vector<InetSocketAddress>();
		TextFileHandle hostsFile=new TextFileHandle(filePath);
		hostsFile.openForRead();
		for(String line=hostsFile.readLine();line!=null;line=hostsFile.readLine())
			hosts.add(toInetSocketAddress(line));
		return hosts;
	}
	
	public static void main(String args[]){
		InetSocketAddress address=generateAddress(9120);
		InetSocketAddress add2=toInetSocketAddress("127.0.1.1:9120");
		if(add2.equals(address)){
			print("true");
		}
		print(address.toString());
		print(Tools.toInetSocketAddress(address.toString()));
		InetAddress inetaddr=address.getAddress();
		InetSocketAddress newAdd=new InetSocketAddress("10.6.9.19",9120);
		print(newAdd.toString());
		print(inetaddr.toString());
		String path="/var/www/html";
		Tools.print(path.substring(path.indexOf("/")+1));
		String newPath=path.replace("/","$");
		Tools.print(newPath);
	}
}
