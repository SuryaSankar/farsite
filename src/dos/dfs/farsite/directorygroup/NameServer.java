package dos.dfs.farsite.directorygroup;

import java.io.IOException;

import dos.common.util.Tools;
import dos.dfs.farsite.Config;

public class NameServer {
	public static ConsistencyMaintainer consistencyMaintainer;
	public static void startConsistencyMaintainer(){
		try {
			System.out.println("starting consistency maintainer with id "+Config._INTRA_CLUSTER_ID_);
			consistencyMaintainer=new ConsistencyMaintainer(Config._INTRA_CLUSTER_ID_,"ns".concat(Config.CLUSTER_ROOT_NAMESPACE_.replace('/', '_')).concat(Config._INTRA_CLUSTER_ID_),Config.clusterMates,Config.clusterConsistencyPort);

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
