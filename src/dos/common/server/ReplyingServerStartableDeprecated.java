package dos.common.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import dos.common.Parameters;
import dos.common.util.Tools;

public abstract class ReplyingServerStartableDeprecated extends Thread {
    /*
     * ActiveListener  listens to incoming connections and creates a RequestHandler thread for each request which will return a reply.
     * It is a thread because it should be forever alive
     * replyToRequest is an abstract method for the caller to override. 
     * 
     * Lets user call start method appropriately
     */
    ServerSocket ListeningSocket;
    Socket incomingConnection;
    public ReplyingServerStartableDeprecated(int listeningPort){
        try{
            ListeningSocket=new ServerSocket(listeningPort);
        }
        catch(Exception e){
            System.out.println("Caught Exception while instantiating ActiveListener "+e);
        }
    }
    

    public void run(){
                  try{
                     while(true){
                         incomingConnection=ListeningSocket.accept();
                         RequestHandler requestHandler=new RequestHandler(incomingConnection);
                         Thread requestHandlerInstance=new Thread(requestHandler);
                         requestHandlerInstance.start();
                        }
                    }
                    catch(Exception e){
                        System.out.println("Caught Exception while running ActiveListener "+e);
                    }
    }

    class RequestHandler implements Runnable{
        /*
         * This is a runnable that handles each request. This is a runnable because requests keep coming in and simultaneous handling is required
         */
         Socket connection;
         RequestHandler(Socket socket){
             connection=socket;
         }
        public void run(){
            try{
                BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                PrintStream out = new PrintStream(connection.getOutputStream());
                while(!in.ready()) Thread.sleep(Parameters.THREAD_SLEEP_TIME);
                String request=in.readLine();
                String reply=replyToRequest(request);
                out.println(reply);
                postReplyAction(request);
            }
            catch(Exception e){

            }
        }
    }

    public abstract String replyToRequest(String request);
    public void postReplyAction(String request){
    	//Inheriting classes can over ride this to do something after replying
    }
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
