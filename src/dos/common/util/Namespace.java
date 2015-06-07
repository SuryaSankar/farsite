package dos.common.util;
import java.util.*;
/**
 * Namespace is the uri schema of the filesystem. This provides unique ness to 
 * file system resources
 */
public class Namespace {

	String fullPath;
	static String delimiter="/";
	static Namespace NULL=new Namespace("__NULL_NAMESPACE__");
	protected static String root="/";
	public Namespace(String path) {
		fullPath=path;
	}

	public static void setRoot(String Root){
		root=Root;
	}
	public Namespace root(){
		if(this.isRoot())
			return this;
		return new Namespace(root);
	}
	public String toString(){
		return fullPath;
	}
	public String baseName(){
		return fullPath.substring(fullPath.lastIndexOf(delimiter)+1);
	}
	public static String baseName(String namespaceString){
		return namespaceString.substring(namespaceString.lastIndexOf(delimiter)+1);
	}

	public Namespace parent(){
		if(this.isRoot()||this.isNull())
			return NULL;
		String parent=fullPath.substring(0, fullPath.lastIndexOf(delimiter));
		return new Namespace(parent);
	}

	public Namespace stripPrefix(String prefix){
		return new Namespace(fullPath.substring(prefix.length()));
	}

	public StringTokenizer tokenize(){
		return new StringTokenizer(fullPath,Namespace.delimiter);
	}
	public boolean equals(Namespace another){
		return (this.fullPath.equals(another.fullPath));
	}
	
	public int hashCode(){
		return this.fullPath.hashCode();
	}
	public boolean isNull(){
		return this.equals(NULL);
	}
	public boolean isRoot(){
		return (fullPath.equals(root));
	}
	public String delimiter(){
		return delimiter;
	}
	
	public Namespace createChild(String name){
		return new Namespace(fullPath.concat(delimiter).concat(name));
	}
	public static void main(String args[]){
		Namespace name=new Namespace("/cluster/user/surya");
		Namespace subname=name.stripPrefix("/cluster");
		Tools.print(subname);
		Tools.print(name.toString());
		Tools.print(name.root().toString());
		Tools.print(name.baseName());
		Tools.print(name.parent().toString());
		Namespace dir=name.createChild("dir");
		Tools.print(dir.toString());

	}

}
