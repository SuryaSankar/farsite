package dos.dfs.farsite;

import dos.common.client.CommunicationTools;
import dos.common.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;
public class SystemInitializer {
	static Vector<InetSocketAddress> fileHosts=new Vector<InetSocketAddress>();
	public static Hashtable<String, Vector<InetSocketAddress>> NamespaceMap=new Hashtable<String, Vector<InetSocketAddress>>();
	public static void initializeRootNamespace(){
	
	}
	
	public static void initializeFileHosts(){
		TextFileHandle hostsFile=new TextFileHandle(Config.__FILE_HOSTS__);
		try {
			hostsFile.openForRead();
		} catch (FileNotFoundException e) {
			System.out.println(Config.__FILE_HOSTS__+" no found");
		}
		String line=null;
		try {
			while((line=hostsFile.readLine())!=null){
				InetSocketAddress address=Tools.toInetSocketAddress(line);
				//fileHosts.add(new InetSocketAddress(line,Config.fileHostListenerPort));
				fileHosts.add(address);
			}
		} catch (IOException e) {
			System.out.println("Exception while reading "+Config.__FILE_HOSTS__+" "+e);
			e.printStackTrace();
		}
	}
	public static void knowNonClusterHosts(){
		TextFileHandle hostsFile=new TextFileHandle(Config.__ALL_NODES__);
		try {
			hostsFile.openForRead();
		} catch (FileNotFoundException e) {
			System.out.println(Config.__ALL_NODES__+" no found");
		}
		String line=null;
		try {
			while((line=hostsFile.readLine())!=null)
				Config.allOtherHosts.add(Tools.toInetSocketAddress(line));
		} catch (IOException e) {
			System.out.println("Exception while reading "+Config.__ALL_NODES__+" "+e);
			e.printStackTrace();
		}

	}
	public static void knowClusterMates(){
		TextFileHandle hostsFile=new TextFileHandle(Config.__CLUSTER_MATES__);
		try {
			hostsFile.openForRead();
		} catch (FileNotFoundException e) {
			System.out.println(Config.__CLUSTER_MATES__+" not found");
		}
		String line=null;
		try {
			while((line=hostsFile.readLine())!=null){
				InetSocketAddress address=Tools.toInetSocketAddress(line);
				if(!Tools.generateAddress(Config.clusterConsistencyPort).equals(address)){
					Config.clusterMates.add(address);
					Tools.print(address);
				}
			}
		} catch (IOException e) {
			System.out.println("Exception while reading "+Config.__CLUSTER_MATES__+" "+e);
			e.printStackTrace();
		}
		
	}
	public static void initializeNamespaceMap(){
		TextFileHandle NamespaceMapFile=new TextFileHandle(Config.__NAMESPACE_MAP__);
		NamespaceMap=new Hashtable<String, Vector<InetSocketAddress>>();
		try {
			NamespaceMapFile.openForRead();
		} catch (FileNotFoundException e) {
			System.out.println(Config.__NAMESPACE_MAP__+" no found");
			e.printStackTrace();
		}
		String KeyValuePair;
		String FieldSeparator=" ";
		try {
			while((KeyValuePair=NamespaceMapFile.readLine())!=null){
					StringTokenizer tokenizer=new StringTokenizer(KeyValuePair,FieldSeparator);
					String nmstring=tokenizer.nextToken();
					Namespace namespace=new Namespace(nmstring);
					Vector<InetSocketAddress> respNodes=new Vector<InetSocketAddress>();
					StringTokenizer tokenizer2=new StringTokenizer(tokenizer.nextToken(),",");
					while(tokenizer2.hasMoreTokens()){
						String addr=tokenizer2.nextToken();
						Tools.print(addr);
						//respNodes.add(new InetSocketAddress(tokenizer2.nextToken(),Config.directoryGroupListeningPort));
						respNodes.add(Tools.toInetSocketAddress(addr));
					}
					NamespaceMap.put(namespace.toString(), respNodes);
					}
		} catch (IOException e) {
			System.out.println("Exception while reading "+Config.__NAMESPACE_MAP__+" "+e);
			e.printStackTrace();
		}
	}
	
	public static Vector<InetSocketAddress> provideFileHostsList(){
		return fileHosts;
	}
	
	public static Hashtable<String, Vector<InetSocketAddress>> provideNamespaceMap(){
		return NamespaceMap;
	}

	
	public static void main(String args[]){
		InetSocketAddress addr1=Tools.generateAddress(9120);
		InetSocketAddress addr=Tools.toInetSocketAddress("10.6.9.18:9120");
		if(addr.equals(addr1))
			Tools.print(addr + "equals" + addr1);
		else
			Tools.print("no");
	}
}
