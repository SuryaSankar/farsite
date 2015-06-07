package dos.dfs.farsite.directorygroup;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeoutException;

import dos.common.client.CommunicationTools;
import dos.common.util.*;
import dos.dfs.farsite.Config;
import dos.dfs.farsite.Messages;
public class NamespaceMigrator {
	Namespace dir;
	Hashtable<InetSocketAddress,Integer> hostStatus=new Hashtable<InetSocketAddress, Integer>();
	public NamespaceMigrator(Namespace dir){
		this.dir=dir;
		for(InetSocketAddress host:Config.allOtherHosts)
			hostStatus.put(host, 0);//Indicates that the host can be used to transfer load
	}
	public  void migrate() throws Exception{
		long noOfHits=DirectoryManager.getTotalHits(dir);
		boolean failed=false;
		System.out.println("No of hits is "+noOfHits);
		if(!dir.toString().equals(Config.CLUSTER_ROOT_NAMESPACE_))
			if(noOfHits>Config.MAX_HITS_PER_SUBTREE){
				Tools.print("Migration starting");
				Vector<InetSocketAddress> newClusterHosts=new Vector<InetSocketAddress>();
				String message=null;
				String serializedSubDir=null;
				int nextClusterIndexToTry=0;
				Hashtable<InetSocketAddress,String> clusterIdsInformed=new Hashtable<InetSocketAddress, String>();
				if(Config.singleNodeMode){
					newClusterHosts.add(Config.allOtherHosts.elementAt(nextClusterIndexToTry));
					clusterIdsInformed.put(Config.allOtherHosts.elementAt(nextClusterIndexToTry),"000");

					nextClusterIndexToTry++;
					serializedSubDir=DirectoryManager.serializeSubDir(dir,newClusterHosts);
					message=serializedSubDir.concat(Messages.Params_Separator).concat(Tools.serializeVectorAddress(newClusterHosts));
					
				}
				else{
					while(nextClusterIndexToTry<Config.clusterMates.size()){
						newClusterHosts.add(Config.allOtherHosts.elementAt(nextClusterIndexToTry));
						if(nextClusterIndexToTry<10)
							clusterIdsInformed.put(Config.allOtherHosts.elementAt(nextClusterIndexToTry),"00".concat(Integer.toString(nextClusterIndexToTry)));
						else if(nextClusterIndexToTry<100)
							clusterIdsInformed.put(Config.allOtherHosts.elementAt(nextClusterIndexToTry),"0".concat(Integer.toString(nextClusterIndexToTry)));
						nextClusterIndexToTry++;
					}
					
					serializedSubDir=DirectoryManager.serializeSubDir(dir,newClusterHosts);
					message=serializedSubDir.concat(Messages.Params_Separator).concat(Tools.serializeVectorAddress(newClusterHosts));
				}
				Tools.print("sending migration msg "+message+" to "+Tools.serializeVectorAddress(newClusterHosts));
				try {
					Hashtable<InetSocketAddress, String> replies =new Hashtable<InetSocketAddress, String>();
					 for(InetSocketAddress receiver:newClusterHosts){
							try {
								message=message.concat(Messages.Params_Separator).concat(clusterIdsInformed.get(receiver));
								replies.put(receiver, CommunicationTools.queryAndBlockForReply(receiver, message));
							} catch (Exception e) {
								replies.put(receiver,Messages.FAILED_TO_START_CLUSTER);
							}	
					 }
					 Vector<InetSocketAddress> actualClusterHosts=new Vector<InetSocketAddress>();
					 for(int j=0;j<newClusterHosts.size();j++){
						 String reply=replies.get(newClusterHosts.elementAt(j));
						 if(reply.equals(Messages.ALREADY_RUNNING_A_CLUSTER) || reply.equals(Messages.FAILED_TO_START_CLUSTER)){
								if(reply.equals(Messages.ALREADY_RUNNING_A_CLUSTER)){
									hostStatus.put(Config.allOtherHosts.elementAt(j), 1);
								}
								String clusterId=clusterIdsInformed.get(newClusterHosts.elementAt(j));
								newClusterHosts.remove(j);
								Tools.print(serializedSubDir);
								String newMessage=serializedSubDir.concat(Messages.Params_Separator).concat(clusterId);
								while(nextClusterIndexToTry<Config.allOtherHosts.size()){
									try {
										InetSocketAddress replacer=Config.allOtherHosts.elementAt(nextClusterIndexToTry);
										String newReply=CommunicationTools.queryAndBlockForReply(replacer, newMessage);
										if(newReply.equals(Messages.STARTED_CLUSTER)){
											actualClusterHosts.insertElementAt(replacer, j);
											break;
										}
										else if(newReply.equals(Messages.ALREADY_RUNNING_A_CLUSTER))
											hostStatus.put(Config.allOtherHosts.elementAt(nextClusterIndexToTry), 1);
									} catch (TimeoutException e) {}			
									nextClusterIndexToTry++;
								}
								if(nextClusterIndexToTry==Config.allOtherHosts.size())
									failed=true;
						 }
						else if(reply.equals(Messages.STARTED_CLUSTER)){
								hostStatus.put(Config.allOtherHosts.elementAt(j), 1);
								actualClusterHosts.insertElementAt(newClusterHosts.elementAt(j), j);
							}
						 
					 }
					 
					Enumeration<InetSocketAddress> hosts=hostStatus.keys();
					while(hosts.hasMoreElements()){
							InetSocketAddress host=hosts.nextElement();
							int status=hostStatus.get(host);
							if(status==1)
								Config.allOtherHosts.remove(host);
					}

					if(failed)
						throw new Exception("could Not Migrate");
					else DirectoryManager.noteMigration(dir, newClusterHosts);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	
	
	}
}
