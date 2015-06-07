package dos.dfs.farsite.client;


import dos.common.client.*;
import dos.common.util.*;
import dos.dfs.farsite.*;

import java.util.*;

public class ClientDaemon extends STDIODaemon {
	Manager manager;
	FileManager fileManager;
	int fileHostListenerPort=Config.fileHostListenerPort;
	int fileHostPort=Config.fileHostPort;
	int fileDownloaderPort=Config.fileDownloaderPort;
	String FileStoragePath=Config._FARSITE_DEFAULT_DOWNLOAD_LOCATION_;
	long blockSize=Config.blockSize;
	boolean testingMode=false;
	ClientDaemon(){
		super(false);
		manager=new Manager();
	}
	public void switchToTestingMode(TextFileHandle input){
		testingMode=true;
		super.switchToTestingMode(input);
	}
	public void switchBackToNormalMode(){
		testingMode=false;
		super.switchBackToNormalMode();
	}
	ClientDaemon(boolean modeIsInteractive){
		super(modeIsInteractive);
		manager=new Manager();
		fileManager=new FileManager(fileHostListenerPort,fileHostPort,fileDownloaderPort,FileStoragePath,blockSize);
	}
	public void start(){
		super.start();
	}
	public void executeCommand(){
		try {
			String reply=null;
			String command=command();
			long startTime=System.currentTimeMillis();
			if(command.equals(Commands.READ_METADATA)){
				if(modifierParamPairs.isEmpty()){
					reply=manager.getMetadata(arguments.elementAt(0));
				}
			}
			else if(command.equals(Commands.CREATE_DIRECTORY))
				for(String arg:arguments)
					reply=manager.askDirectoryGroupToCreateDirectory(arg);
			
			else if(command.equals(Commands.LOAD_RESOURCE)){
				if(modifierParamPairs.isEmpty()){
					String localFilePath=arguments.elementAt(0);
					Namespace destinationNamespace=new Namespace(arguments.elementAt(1));
					String dfsFileIdAndHosts=manager.createFileEntry(localFilePath, destinationNamespace);
					//fileManager.load(localFilePath, Messages.getVectorHostsFromReply(dfsFileIdAndHosts), Messages.getDFSFileIdFromReply(dfsFileIdAndHosts),manager.getContactNodes(destinationNamespace));
					fileManager.loadWithReplication(localFilePath, Messages.getVectorVectorHostsFromReply(dfsFileIdAndHosts), Messages.getDFSFileIdFromReply(dfsFileIdAndHosts),manager.getContactNodes(destinationNamespace));
					reply="loaded";
				}
			}
			else if(command.equals(Commands.DOWNLOAD_RESOURCE)){
					Namespace dfsFilePath=new Namespace(arguments.elementAt(0));
					String localFilePath=arguments.elementAt(1);
					String Reply=manager.getHostsAndDfsFileIdToDownload(dfsFilePath);
					if(Reply.equals(Messages.NO_SUCH_FILE_EXCEPTION) || Reply.equals(Messages.FILE_BLOCKS_MISSING_EXCEPTION)){
						reply=Reply;
					}
					else{
						Tools.print(Reply);
						fileManager.download(Messages.extractDFSFileIdForDownloadFromReply(Reply), Messages.extractAddressesForDownloadFromReply(Reply), localFilePath);
						reply="downloaded";
					}
			}
			else if(command.equals(Commands.PERFORM)){
				Namespace dfsFilePath=new Namespace(arguments.lastElement());
				String localFilePath=Config._FARSITE_TEMPORARY_DOWNLOAD_LOCATION.concat(dfsFilePath.baseName());
				String Reply=manager.getHostsAndDfsFileIdToDownload(dfsFilePath);
				if(Reply.equals(Messages.NO_SUCH_FILE_EXCEPTION) || Reply.equals(Messages.FILE_BLOCKS_MISSING_EXCEPTION)){
					reply=Reply;
				}
				else{
					Tools.print(Reply);
					fileManager.download(Messages.extractDFSFileIdForDownloadFromReply(Reply), Messages.extractAddressesForDownloadFromReply(Reply), localFilePath);
					reply="downloaded";
				}
				String[] cmdargs=new String[arguments.size()];
				for(int i=0;i<arguments.size()-1;i++)
					cmdargs[i]=arguments.elementAt(i);
				cmdargs[arguments.size()-1]=localFilePath;
				ExternalCommandRunner.Run(cmdargs);
			}
			if(testingMode){
				Tools.print("TIME: "+Long.toString(System.currentTimeMillis()-startTime));
			}
			Tools.print(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printCommandPrompt(){
		System.out.print(Config._COMMAND_PROMPT_);
	}

}
