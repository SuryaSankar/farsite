package dos.common.fs.file;
import java.nio.channels.*;
/**
 * 
 * This is the actual representation of the block in a file host. Blocks are physically only Files. So this has the associated File
 * Channel objects. We can also implement block level locks and permissions here. Only ensure that they are consistent with what is 
 * stored in the file Inode
 * 	There is a one to one mapping between a block on the fileHost and a blockHandle on the name server
 * 
 *(Not used anywhere now)
 */
public class Block {
	BlockId blockId;
	long size;
	boolean lockedForWrite;
	
}
