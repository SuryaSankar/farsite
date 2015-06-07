package dos.common.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import dos.common.Parameters;


public abstract class NonReplyingServer extends Thread {

	/*
     * PassiveListener just listens to incoming connections and creates a RequestHandler thread for each request.
     * It is a thread because it should be forever alive
     * handleRequest is an abstract method for the caller to override. Since it doesn't return anything to the caller we call it passive
     */
    ServerSocket ListeningSocket;
    Socket incomingConnection;
    public NonReplyingServer(int listeningPort){
        try{
            ListeningSocket=new ServerSocket(listeningPort);
            start();
        }
        catch(Exception e){
            System.out.println("Caught Exception while instantiating PassiveListener "+e);
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
                        System.out.println("Caught Exception while instantiating PassiveListener "+e);
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
                while(!in.ready()) Thread.sleep(Parameters.THREAD_SLEEP_TIME);
                String request=in.readLine();
                handleRequest(request);
            }
            catch(Exception e){

            }
        }
    }

    public abstract void handleRequest(String request);
    


}
