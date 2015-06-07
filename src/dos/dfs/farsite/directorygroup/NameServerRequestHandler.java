package dos.dfs.farsite.directorygroup;

import dos.common.client.CommunicationTools;
import dos.common.fs.FileSystemTable;
import dos.common.fs.file.BlockId;
import dos.common.server.ReplyingServerRequestHandler;
import dos.common.util.Namespace;
import dos.common.util.Tools;
import dos.dfs.farsite.Config;
import dos.dfs.farsite.Messages;

import java.io.IOException;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.net.*;
import javax.naming.InsufficientResourcesException;
public class NameServerRequestHandler extends ReplyingServerRequestHandler {
	

	public NameServerRequestHandler(Socket connection) {
		super(connection);
	}
	String command;
	String params;
	long blockSize=Config.blockSize;
	boolean loadingACluster=false;
	public String replyToRequest(String request){
		String reply=Config._PARSE_ERROR_;
		try {
			Tools.print(request);
			command=Messages.extractCommand(request);
			params=Messages.extractParams(request);
			Tools.print(command);
			StringTokenizer paramsTokenizer=Messages.paramsTokenizer(params);
			if(command.equals(Messages.READ_METADATA)){
				Tools.print("entered READMETA part");
				reply=DirectoryManager.listMetadata(new Namespace(params));
				System.out.println("replying with "+reply);
			}
			else if(command.equals(Messages.CREATE_DIRECTORY)){
				Tools.print("Entered Nameserver at "+Long.toString(System.currentTimeMillis()));

				reply=DirectoryManager.createDirectory(new Namespace(params));
				if(!Config.singleNodeMode)
					if(!NameServer.consistencyMaintainer.proposeDirectoryCreation(new Namespace(params))){
						//what if proposed value was replaced during paxos
					}
			}
			else if(command.equals(Messages.CREATE_AND_GIVE_HOSTS_AND_ID)){

				Tools.print("Entered load part");
				Namespace namespace=new Namespace(paramsTokenizer.nextToken());
				long fileSize=Long.parseLong(paramsTokenizer.nextToken());
				int noOfChunks;
				if(fileSize%blockSize==0)
				noOfChunks=(int)(fileSize/blockSize);
				else
					noOfChunks=(int)(fileSize/blockSize)+1;
				Tools.print(noOfChunks);
				String dfsFileId=DirectoryManager.createFileEntryAndReturnDFSFileId(namespace, noOfChunks);
				Tools.print(dfsFileId);
				//Unreplicated loading
				/*
				reply=Messages.serializeFileIdAndVectorHosts(dfsFileId, FileHostsManager.getSuitableFileHosts(noOfChunks));
				*/
				//Replicated loading
				reply=Messages.serializeFileIdAndVectorVectorHosts(dfsFileId, FileHostsManager.getSuitableFileHostsAccountingForReplication(noOfChunks, Config._REPLICATION_FACTOR_));
			}
			else if(command.equals(Messages.DOWNLOAD_RESOURCE)){
				Namespace dfsfile=new Namespace(paramsTokenizer.nextToken());
				String dfsFileId=DirectoryManager.getDfsFileId(dfsfile);
				try {
					Vector<Vector<InetSocketAddress>> blockAddresses=DirectoryManager.blockAddresses(dfsfile);
					Tools.print("Got all block addresses "+Tools.serializeVectorVectorAddress(blockAddresses));
					reply=Messages.PROVIDE_FILE_ID_AND_REPLICATING_HOSTS(dfsFileId, blockAddresses);
				} catch (NoSuchElementException e) {
					reply=Messages.NO_SUCH_FILE_EXCEPTION;
					e.printStackTrace();
				}
				catch(InsufficientResourcesException i){
					reply=Messages.FILE_BLOCKS_MISSING_EXCEPTION;
					i.printStackTrace();
				}
			}
			else if(command.equals(Messages.LOADED_RESOURCE));
			else if(command.equals(Messages.LOADED_BLOCK)){
				Tools.print("Block loaded part");
				InetSocketAddress blockStorer=Tools.toInetSocketAddress(paramsTokenizer.nextToken());
				String dfsFileId=paramsTokenizer.nextToken();
				BlockId blockId=new BlockId(paramsTokenizer.nextToken());
				int inodeIndex=Integer.parseInt(paramsTokenizer.nextToken());
				long blockSize=Long.parseLong(paramsTokenizer.nextToken());
				DirectoryManager.addBlockReplicaToFile(blockStorer, dfsFileId, blockId, inodeIndex,blockSize);
				System.out.println("added block replica for file "+dfsFileId+" blockName: "+blockId.toString());
			}
			else if(command.equals(Messages.START_CLUSTER) ){
				if(Config.inStandByMode){
					if(Config.loadingACluster){
						reply=Messages.FAILED_TO_START_CLUSTER;
					}
					else{
						Config.loadingACluster=true;
						try {
							String rootNamespace=paramsTokenizer.nextToken();
							Config.CLUSTER_ROOT_NAMESPACE_=rootNamespace;
							Namespace.setRoot(rootNamespace);
							String serializedDirectory=paramsTokenizer.nextToken();
							Config.blockSize=Long.parseLong(paramsTokenizer.nextToken());
							Vector<InetSocketAddress> clusterHosts=Tools.getVectorInetSocketAddress(paramsTokenizer.nextToken());
							Config._INTRA_CLUSTER_ID_=paramsTokenizer.nextToken();
							InetSocketAddress myAddress=Tools.generateAddress(Config.clusterConsistencyPort);
							for(InetSocketAddress clusterMate:clusterHosts)
								if(!clusterMate.equals(myAddress))
									Config.clusterMates.add(clusterMate);
							Tools.print("My cluster mates are "+Tools.serializeVectorAddress(clusterHosts));
							if(Config.clusterMates.size()>0)
								NameServer.startConsistencyMaintainer();
							else
								Config.singleNodeMode=true;
							FileSystemTable fst=FileSystemTable.buildFileSystemTableFromStructure(serializedDirectory);
							DirectoryManager.setFileSystemTable(fst);
							DirectoryManager.resetBlockSize();
							reply=Messages.STARTED_CLUSTER;
							Config.inStandByMode=false;
						} catch (Exception e) {
							reply=Messages.FAILED_TO_START_CLUSTER;
							
							e.printStackTrace();
						}
						Config.loadingACluster=false;
					}
				}
				else{
					reply=Messages.ALREADY_RUNNING_A_CLUSTER;
				}
			}
		}  catch (Exception e) {
			Tools.print(e);
			e.printStackTrace();
		}
		return reply;
	}
	public void postReplyAction(){
		try {
			Tools.print("entered post Reply");
			StringTokenizer paramsTokenizer=Messages.paramsTokenizer(params);
			if(command.equals(Messages.READ_METADATA)){
				if(!Config.allOtherHosts.isEmpty())
					(new NamespaceMigrator(new Namespace(params))).migrate();
				else Tools.print("No one to offload to");
				/*
				Namespace dir=new Namespace(params);
				long noOfHits=DirectoryManager.getTotalHits(dir);
				System.out.println("No of hits is "+noOfHits);
				if(!dir.toString().equals(Config.CLUSTER_ROOT_NAMESPACE_))
					if(noOfHits>Config.MAX_HITS_PER_SUBTREE){
						Tools.print("Migration starting");
						Vector<InetSocketAddress> newClusterHosts=new Vector<InetSocketAddress>();
						String message=null;
						if(Config.singleNodeMode){
						newClusterHosts.add(Config.allOtherHosts.elementAt(0));
						String serializedSubDir=DirectoryManager.getReadyForMigrationAndSerializeSubDir(dir,newClusterHosts);
						message=serializedSubDir;
						}
						else{
							for(int i=0;i<Config.clusterMates.size();i++)
								newClusterHosts.add(Config.allOtherHosts.elementAt(i));
							
							String serializedSubDir=DirectoryManager.getReadyForMigrationAndSerializeSubDir(dir,newClusterHosts);

						}
						try {
							CommunicationTools.getRepliesOneByOne(newClusterHosts,message);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			*/
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}
}
