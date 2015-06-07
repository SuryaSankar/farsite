package dos.common.fs;
import java.net.InetSocketAddress;
import java.util.*;

import dos.common.fs.file.BlockHandle;
import dos.common.util.*;
public class FileInodeTable {

	String dfsFileIdentifier;
	Namespace associatedNamespace;
	Hashtable<Integer, FileInode > inodeTable;
	int noOfInodes;
	int noOfEmptyInodes;
	/*
	public FileInodeTable(String dfsFileIdentifier) {
		this.dfsFileIdentifier=dfsFileIdentifier;
		inodeTable=new Hashtable<Integer, FileInode>();
	}
	public FileInodeTable(String dfsFileIdentifier,int noOfInodes){
		this.dfsFileIdentifier=dfsFileIdentifier;
		inodeTable=new Hashtable<Integer, FileInode>();
		this.noOfInodes=noOfInodes;
		noOfEmptyInodes=noOfInodes;
		for (int inodeId=1;inodeId<=noOfInodes;inodeId++)
			inodeTable.put(inodeId,new FileInode(inodeId));
	}
	*/
	public FileInodeTable(Namespace namespace,int noOfInodes){
		this.associatedNamespace=namespace;
		inodeTable=new Hashtable<Integer, FileInode>();
		this.noOfInodes=noOfInodes;
		noOfEmptyInodes=noOfInodes;
		for (int inodeId=1;inodeId<=noOfInodes;inodeId++)
			inodeTable.put(inodeId,new FileInode(inodeId));
	}
/*
	public String dfsFileId(){
		return dfsFileIdentifier;
	}
	*/
	public boolean hasInodeEntry(int inodeId){
		return inodeTable.containsKey(inodeId);
	}
	public boolean inodeEntryIsEmpty(int inodeId){
		return inodeTable.get(inodeId).isEmpty();
	}
	public int noOfInodes(){
		return noOfInodes;
	}
	public int noOfEmptyInodes(){
		return noOfEmptyInodes;
	}
	public void put(int inodeIndex,FileInode inode){
		if(inodeEntryIsEmpty(inodeIndex))
			noOfEmptyInodes--;
		inodeTable.put(inodeIndex, inode);
	}
	public FileInode get(int inodeIndex){
		return inodeTable.get(inodeIndex);
	}
	public void addBlockReplicaToInode(int inodeIndex,BlockHandle block){
		FileInode inode=inodeTable.get(inodeIndex);
		if(inode.isEmpty())
			noOfEmptyInodes--;
		inode.addBlockReplica(block);
	}
	public boolean fileHasAllInodes(){
		return noOfEmptyInodes<=0;
	}
	public Vector<Vector<BlockHandle>> blocks(){
		Vector<Vector<BlockHandle>> blocks=new Vector<Vector<BlockHandle>>();
		for(int i=1;i<noOfInodes;i++)
			blocks.add(inodeTable.get(i).replicatedBlocks());
		return blocks;
	}
	public Vector<Vector<InetSocketAddress>> blockAddresses(){
		Vector<Vector<InetSocketAddress>> addresses=new Vector<Vector<InetSocketAddress>>();
		for(int i=1;i<=noOfInodes;i++){
			Vector<InetSocketAddress> replicatedBlocks=inodeTable.get(i).replicatedBlockAddresses();
			Tools.print("Got Block addresses "+Tools.serializeVectorAddress(replicatedBlocks)+" for inode "+i);
			addresses.add(replicatedBlocks);
		}
		return addresses;
	}
	public Namespace namespace(){
		return associatedNamespace;
	}
	public String serialize(){
		String result=associatedNamespace.toString().concat(FSParams.fileInodeSeparator).concat(Integer.toString(noOfInodes)).concat(FSParams.fileInodeSeparator).concat(FSParams.fileInodeSeparator);
		Enumeration<Integer> inodes=inodeTable.keys();
		while(inodes.hasMoreElements()){
			int inodeId=inodes.nextElement();
			result=result.concat(Integer.toString(inodeId)).concat(FSParams.inodeIdInodeSeparator).concat(inodeTable.get(inodeId).toString()).concat(FSParams.fileInodeSeparator);
		}
		return result;
	}
	public static FileInodeTable buildFileInodeTableFromStructure(String str){
		StringTokenizer tokenizer=new StringTokenizer(str,FSParams.fileInodeSeparator);
		Namespace namespace=new Namespace(tokenizer.nextToken());
		int noOfInodes=Integer.parseInt(tokenizer.nextToken());
		FileInodeTable fit=new FileInodeTable(namespace,noOfInodes);
		while(tokenizer.hasMoreTokens()){
			String token=tokenizer.nextToken();
			StringTokenizer tokenizer2=new StringTokenizer(token,FSParams.inodeIdInodeSeparator);
			int inodeId=Integer.parseInt(tokenizer2.nextToken());
			FileInode inode=FileInode.buildFileInodeFromString(tokenizer2.nextToken());
			fit.put(inodeId, inode);
		}
		return fit;
	}
}
