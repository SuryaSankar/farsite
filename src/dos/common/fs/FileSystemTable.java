package dos.common.fs;
import dos.common.util.*;
import dos.common.fs.file.*;
import java.util.*;
import java.net.*;

import javax.naming.InsufficientResourcesException;
import javax.naming.NameAlreadyBoundException;
public class FileSystemTable {

	Hashtable<String, Directory> directoryTable;
	Hashtable<String, FileInodeTable> fileTable;
	Hashtable<String,Long> namespaceHits;
	Namespace rootNamespace;
	Directory root;
	
	public FileSystemTable(String rootString){
		directoryTable=new Hashtable<String, Directory>();
		fileTable=new Hashtable<String, FileInodeTable>();
		namespaceHits=new Hashtable<String, Long>();
		createRoot(new Namespace(rootString));
	}
	public void createRoot(Namespace rootNamespace){
		root=new Directory(rootNamespace);
		addToDirectoryTable(rootNamespace, root);
		this.rootNamespace=rootNamespace;
		setHits(rootNamespace, (long) 1);
	}
	public Directory root(){
		return root;
	}
	
	public synchronized void addToDirectoryTable(Namespace namespace, Directory directory){
		directoryTable.put(namespace.toString(), directory);
	}
	public synchronized void addToFileTable(Namespace namespace, FileInodeTable inodeTable){
		fileTable.put(namespace.toString(), inodeTable);
	}
	public synchronized Directory getFromDirectoryTable(Namespace namespace){
		return directoryTable.get(namespace.toString());
	}
	public synchronized Directory getFromDirectoryTable(String string){
		return directoryTable.get(string);
	}
	public synchronized FileInodeTable getFromFileTable(Namespace namespace){
		return fileTable.get(namespace.toString());
	}
	public synchronized boolean containsDirectory(Namespace namespace){
		return directoryTable.containsKey(namespace.toString());
	}
	public synchronized boolean containsFile(Namespace namespace){
		return fileTable.containsKey(namespace.toString());
	}
	public synchronized long getHits(Namespace resourcePath){
		return namespaceHits.get(resourcePath.toString());
	}
	public synchronized long getHits(String namespaceString){
		return namespaceHits.get(namespaceString);
	}
	public synchronized void setHits(Namespace resourcePath,long l){
		namespaceHits.put(resourcePath.toString(), l);
	}
	public synchronized void addHit(Namespace resourcePath){
		namespaceHits.put(resourcePath.toString(), namespaceHits.get(resourcePath.toString())+1);
	}


	public FileInodeTable findFile(Namespace resourcePath){
		if(fileTable.containsKey(resourcePath.toString())){
			addHit(resourcePath);
			return fileTable.get(resourcePath.toString());
		}
		else
			throw new NoSuchElementException("FILE DOES NOT EXIST");
	}
	public Directory findDirectory(Namespace resourcePath) throws NoSuchElementException{
		System.out.println("doing ls for "+resourcePath);
		if(directoryTable.containsKey(resourcePath.toString())){
			addHit(resourcePath);
			return directoryTable.get(resourcePath.toString());
		}
		else
			throw new NoSuchElementException("DIRECTORY DOES NOT EXIST");	
	}
	

	public Directory createNewDirectory(Namespace resourcePath) throws NoSuchElementException,NameAlreadyBoundException{
		Tools.print("Entered createNewDirectory at "+Long.toString(System.currentTimeMillis()));
		if(directoryTable.containsKey(resourcePath.toString())){
			addHit(resourcePath);
			throw new NameAlreadyBoundException("DIRECTORY EXISTS");
		}
		Namespace parent=resourcePath.parent();
		Directory result;
		int i=0;
		if(directoryTable.containsKey(parent.toString())){
			Directory parentDir= directoryTable.get(parent.toString());
			Tools.print("Step "+Integer.toString(++i)+" got "+Long.toString(System.currentTimeMillis()));
			result=parentDir.addNewSubDirectoryEntry(resourcePath.baseName());
			Tools.print("Step "+Integer.toString(++i)+" took "+Long.toString(System.currentTimeMillis()));
			addToDirectoryTable(resourcePath, result);
			Tools.print("Step "+Integer.toString(++i)+" took "+Long.toString(System.currentTimeMillis()));
			setHits(resourcePath, (long) 1);
			Tools.print("Step "+Integer.toString(++i)+" took "+Long.toString(System.currentTimeMillis()));
			return result;
		}
		else
			throw new NoSuchElementException("PARENT DOES NOT EXIST");
		
	}
	


