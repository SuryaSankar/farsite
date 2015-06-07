package dos.common.util;

import java.net.InetSocketAddress;
import java.util.Vector;

import dos.common.client.CommunicationTools;


public class PulseTicker implements Runnable {


    String Pulse;
    int PulseBeatInterval;
    Vector<InetSocketAddress> Doctors;
    public void configurePulse(String pulse,int pulseBeatInterval){
    	Pulse=pulse;
    	PulseBeatInterval=pulseBeatInterval;
    }
    public void setDoctors(Vector<InetSocketAddress> doctors){
    	Doctors=doctors;
    }


    public void run(){
        try{
        Thread.sleep(PulseBeatInterval);
        CommunicationTools.informOneByOne(Doctors, Pulse);
        }
        catch(Exception e){
            System.out.println("Caught Exception in Heart "+e);
        }
    } 


}
