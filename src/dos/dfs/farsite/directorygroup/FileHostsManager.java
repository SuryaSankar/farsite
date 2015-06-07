package dos.dfs.farsite.directorygroup;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.*;
import dos.dfs.farsite.SystemInitializer;
public class FileHostsManager {

	static Vector<InetSocketAddress> allFileHosts=SystemInitializer.provideFileHostsList();
	public static Vector<Vector<InetSocketAddress>> getSuitableFileHostsAccountingForReplication(int noOfChunks,int noOfReplicas){
		int noOfAvailableHosts=allFileHosts.size();
		Vector<Vector<InetSocketAddress>> suitableFileHosts=new Vector<Vector<InetSocketAddress>>();
		for(int i=0;i<noOfChunks;i++){
			Vector<InetSocketAddress> blockHosts=new Vector<InetSocketAddress>();
			for(int j=0;j<noOfReplicas;j++){
				blockHosts.add(allFileHosts.elementAt(new Random().nextInt(noOfAvailableHosts)));
			}
			suitableFileHosts.add(blockHosts);
		}
		return suitableFileHosts;
	}
	
	public static Vector<InetSocketAddress> getSuitableFileHosts(int noOfHosts){
		Vector<InetSocketAddress> hosts=new Vector<InetSocketAddress>();
		int totalHosts=allFileHosts.size();
		for(int i=0;i<noOfHosts;i++)
			hosts.add(allFileHosts.elementAt(i%totalHosts));
		return hosts;
	}
}
