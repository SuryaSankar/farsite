package dos.common.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import dos.common.util.*;


public abstract class ReplyingServer extends Thread {

    ServerSocket ListeningSocket;
    Socket incomingConnection;
    int listeningPort;
    public ReplyingServer(int listeningPort){
    	System.out.println("Replying server starting at "+listeningPort);
    	this.listeningPort=listeningPort;
    }
    public void boot() throws Exception{
            ListeningSocket=new ServerSocket(listeningPort);
            super.start();
    }

    public void run(){
                  try{
                     while(true){
                         incomingConnection=ListeningSocket.accept();
                         Tools.print("accepted");                         
                         ReplyingServerRequestHandler requestHandler=provideRequestHandler(incomingConnection);
                         Thread requestHandlerInstance=new Thread(requestHandler);
                         requestHandlerInstance.start();
                        }
                    }
                    catch(Exception e){
                        System.out.println("Caught Exception while running ReplyingServer "+e);
                    }
    }

public abstract ReplyingServerRequestHandler provideRequestHandler(Socket incomingConnection) throws Exception;



    public void shutdown(){
    	while(!ListeningSocket.isClosed())
	    	try {
				ListeningSocket.close();
			} catch (IOException e) {
				Tools.print("IOException caught while closing. Trying again..");
				e.printStackTrace();
			}
    }



}
