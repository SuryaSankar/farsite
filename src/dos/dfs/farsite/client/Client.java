package dos.dfs.farsite.client;
import dos.common.util.TextFileHandle;
public class Client {
   ClientDaemon clientDaemon;
	public void start(){
		clientDaemon=new ClientDaemon();
		clientDaemon.start();
	}
	public void startInteractiveSession(){
		clientDaemon=new ClientDaemon(true);
		clientDaemon.start();
	}
	public  void startInTestingMode(String testFile){
		clientDaemon=new ClientDaemon(true);
		clientDaemon.switchToTestingMode(new TextFileHandle(testFile));
		clientDaemon.start();
	}
}
