package dos.common.fs;
import dos.common.util.*;
import java.util.*;
public class FileSystemTree {

	Directory root;

	public FileSystemTree(Directory root) {
		this.root=root;
	}
	public FileSystemTree(Namespace rootNamespace){
		root=new Directory(rootNamespace);
	}
	public Directory findDirectory(Namespace resourcePath) throws NoSuchElementException{
		Namespace resource=resourcePath.stripPrefix(root.namespace().toString());
		StringTokenizer directoriesOnPath=resource.tokenize();
		Directory Iterator=root;
		while(directoriesOnPath.hasMoreTokens()){
			String subDirName=directoriesOnPath.nextToken();
			if(Iterator.hasSubDirectoryByName(subDirName))
				Iterator=Iterator.getSubDirectoryByName(subDirName);
			else
				throw new NoSuchElementException("DIRECTORY DOES NOT EXIST");				
		}
		return Iterator;
	}
	public Directory createNewDirectory(Namespace resourcePath){
		Namespace resource=resourcePath.stripPrefix(root.namespace().toString());
		StringTokenizer directoriesOnPath=resource.tokenize();
		Directory Iterator=root;
		String subDirName=null;
		while(directoriesOnPath.hasMoreTokens()){
			subDirName=directoriesOnPath.nextToken();
			if(Iterator.hasSubDirectoryByName(subDirName))
				Iterator=Iterator.getSubDirectoryByName(subDirName);
			else{
				Iterator=Iterator.addNewSubDirectoryEntry(subDirName);
				break;
			}
		}
		while(directoriesOnPath.hasMoreTokens()){
			subDirName=directoriesOnPath.nextToken();
			Iterator=Iterator.addNewSubDirectoryEntry(subDirName);
			}
		return Iterator;
	}

	
}
