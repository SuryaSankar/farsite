package dos.common.fs;
import java.net.InetSocketAddress;
import java.util.*;
import dos.common.fs.file.BlockHandle;
import dos.common.util.Tools;

public class FileInode {

	int inodeId;
	Vector<BlockHandle> replicatedBlocks;
	FileInode(int inodeId){
		this.inodeId=inodeId;
		replicatedBlocks=new Vector<BlockHandle>();
	}
	FileInode(int inodeId,Vector<BlockHandle> replicatedBlocks){
		this.inodeId=inodeId;
		this.replicatedBlocks=replicatedBlocks;
	}
	public void addBlockReplica(BlockHandle replica){
		replicatedBlocks.add(replica);
	}
	public void removeBlockReplica(BlockHandle replica){
		replicatedBlocks.remove(replica);
	}
	public Vector<BlockHandle> replicatedBlocks(){
		return replicatedBlocks;
	}
	public Vector<InetSocketAddress> replicatedBlockAddresses(){
		Vector<InetSocketAddress> addresses=new Vector<InetSocketAddress>();
		for(BlockHandle block:replicatedBlocks)
			addresses.add(block.address());
		return addresses;
	}
	public int inodeId(){
		return inodeId;
	}
	boolean isEmpty(){
		return (replicatedBlocks.size()==0);
	}
	public int numberOfReplicas(){
		return replicatedBlocks.size();
	}
	public String toString(){
		String result=Integer.toString(inodeId).concat(FSParams.blockHandleSeparator);
		for(BlockHandle block:replicatedBlocks){
			result=result.concat(block.toString()).concat(FSParams.blockHandleSeparator);
		}
		return result;
	}
	public static FileInode buildFileInodeFromString(String str){
		StringTokenizer tokenizer=new StringTokenizer(str,FSParams.blockHandleSeparator);
		int inodeId=Integer.parseInt(tokenizer.nextToken());
		Vector<BlockHandle> blocks=new Vector<BlockHandle>();
		while(tokenizer.hasMoreTokens())
			blocks.add(BlockHandle.buildBlockHandleFromString(tokenizer.nextToken()));
		Tools.print("added blocks "+Tools.serializeVectorBlocks(blocks));
		return new FileInode(inodeId,blocks);
	}
	
}
