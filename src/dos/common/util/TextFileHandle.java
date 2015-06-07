package dos.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * A wrapper class over java Character File Stream.
 *
 */

public class TextFileHandle {
	String filename;
	BufferedWriter out;
	BufferedReader in;
	boolean openedForRead=false;
	boolean openedForWrite=false;
	public TextFileHandle(String name){
			filename=name;
	}
	public void open(){
		this.open(false);
	}
	public void open(boolean append){
		try {
			out=new BufferedWriter(new FileWriter(new File(filename),append));
			openedForWrite=true;
			in=new BufferedReader(new FileReader(filename));
			openedForRead=true;
		} catch (FileNotFoundException e) {
			Tools.print("No such file");
			e.printStackTrace();
		} catch (IOException e) {
			Tools.print("Caught IOException");
			e.printStackTrace();
		}
	}
	public void openForWrite(boolean append){
		try {
			out=new BufferedWriter(new FileWriter(filename,append));
			openedForWrite=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void openForRead() throws FileNotFoundException{

			in=new BufferedReader(new FileReader(filename));
			openedForRead=true;

	}
	public void writeline(String str) throws IOException{

			out.write(str);
			out.newLine();

	}
	public void write(String str) throws IOException{
			out.write(str);

	}
	public String readLine() throws IOException{

			return in.readLine();

	}

	public void closeIn() throws IOException{

			in.close();
			openedForRead=false;

	}
	public void closeOut() throws IOException{

			out.close();
			openedForWrite=false;

	}
	public void close() throws IOException{

			in.close();
			openedForRead=false;
			out.close();
			openedForWrite=false;

	}
	
	public void reset() throws IOException{
		in.reset();
	}
	public void mark(int readAheadLimit ) throws IOException{
		in.mark(readAheadLimit);
	}
	public boolean isOpenForRead(){
		return openedForRead;
	}
	public boolean isOpenForWrite(){
		return openedForWrite;
	}
	public boolean isEmpty() throws IOException{
		if(!this.isOpenForRead())
			this.openForRead();
		int firstChar=0;

			in.mark(1);
			firstChar=in.read();
			in.reset();

		if(firstChar==-1)
			return true;
		else return false;
		
	}
	public boolean notFound(){
		
			try {
				this.openForRead();
				return false;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				return true;
			}
		
	}
	public void print() throws IOException{
		if(!this.isOpenForRead())
			this.openForRead();
		for(String line=this.readLine();line!=null;line=this.readLine())
			Tools.print(line);
	}
	public long size() throws FileNotFoundException, IOException{
		return new FileInputStream(filename).getChannel().size();
	}
	
	public static long size(String filename) throws FileNotFoundException, IOException{
		return new FileInputStream(filename).getChannel().size();
	}
	public static void main(String args[]){
		TextFileHandle test=new TextFileHandle("./lp.log");
		try {
			if(test.isEmpty())
				Tools.print("File is Empty");
			else
				test.print();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Tools.print(size("./lp.log"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}



