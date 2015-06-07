package dos.common.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.concurrent.TimeoutException;

import dos.common.Parameters;


public class RWChannel {
	//Establishes a 2 way communication channel with the specified destination
	Socket connection = null;
    BufferedReader in;
    PrintStream out;
	String reply;
	long sentTimeStamp;
	int count = 0;
	boolean connected=false;
	InetSocketAddress destinationAddress;
	public RWChannel(InetSocketAddress destinationInetSocketAddress) throws IOException{
        	this.destinationAddress=destinationInetSocketAddress;
        	this.open();

	}
	public RWChannel(InetSocketAddress destinationInetSocketAddress,boolean writeOnly) throws IOException{
        	this.destinationAddress=destinationInetSocketAddress;
        	this.open(writeOnly);
   	}
	public void open(boolean writeOnly) throws IOException{
			while(!connected&&count < Parameters.MAX_CONN_ATTEMPTS){
			    try{
			        connection = new Socket(destinationAddress.getAddress(), destinationAddress.getPort());
			        connected=true;
			    }
			    catch(Exception e){
			        count++;
			    }
			}
			if(connected){
				out=new PrintStream(connection.getOutputStream());
				if(!writeOnly)
					in= new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			else throw new ConnectException("Failed to open Channel");
		
	}
	public void open() throws IOException{
		open(false);
	}
	public void write(String message){//will always write with a from InetSocketAddress address at front
		out.println(message);
	}
	
	public String read() throws TimeoutException, IOException, InterruptedException{   		
				sentTimeStamp = System.currentTimeMillis();
				while(!(in.ready())){
					if((System.currentTimeMillis() - sentTimeStamp)	> 30000)
						throw new TimeoutException();
				    Thread.sleep(Parameters.THREAD_SLEEP_TIME);
				}
				return in.readLine();
	}

	public void close() throws IOException{

			connection.close();
			connected=false;

	}
	public static void main(String args[]) throws Exception{
		RWChannel channel=new RWChannel(Tools.toInetSocketAddress("/10.6.9.18:10009"));
		channel.open();
		channel.write("hi");
		
	}


}
