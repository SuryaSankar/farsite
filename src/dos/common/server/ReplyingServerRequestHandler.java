package dos.common.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import dos.common.Parameters;
import dos.common.util.Tools;

public abstract class ReplyingServerRequestHandler implements Runnable {

    /*
     * This is a runnable that handles each request. This is a runnable because requests keep coming in and simultaneous handling is required
     */
     Socket connection;
     public ReplyingServerRequestHandler(Socket socket){
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
            Tools.print("Replying server printed at "+Long.toString(System.currentTimeMillis()));
            postReplyAction();
        }
        catch(Exception e){

        }
        
    }
    public abstract String replyToRequest(String request) throws Exception;
    public void postReplyAction() throws Exception{
    	//Inheriting classes can over ride this to do something after replying
    }

}
