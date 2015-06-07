package dos.dfs.farsite.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import dos.common.fs.file.BlockHandle;


import dos.common.util.*;
import dos.dfs.farsite.*;

public class Manager {
	Hashtable<String,Vector<InetSocketAddress> > NamespaceMap;
	public Manager(){
		NamespaceMap=SystemInitializer.provideNamespaceMap();
	}
	
	public Namespace closestKnownNamespace(Namespace namespace){
		Namespace closestMatch=namespace;
		while(!closestMatch.isNull()){
			if(NamespaceMap.containsKey(closestMatch.toString()))
				return closestMatch;
			closestMatch=new Namespace(closestMatch.parent().toString());
		}
		return closestMatch;		
	}
	public Vector<InetSocketAddress> getContactNodes(Namespace namespace) throws Exception{
		Namespace closestKnownNamespace=closestKnownNamespace(namespace);
		if(closestKnownNamespace.isNull()) 
			throw new Exception(Config.__NO_SUCH_NAMESPACE_ERROR__);
		return NamespaceMap.get(closestKnownNamespace.toString());
	}
	public String getMetadata(String nameSpaceString) throws IOException, InterruptedException{
		Namespace namespace=new Namespace(nameSpaceString);
		Namespace closestKnownNamespace=closestKnownNamespace(namespace);
		if(closestKnownNamespace.isNull()) 
			return Config.__NO_SUCH_NAMESPACE_ERROR__;
		Vector<InetSocketAddress> contactNodes=NamespaceMap.get(closestKnownNamespace.toString());
		String reply=Messenger.tryToGetReplyFromOne(contactNodes, Messages.LS(namespace));
		while(reply.startsWith(Messages.REMAP_NAMESPACE)){
			Tools.print(reply);
			contactNodes=Messages.getNewClusterHostsFromRemapCommand(reply);
			reply=Messenger.tryToGetReplyFromOne(contactNodes, Messages.LS(namespace));
		}
		NamespaceMap.put(nameSpaceString, contactNodes);
		return reply;
	}
	public String askDirectoryGroupToCreateDirectory(String nameSpaceString) throws IOException, InterruptedException{
		long startTime=System.currentTimeMillis();
		Tools.print("Entered manager at "+Long.toString(startTime));

		Namespace namespace=new Namespace(nameSpaceString);
		Namespace closestKnownNamespace=closestKnownNamespace(namespace);
		if(closestKnownNamespace.isNull()) 
			return Config.__NO_SUCH_NAMESPACE_ERROR__;
		Vector<InetSocketAddress> contactNodes=NamespaceMap.get(closestKnownNamespace.toString());
		String reply=Messenger.tryToGetReplyFromOne(contactNodes, Messages.MKDIR(namespace));
		long end=System.currentTimeMillis();
		Tools.print("Got reply in "+Long.toString(end-startTime)+" at "+Long.toString(end));

		return reply;
	}
	public String createFileEntry(String localFilePath,Namespace destinationNamespace) throws Exception{
		return Messenger.tryToGetReplyFromOne(getContactNodes(destinationNamespace), Messages.CREATE_FILE_AND_GIVE_HOSTS_AND_ID(destinationNamespace, Tools.fileSize(localFilePath)));
	}
	public Vector<Vector<InetSocketAddress>> getHostsToLoadTo(long fileSize,Namespace destinationNamespace) throws Exception{
		return Tools.getVectorVectorInetSocketAddress(Messenger.tryToGetReplyFromOne(getContactNodes(destinationNamespace), Messages.REQUEST_HOSTS_TO_LOAD_TO(destinationNamespace,fileSize)));	
	}
	public String getHostsAndDfsFileIdToDownload(Namespace namespace) throws Exception{
		Vector<InetSocketAddress> contactNodes=getContactNodes(namespace);
		return Messenger.tryToGetReplyFromOne(contactNodes, Messages.REQUEST_HOSTS_TO_DOWNLOAD_FROM(namespace));
		
	}
	
	
	
	
}
