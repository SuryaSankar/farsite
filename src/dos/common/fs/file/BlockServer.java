package dos.common.fs.file;
import java.io.IOException;
import java.net.*;

import dos.common.server.*;

/**
 * 
 * look at the implementation of Replying Server to understand how this works. 
 *
 */
public abstract class BlockServer extends ReplyingServer {




	public BlockServer(int requestListeningPort) throws IOException{
		super(requestListeningPort);
	}
	public abstract BlockRequestHandler provideRequestHandler(Socket connection) throws IOException,InterruptedException;



}
