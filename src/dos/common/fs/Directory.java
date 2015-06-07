package dos.common.fs;
import dos.common.users.*;
import dos.common.util.*;

import java.util.*;
public class Directory   {
	/**
	 * associatedNamespace - the full path of the directory- this is a namespace object and provides
	 * 				 the directory a sense of location
	 */
	Namespace associatedNamespace;
	String name;
	Hashtable<String,Directory> subDirTable;
	Hashtable<String,FileInodeTable> fileTable;
 	Owner owner;
	Group group;
	long noOfHitsTillNow;
	int noOfSubdirs;
	int noOfFiles;

	public Directory(Namespace associatedNamespace){
		this.associatedNamespace=associatedNamespace;
		name=this.associatedNamespace.baseName();
		subDirTable=new Hashtable<String, Directory>();
		fileTable=new Hashtable<String, FileInodeTable>();
		noOfHitsTillNow=1;
		noOfSubdirs=0;
		noOfFiles=0;
	}
	public void setOwner(Owner owner){
		this.owner=owner;
	}
	public void setGroup(Group group){
		this.group=group;
	}


	
	void addSubDirectoryEntry(String name,Directory subDir){
		subDirTable.put(name, subDir);
	}
	public void addFileEntry(String fileName,FileInodeTable file){
		fileTable.put(fileName, file);
	}
	public Directory addNewSubDirectoryEntry(String subDirName){
		Namespace subDirNamespace=associatedNamespace.createChild(subDirName);
		noOfSubdirs++;
		Directory subDir=new Directory(subDirNamespace);
		addSubDirectoryEntry(subDirName,subDir);
		return subDir;
	}

	public FileInodeTable addNewFileEntry(String fileName,int noOfInodes){
		Namespace fileNamespace=associatedNamespace.createChild(fileName);
		noOfFiles++;
		FileInodeTable file=new FileInodeTable(fileNamespace,noOfInodes);
		addFileEntry(fileName, file);
		return file;
	}
	public int noOfImmediateSubDirs(){
		return noOfSubdirs;
	}
	public int noOfImmediateSubFiles(){
		return noOfFiles;
	}
	public boolean isEmpty(){
		return noOfFiles==0 && noOfSubdirs==0;
	}

