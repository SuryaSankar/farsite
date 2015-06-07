package dos.common.fs.file;

import java.net.InetSocketAddress;
/**
 * 
 *This is the identifier used to identify a block within the dfs between nameserver and filehost.<p>
 *The blocks are actually stored as files of this name on filehost. So uniqueness and security should be ensured in the
 *static methods used to generate blockId here</p><p>
 *The client does not get to know this blockId guaranteeing security</p>
 *
 */
public class BlockId {//Implement block level naming schemes here

	String blockId;
	static String blockIdDelimiter="|";
	public BlockId(String blockId){
		this.blockId=blockId;
	}
	public String toString(){
		return this.blockId;
	}
	
	public static BlockId generateBlockIdentifier(String dfsFileId,int blockId,InetSocketAddress fileLoader){
		return new BlockId(fileLoader.toString().concat(blockIdDelimiter).concat(dfsFileId).concat(blockIdDelimiter).concat(Integer.toString(blockId)));
	}
	public static BlockId generateBlockIdentifier(String dfsFileId,int blockId){//Do not use `@` here as it would conflict with BlockHandle serialization. Or change the delim there
		return new BlockId(dfsFileId.concat(blockIdDelimiter).concat(Integer.toString(blockId)));
	}
}
