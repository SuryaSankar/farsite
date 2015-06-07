package dos.common.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import dos.common.util.*;

public abstract class STDIODaemon extends Thread {

	String command,parameters;
	boolean paused=false;
	boolean modeIsInteractive;
	boolean inputFromFile=false;
	TextFileHandle inputFile;
	protected Hashtable<String, String> modifierParamPairs=new Hashtable<String, String>();
	protected Vector<String> arguments=new Vector<String>();
	boolean switchbackToNormalModeAfterTest=false;
	public STDIODaemon(boolean modeIsInteractive){
		this.modeIsInteractive=modeIsInteractive;
	}
	public void switchToTestingMode(TextFileHandle input){
		inputFromFile=true;
		inputFile=input;
		try {
			inputFile.openForRead();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			switchBackToNormalMode();
		}
	}
	public void switchBackToNormalMode(){
		try {
			inputFile.closeIn();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tools.print("switching back to normal mode");
		inputFromFile=false;
	}
	
	public void printCommandPrompt(){}
	public abstract void executeCommand();
	public void pause(){
		paused=true;
	}
	public void unpause(){
		paused=false;
	}
	public String command(){
		return command;
	}

	public Hashtable<String, String> modifierParams(){
		return modifierParamPairs;
	}
	public String getParam(String modifier){
		return modifierParamPairs.get(modifier);
	}
	public Vector<String> arguments(){
		return arguments;
	}

	
	public void run(){
		while(true){
			modifierParamPairs=new Hashtable<String, String>();
			arguments=new Vector<String>();
			String line=null;
			if(!paused){
					if(inputFromFile){
						try {
							line=inputFile.readLine();
							Tools.print("OPERATION: "+line);
							if(line==null){
								Tools.print("Test Over");
								if(switchbackToNormalModeAfterTest){
									switchBackToNormalMode();
									continue;
								}
								else break;
							}
						} catch (FileNotFoundException e) {
								e.printStackTrace();
								switchBackToNormalMode();
								continue;
						} catch (IOException e) {
								e.printStackTrace();
								switchBackToNormalMode();
								continue;
						}
					}
					else {
						if(modeIsInteractive)
							printCommandPrompt();
						try {
							BufferedReader reader=new BufferedReader( new InputStreamReader(System.in));
							line=reader.readLine();
						} catch (IOException e) {
								System.out.println("Caught exception at STDIODaemon"+e);
								e.printStackTrace();
						}
					}
					StringTokenizer tokenizer=new StringTokenizer(line," ");
					command=tokenizer.nextToken().trim();
					while(tokenizer.hasMoreTokens()){
						String token=tokenizer.nextToken().trim();
						if (token.charAt(0)=='-' && token.charAt(1)=='-'){
							String modifier=token.substring(2);
							String param=tokenizer.nextToken().trim();
							modifierParamPairs.put(modifier, param);
						}
						else{
							arguments.add(token);
						}
					}
					executeCommand();
				}//if(!paused)
			}//while true
		}
	public static void main(String args[]){
		while(true)
		try {
			Tools.print("Enter ");
			BufferedReader br=new BufferedReader( new InputStreamReader(System.in));
			System.out.println();
			String line=br.readLine();
			StringTokenizer tokenizer=new StringTokenizer(line," ");
			String command=tokenizer.nextToken().trim();
			Hashtable<String, String> modifierParamPairs=new Hashtable<String, String>();
			Vector<String> arguments=new Vector<String>();
			while(tokenizer.hasMoreTokens()){
				String token=tokenizer.nextToken().trim();
				if (token.charAt(0)=='-'){
					String modifier=token.substring(1);
					String param=tokenizer.nextToken().trim();
					modifierParamPairs.put(modifier, param);
				}
				else{
					arguments.add(token);
				}
					
			}
			Enumeration<String> params=modifierParamPairs.keys();
			Tools.print(command);
			while(params.hasMoreElements()){
				String param=params.nextElement();
				System.out.println(param+" - "+modifierParamPairs.get(param));
			}
			for(String arg: arguments){
				Tools.print(arg);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
