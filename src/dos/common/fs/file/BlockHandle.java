package dos.common.fs.file;
import java.net.*;
import java.util.StringTokenizer;

import dos.common.util.Tools;
/**
 * 
 *This is the top level blockHandle stored on a nameserver's inode Table. This is one level above BlockId. It stores the storer address also.
 *</br>The nameserver also stores other block level properties here. A file inode should contain a list of blockHandles corresponding to 
 *various replicas
 *
 */
public class BlockHandle {
	BlockId blockId;
	InetSocketAddress blockStorerAddress;
	long blockSize;
	boolean lockedForWrite;
	boolean lockedForRead;
	public BlockHandle(BlockId blockId,InetSocketAddress blockStorerAddress,long blockSize){
		this.blockId=blockId;
		this.blockStorerAddress=blockStorerAddress;
		this.blockSize=blockSize;
	}
	public InetSocketAddress address(){
		return blockStorerAddress;
	}
	public BlockId blockId(){
		return blockId;
	}
	public String toString(){
		return blockId.toString().concat("@").concat(blockStorerAddress.toString()).concat("@").concat(Long.toString(blockSize));
	}
	public static BlockHandle buildBlockHandleFromString(String str){
		StringTokenizer tokenizer=new StringTokenizer(str,"@");
		return new BlockHandle(new BlockId(tokenizer.nextToken()),Tools.toInetSocketAddress(tokenizer.nextToken()),Long.parseLong(tokenizer.nextToken()));
	}
}
