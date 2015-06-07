package dos.dfs.farsite.directorygroup;
import dos.common.fs.*;
import dos.common.fs.file.BlockHandle;
import dos.common.fs.file.BlockId;
import dos.common.util.*;
import dos.dfs.farsite.Config;
import dos.dfs.farsite.Messages;

import java.net.InetSocketAddress;
import java.util.*;

import javax.naming.InsufficientResourcesException;
import javax.naming.NameAlreadyBoundException;

public class DirectoryManager {

	static FileSystemTable fileSystemTable=new FileSystemTable(Config.CLUSTER_ROOT_NAMESPACE_);
	static long blockSize=Config.blockSize;
	static Hashtable<String,Namespace> dfsFileIdToNamespace=new Hashtable<String, Namespace>();
	static Hashtable<String, String> namespaceToDfsFileId=new Hashtable<String, String>();
	
	static Hashtable<String,Vector<InetSocketAddress>> remappedNamespaces=new Hashtable<String, Vector<InetSocketAddress>>();
	public static void setFileSystemTable(FileSystemTable fst){
		fileSystemTable=fst;
		
	}
	public static void resetBlockSize(){
		blockSize=Config.blockSize;
	}
	public static long getTotalHits(Namespace resourcePath) throws NoSuchElementException{
		    Tools.print("finding totalHits for "+resourcePath);
			long hits=fileSystemTable.getTotalNoOfHits(resourcePath);
			System.out.println("TOTAL HITS :"+hits);
			return hits;
		
	}
	
	public static String serializeSubDir(Namespace namespace,Vector<InetSocketAddress> newClusterHosts){
		String dirStructure=fileSystemTable.getFromDirectoryTable(namespace).serialize();
		return Messages.COMMAND_TO_CREATE_NEW_CLUSTER(namespace.toString(), dirStructure, blockSize);
	}
	public static void noteMigration(Namespace namespace,Vector<InetSocketAddress> newClusterHosts){
		String dirStructure=fileSystemTable.getFromDirectoryTable(namespace).serialize();
		FileSystemTable fst=FileSystemTable.buildFileSystemTableFromStructure(dirStructure);
		Enumeration<String> subDirs=fst.directories();
		Enumeration<String> files=fst.files();
		while(subDirs.hasMoreElements()){
			String subDir=subDirs.nextElement();
			System.out.println("remapping "+subDir);
			remappedNamespaces.put(subDir, newClusterHosts);
		}
		while(files.hasMoreElements())
			remappedNamespaces.put(files.nextElement(), newClusterHosts);
	}
	public static String createDirectory(Namespace resourcePath) {
		try {
			fileSystemTable.createNewDirectory(resourcePath).name();
		} catch (NoSuchElementException n) {
			return Messages.PARENT_DIRECTORY_DOES_NOT_EXIST;
		} catch (NameAlreadyBoundException n) {
			return Messages.DIRECTORY_ALREADY_EXISTS;
		} 
		return Messages.CREATED_DIRECTORY;
	}
	public static String listMetadata(Namespace resourcePath){
		String reply="";
		if(remappedNamespaces.containsKey(resourcePath.toString())){
			reply= Messages.INFORM_CLIENT_OF_REMAPPED_NAMESPACE(remappedNamespaces.get(resourcePath.toString()));
		}
		else if(fileSystemTable.containsDirectory(resourcePath)){
		Directory dir=fileSystemTable.findDirectory(resourcePath);
		Enumeration<String> subdirs =dir.subDirs();
		while(subdirs.hasMoreElements())
			reply=reply.concat(subdirs.nextElement().concat(" "));
		reply=reply.concat("\n");
		Enumeration<String> files=dir.subFiles();
		while(files.hasMoreElements())
			reply=reply.concat(files.nextElement().concat(" "));
		}
		else if(fileSystemTable.containsFile(resourcePath)){
			FileInodeTable fit=fileSystemTable.findFile(resourcePath);
			reply= fit.serialize();
		}
		else reply= "No such resource found";
		Tools.print("replying with "+reply);
		return reply;
	}
	public static String getDfsFileId(Namespace namespace) throws Exception{
		return toDFSFileId(namespace);
	}
	
	public static String createFileEntryAndReturnDFSFileId(Namespace destinationNamespace,int noOfChunks) throws Exception{
		fileSystemTable.createAFileEntryAndBeReadyToStoreBlockInfo(destinationNamespace, noOfChunks);
		String dfsFileId=toDFSFileId(destinationNamespace);
		dfsFileIdToNamespace.put(dfsFileId, destinationNamespace);
		namespaceToDfsFileId.put(destinationNamespace.toString(), dfsFileId);
		return dfsFileId;
	}
	public static void addBlockReplicaToFile(InetSocketAddress blockStorer,String dfsFileId,BlockId blockId,int inodeIndex,long blockSize){
		fileSystemTable.addBlockReplicaToFile(dfsFileIdToNamespace.get(dfsFileId),blockStorer, blockId, inodeIndex,blockSize);
	}
	public static Vector<Vector<InetSocketAddress>> blockAddresses(Namespace file) throws NoSuchElementException, InsufficientResourcesException{
		return fileSystemTable.blockAddresses(file);
	}
	public static Vector<Vector<BlockHandle>> blocks(Namespace file) throws NoSuchElementException, InsufficientResourcesException{
		return fileSystemTable.fileBlocks(file);
	}
	public static String toDFSFileId(Namespace resourcePath) throws Exception{
		String resourceString=resourcePath.toString();
		if(resourceString.contains("^"))
			throw new Exception("Character ^ reserved for internal use");
		return resourceString.replace('/', '^');
	}
	public static String dfsFileIdToNamespaceString(String dfsFileId){
		return dfsFileId.replace('^', '/');
	}
	
}