    public boolean hasSameNamespaceAs(Directory another){
    	return this.associatedNamespace.equals(another.associatedNamespace);
    }
    public boolean hasSameNameAs(Directory another){
    	return this.name().equals(another.name());
    }
    public boolean hasSubDirectoryByName(String subDirName){
    	return subDirTable.containsKey(subDirName);
    }
    public Directory getSubDirectoryByName(String subDirName){
    	return subDirTable.get(subDirName);
    }
    public FileInodeTable getFileByName(String fileName){
    	return fileTable.get(fileName);
    }
	public String name(){
		return name;
	}
	public Namespace namespace(){
		return associatedNamespace;
	}
	public Enumeration<String> subDirs(){
		return subDirTable.keys();
	}
	public Enumeration<String> subFiles(){
		return fileTable.keys();
	}
	public void recursivelyListSubDirs(){
		Tools.print(associatedNamespace);
		if(noOfImmediateSubDirs()!=0){
		Enumeration<String> subDirs=subDirTable.keys();
		while(subDirs.hasMoreElements())
			subDirTable.get(subDirs.nextElement()).recursivelyListSubDirs();
		}
	}
	public String recursivelySerialize(){
		String delim=FSParams.subDirSerializationdelim;
		String returnString=associatedNamespace.toString().concat(delim);
		if(noOfImmediateSubDirs()!=0){
			Enumeration<String> subDirs=subDirTable.keys();
			while(subDirs.hasMoreElements())
				returnString=returnString.concat(subDirTable.get(subDirs.nextElement()).recursivelySerialize());
			}
		return returnString;
	}
	public String serializeFiles(){
		String returnString="";

			Enumeration<String> files=fileTable.keys();
			while(files.hasMoreElements()){
				FileInodeTable fit=fileTable.get(files.nextElement());
				returnString=returnString.concat(fit.namespace().toString()).concat(FSParams.namespaceFileSeparator).concat(fit.serialize().concat(FSParams.fileSeparator));
			}
	

			Enumeration<String> subDirs=subDirTable.keys();
			while(subDirs.hasMoreElements())
				returnString=returnString.concat(subDirTable.get(subDirs.nextElement()).serializeFiles());
	

		return returnString;
	}
	public String serialize(){
		return this.recursivelySerialize().concat(FSParams.fileSeparator).concat(this.serializeFiles());
	}
	public static Directory rebuildDirectoryStructure(String string){
		Hashtable<String,Directory> createdDirs=new Hashtable<String, Directory>();
		Hashtable<String,FileInodeTable> createdFiles=new Hashtable<String, FileInodeTable>();
		StringTokenizer Tokenizer=new StringTokenizer(string,FSParams.fileSeparator);
		String subDirString=Tokenizer.nextToken();
		StringTokenizer tokenizer=new StringTokenizer(subDirString,FSParams.subDirSerializationdelim);
		String rootNamespaceString=tokenizer.nextToken();
		Namespace rootNamespace=new Namespace(rootNamespaceString);
		Directory root=new Directory(rootNamespace);
		createdDirs.put(rootNamespaceString,root);
		while(tokenizer.hasMoreTokens()){
			String subDirNamespace=tokenizer.nextToken();
			String parentNamespace=new Namespace(subDirNamespace).parent().toString();
			Directory subDir=createdDirs.get(parentNamespace).addNewSubDirectoryEntry(Namespace.baseName(subDirNamespace));
			createdDirs.put(subDirNamespace, subDir);
		}
		/*
		while(Tokenizer.hasMoreTokens()){
			String token=Tokenizer.nextToken();
			StringTokenizer tokenizer2=new StringTokenizer(token,FSParams.subDirSerializationdelim);
			String subDirNamespace=tokenizer2.nextToken();
			Tools.print(subDirNamespace);
		}*/
		return root;
	}
	public void listSubDirs(){
		Enumeration<String> subDirs=subDirTable.keys();
		while(subDirs.hasMoreElements()){
			Tools.print(subDirs.nextElement());
		}
	}
	public void listFiles(){
		Enumeration<String> files=fileTable.keys();
		while(files.hasMoreElements()){
			Tools.print(files.nextElement());
		}
	}
	public void listAll(){
		Tools.print("Directories:");
		listSubDirs();
		Tools.print("Files");
		listFiles();
	}

	public void createFile(String name){
		
	}
	public static void main(String args[]){
		Directory root=new Directory(new Namespace("root"));
		
		root.addNewSubDirectoryEntry("dir1");
		root.addNewSubDirectoryEntry("dir2");
		root.addNewSubDirectoryEntry("dir3");
		root.getSubDirectoryByName("dir1").addNewSubDirectoryEntry("dir1");
		root.getSubDirectoryByName("dir1").addNewFileEntry("firstFile", 10);
		root.getSubDirectoryByName("dir2").addNewSubDirectoryEntry("dir1");
		root.getSubDirectoryByName("dir1").getSubDirectoryByName("dir1").addNewSubDirectoryEntry("dir1");
		root.getSubDirectoryByName("dir1").getSubDirectoryByName("dir1").addNewFileEntry("secondFile", 20);
		root.getSubDirectoryByName("dir1").getSubDirectoryByName("dir1").getSubDirectoryByName("dir1").addNewFileEntry("3rdFile", 20);
		String ds=root.serialize();
		//root.listAll();
		//Tools.print(root.serializeFiles());
		Tools.print(root.serialize());
		Directory newRoot=Directory.rebuildDirectoryStructure(ds);
		Tools.print("printing newRoot");
		newRoot.serialize();
	}
	
}
