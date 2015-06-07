package dos.common.util;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Vector;

import dos.common.server.*;

public abstract  class PulseMonitor extends NonReplyingServer implements Runnable
{
           /**
            * Abstract Runnable that extends the abstract PassiveListener in ConnectionHandler. Overrides its abstract method handleRequest
            * Provides a abstract method handleDeath for the caller to override
            */
           Vector<InetSocketAddress> NodesToCheck;
           Hashtable<String,Long> lastRecordedPulseTable=new Hashtable<String,Long>();
           static int ListeningPort;
           static int DeathCount;
           static int PulseBeatInterval;
           public  static void configure(int listeningPort,int deathCount,int pulseBeatInterval){
        	   ListeningPort=listeningPort;
        	   DeathCount=deathCount;
        	   PulseBeatInterval=pulseBeatInterval;
           }
           public void setNodesToMonitor(Vector<InetSocketAddress> nodesToCheck){
        	   NodesToCheck=nodesToCheck;
           }
           PulseMonitor(){
              super(ListeningPort);
           }


           public void handleRequest(String node) {
           if(NodesToCheck.contains(node))
               lastRecordedPulseTable.put(node, Long.valueOf(System.currentTimeMillis()));
           }

           public void run(){
               while(true){
                   for(InetSocketAddress node:NodesToCheck){
                       long lastRecordedPulse=lastRecordedPulseTable.get(node);
                       int noOfMissedPulses=(int)((System.currentTimeMillis()-lastRecordedPulse)/PulseBeatInterval);
                       if(noOfMissedPulses>DeathCount)
                           handleDeath(node);                            
                   }
               }
           }

           abstract void handleDeath(InetSocketAddress node);


           }




