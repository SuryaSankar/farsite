package dos.dfs.farsite.directorygroup;

import java.io.IOException;
import java.net.Socket;

import dos.common.server.ReplyingServer;
import dos.dfs.farsite.Config;

public class NameServerDaemon extends ReplyingServer{
	ConsistencyMaintainer consistencyMaintainer;
	public NameServerDaemon() {
		super(Config.directoryGroupListeningPort);
	}
	public NameServerRequestHandler provideRequestHandler(Socket connection){
		return new NameServerRequestHandler(connection);
	}

	public void boot(){
		try {
			super.boot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