	public FileInodeTable createAFileEntryAndBeReadyToStoreBlockInfo(Namespace resourcePath,int noOfInodes) throws Exception{
		if(directoryTable.containsKey(resourcePath.toString())){
			addHit(resourcePath);
			throw new NameAlreadyBoundException("FILE EXISTS");
		}
		Namespace parent=resourcePath.parent();
		if(directoryTable.containsKey(parent.toString())){
			Directory parentDir= directoryTable.get(parent.toString());
			FileInodeTable fileInodeTable=parentDir.addNewFileEntry(resourcePath.baseName(), noOfInodes);
			addToFileTable(resourcePath, fileInodeTable);
			setHits(resourcePath, (long) 1);
			return fileInodeTable;
		}
		else
			throw new NoSuchElementException("PARENT DOES NOT EXIST");

	}
	public long getTotalNoOfHits(Namespace resourcePath) throws NoSuchElementException{
		System.out.println("Checking total no of hits for "+resourcePath);
		if(fileTable.containsKey(resourcePath.toString()))
			return getHits(resourcePath);
		else if(directoryTable.containsKey(resourcePath.toString())){
			Directory directory=getFromDirectoryTable(resourcePath);
			long totalHits=getHits(resourcePath);
			if(directory.noOfImmediateSubFiles()>0){
				Enumeration<String> subFileNamespaces=directory.subFiles();
				while(subFileNamespaces.hasMoreElements())
					totalHits+=getHits(directory.getFileByName(subFileNamespaces.nextElement()).namespace());
			}
			if(directory.noOfImmediateSubDirs()>0){
				Enumeration<String> subDirNamespaces=directory.subDirs();
				while(subDirNamespaces.hasMoreElements())
					totalHits+=getTotalNoOfHits(directory.getSubDirectoryByName(subDirNamespaces.nextElement()).namespace());
			}
			return totalHits;
		}
		else
			throw new NoSuchElementException("RESOURCE DOES NOT EXIST");
	}

	public void storeBlockInfo(Namespace resourcePath,Vector<Vector<BlockHandle> > fileBlocks){
		FileInodeTable fileInodeTable=fileTable.get(resourcePath.toString());
		int inodeId=1;
		for (Vector<BlockHandle> inodeBlocks:fileBlocks){
			fileInodeTable.put(inodeId,new FileInode(inodeId,inodeBlocks));
			inodeId++;
		}		
	}

	public void addBlockReplicaToFile(Namespace namespace,InetSocketAddress blockStorer,BlockId blockId,int inodeIndex,long blockSize){
		getFromFileTable(namespace).addBlockReplicaToInode(inodeIndex, new BlockHandle(blockId,blockStorer,blockSize));
	}
	public Vector<Vector<InetSocketAddress>> getFileStorers(Namespace filePath) throws NoSuchElementException{
		if(fileExists(filePath)){
			Vector<Vector<InetSocketAddress>> listOfBlockStorerLists=new Vector<Vector<InetSocketAddress>>();
			FileInodeTable fileInodeTable=fileTable.get(filePath.toString());
			for(int inodeId=1;inodeId<=fileInodeTable.noOfInodes();inodeId++)
				listOfBlockStorerLists.add(convertBlockHandleListToAddressList(fileInodeTable.get(inodeId).replicatedBlocks()));
			return listOfBlockStorerLists;
		}
		else throw new NoSuchElementException("No such resource found");
	}

