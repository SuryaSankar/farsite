package dos.dfs.farsite.client;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;



import dos.common.util.*;
import dos.dfs.farsite.Config;

public class Manager {
	FileHandle NamespaceMapFile=new FileHandle(Config.__NAMESPACE_MAP__);
	Hashtable<String,Vector<InetSocketAddress> > NamespaceMap;
	public Manager(){
		createNamespaceMap();
	}
	
	public void createNamespaceMap(){
		NamespaceMap=new Hashtable<String, Vector<InetSocketAddress>>();
		NamespaceMapFile.openForRead();
		String KeyValuePair;
		String FieldSeparator=" ";
		while((KeyValuePair=NamespaceMapFile.readLine())!=null){
				StringTokenizer tokenizer=new StringTokenizer(KeyValuePair,FieldSeparator);
				Namespace namespace=new Namespace(tokenizer.nextToken());
				Vector<InetSocketAddress> respNodes=new Vector<InetSocketAddress>();
				StringTokenizer tokenizer2=new StringTokenizer(tokenizer.nextToken(),",");
				while(tokenizer2.hasMoreTokens())
					respNodes.add(new InetSocketAddress(tokenizer2.nextToken(),Config.directoryGroupListeningPort));
				NamespaceMap.put(namespace.toString(), respNodes);
		}
	}
	public Namespace closestKnownNamespace(Namespace namespace){
		Namespace closestMatch=namespace;
		Tools.print("Inside closestKnownNamespace");	 
		while(!closestMatch.isNull()){
			System.out.println("Checking "+closestMatch);
			System.out.println(NamespaceMap.get(closestMatch.toString()));
			if(NamespaceMap.containsKey(closestMatch.toString()))
				return closestMatch;
			closestMatch=closestMatch.parent();
		}
		return closestMatch;		
	}
	
	public String getMetadata(String nameSpaceString){
		Namespace namespace=new Namespace(nameSpaceString);
		Namespace closestKnownNamespace=closestKnownNamespace(namespace);
		if(closestKnownNamespace.isNull()) 
			return Config.__NO_SUCH_NAMESPACE_ERROR__;
		Vector<InetSocketAddress> contactNodes=NamespaceMap.get(closestKnownNamespace.toString());
		return Messenger.tryToGetReplyFromOne(contactNodes, Commands.LS(namespace));
	}
	public String askDirectoryGroupToCreateDirectory(String nameSpaceString){
		Namespace namespace=new Namespace(nameSpaceString);
		Namespace closestKnownNamespace=closestKnownNamespace(namespace);
		if(closestKnownNamespace.isNull()) 
			return Config.__NO_SUCH_NAMESPACE_ERROR__;
		Vector<InetSocketAddress> contactNodes=NamespaceMap.get(closestKnownNamespace.toString());
		return Messenger.tryToGetReplyFromOne(contactNodes, Commands.MKDIR(namespace));	
	}
}