	public boolean fileExists(Namespace resourcePath){
		return fileTable.containsKey(resourcePath.toString());
	}
	public FileInodeTable getFileInodeTable(Namespace resourcePath){
		return fileTable.get(resourcePath.toString());
	}
	public Vector<Vector<InetSocketAddress>> blockAddresses(Namespace file) throws NoSuchElementException, InsufficientResourcesException{
		FileInodeTable fit=findFile(file);
		if(fit.fileHasAllInodes())
			return fit.blockAddresses();
		else {
			Tools.print(fit.noOfEmptyInodes());
			throw new InsufficientResourcesException("Blocks missing");
		}
	}
	public Vector<Vector<BlockHandle>> fileBlocks(Namespace file)throws NoSuchElementException, InsufficientResourcesException{
		FileInodeTable fit=findFile(file);
		if(fit.fileHasAllInodes())
			return fit.blocks();
		else throw new InsufficientResourcesException("Blocks missing");
	}

	public static Vector<InetSocketAddress> convertBlockHandleListToAddressList(Vector<BlockHandle> blockHandles){
		Vector<InetSocketAddress> addresses=new Vector<InetSocketAddress>();
		for(BlockHandle blockHandle: blockHandles)
			addresses.add(blockHandle.address());
		return addresses;
	}
	public String toString(){
		String str=root.recursivelySerialize().concat(FSParams.fileSeparator);
		Enumeration<String> files=fileTable.keys();
		while(files.hasMoreElements()){
			String namespace=files.nextElement();
			FileInodeTable fit=fileTable.get(namespace);
			str=str.concat(namespace).concat(FSParams.namespaceFileSeparator).concat(fit.serialize()).concat(FSParams.fileSeparator);
		}
		return str;
	}
	
	public static FileSystemTable buildFileSystemTableFromStructure(String string){	
		StringTokenizer fileTokenizer=new StringTokenizer(string,FSParams.fileSeparator);
		String dirString=fileTokenizer.nextToken();
		StringTokenizer tokenizer=new StringTokenizer(dirString,FSParams.subDirSerializationdelim);
		String rootNamespaceString=tokenizer.nextToken();
		FileSystemTable fst=new FileSystemTable(rootNamespaceString);
		while(tokenizer.hasMoreTokens()){
			try {
				fst.createNewDirectory(new Namespace(tokenizer.nextToken()));
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NameAlreadyBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while(fileTokenizer.hasMoreTokens()){
			String token=fileTokenizer.nextToken();
			int sep=token.indexOf(FSParams.namespaceFileSeparator);
			Namespace namespace=new Namespace(token.substring(0, sep));
			FileInodeTable fit=FileInodeTable.buildFileInodeTableFromStructure(token.substring(sep+1));
			fst.addToFileTable(namespace, fit);
			fst.setHits(namespace, 0);
			fst.getFromDirectoryTable(namespace.parent()).addFileEntry(namespace.baseName(), fit);
		}
		return fst;
	}
	public synchronized Enumeration<String> directories(){
		return directoryTable.keys();
	}
	public synchronized Enumeration<String> files(){
		return fileTable.keys();
	}
	
	public static void main(String args[]){
		try {
			FileSystemTable fst1=new FileSystemTable("root");
			fst1.createNewDirectory(new Namespace("root/dir1"));
			fst1.createNewDirectory(new Namespace("root/dir2"));
			fst1.createNewDirectory(new Namespace("root/dir3"));
			fst1.createNewDirectory(new Namespace("root/dir1/dir1"));
			fst1.createAFileEntryAndBeReadyToStoreBlockInfo(new Namespace("root/dir1/grid"), 10);
			fst1.createAFileEntryAndBeReadyToStoreBlockInfo(new Namespace("root/dir1/bore"), 15);
			fst1.createNewDirectory(new Namespace("root/dir1/dir2"));
			fst1.createNewDirectory(new Namespace("root/dir1/dir1/dir1"));
			fst1.createAFileEntryAndBeReadyToStoreBlockInfo(new Namespace("root/dir1/dir1/crane"), 10);
			Tools.print("orig "+fst1.root().serialize());
			String ds=fst1.getFromDirectoryTable("root/dir1/dir1").serialize();;
			Tools.print("sub "+buildFileSystemTableFromStructure(ds).root().serialize());
			
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NameAlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
