package dos.common.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dos.common.Parameters;
import dos.common.util.Envelope;
import dos.common.util.RWChannel;
import dos.common.util.Tools;


public class CommunicationTools {

	
	public static String queryAndBlockForReply(InetSocketAddress destination,String query) throws TimeoutException, IOException, InterruptedException{
		RWChannel rwchannel=new RWChannel(destination,false);
		rwchannel.write(query);
		String reply=rwchannel.read();
		System.out.println("query and block got reply at "+Long.toString(System.currentTimeMillis()));
		return reply;
	}
	
	public static String tryToGetReplyFromOne(Vector<InetSocketAddress> contactNodes,String query) throws ConnectException {
		for (InetSocketAddress node:contactNodes){
			try {
				String reply=queryAndBlockForReply(node, query);
				
				return reply;
			} catch (Exception e) {
				continue;
			}
		}
		throw new ConnectException("Could not connect to any");
	}
	public static Envelope tryToGetEnvelopeFromOne(Vector<InetSocketAddress> contactNodes,String query) throws IOException, InterruptedException{
		String reply=null;
		for (InetSocketAddress node:contactNodes){
			try {
				reply=queryAndBlockForReply(node, query);
				return new Envelope(node,reply);
			} catch (Exception e) {
				continue;
			}
		}
		throw new ConnectException("Could not connect to any");
	}

	public static void inform(InetSocketAddress destination,String message) throws IOException{
		RWChannel wchannel=new RWChannel(destination,true);
		wchannel.write(message);
		wchannel.close();
	}
	public static void informOneByOne(Vector<InetSocketAddress> receivers,String message) throws IOException{
		for(InetSocketAddress receiver:receivers)
			inform(receiver,message);		
	}
	public static InetSocketAddress tryToInformOne(Vector<InetSocketAddress> receivers,String message) throws IOException{
		for (InetSocketAddress receiver:receivers){
			try {
				inform(receiver,message);
				return receiver;
			} catch (Exception e) {
				continue;
			}
		}
		throw new ConnectException("Could not connect to any");
	}
	public static Hashtable<InetSocketAddress,String> getRepliesOneByOne(Vector<InetSocketAddress> receivers,String message) throws IOException, InterruptedException{
		 Hashtable<InetSocketAddress,String> replies=new Hashtable<InetSocketAddress, String>();
		 for(InetSocketAddress receiver:receivers){
				try {
					replies.put(receiver, queryAndBlockForReply(receiver, message));
					return replies;
				} catch (Exception e) {
					continue;
				}	
		 }
		 throw new ConnectException("Could not connect to any");
	}
	


	public abstract static class CallableQueriesAllParticipantsAndMakesDecison implements Callable<String>{
		Vector<InetSocketAddress> servers;
		String query;
		String decision=null;
		boolean getNextReply=true;
		ExecutorService pool;
		public CallableQueriesAllParticipantsAndMakesDecison(Vector<InetSocketAddress> servers,String query){
			this.servers=servers;
			this.query=query;

		}

		public String call() throws IOException{		
			   pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
	    	   contactAllAndGetReplies(servers, query);
		       evaluateRepliesAndMakeDecision();
		       pool.shutdownNow();
		       return decision;
			
		}

		void contactAllAndGetReplies(Vector<InetSocketAddress> servers,String query) {
				Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
		        for (InetSocketAddress server:servers){
		    	   QueryThread queryThread;
				try {
					queryThread = new QueryThread(server,query);
			    	   Future<String> promisedReply=pool.submit(queryThread);
			    	   promisedReplies.put(server, promisedReply);
				} catch (IOException e) {
					System.out.println("Could not open connection to "+server.toString());
				}

		       }
		       try {
				Thread.sleep(Parameters.SERVER_LATENCY);
		       } catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		       }
		       for (InetSocketAddress server:servers){
		    	   		if(!promisedReplies.containsKey(server))
		    	   			continue;
			    	   Future<String> promisedReply=promisedReplies.get(server);
			    	   
			    		   Tools.print("looking at promised reply");
			    		   String reply=null;
						try {
							reply = promisedReply.get(Parameters.RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							Tools.print("InterruptedException");
							e.printStackTrace();
						} catch (ExecutionException e) {
							Tools.print("ExecutionException");
							e.printStackTrace();
						} catch (TimeoutException e) {
							Tools.print("TimeoutException");
				    	     promisedReply.cancel(true);
				    	     Tools.print("cancelled reply");
				             
							e.printStackTrace();
							continue;
						}
			    		   handleReply(reply);

			    	   if(!getNextReply) break;
		       }
	       
			
		}

		public abstract void evaluateRepliesAndMakeDecision();
		public void makeDecisionHalfWay(String decision){
			this.decision=decision;
			getNextReply=false;
			pool.shutdownNow();
		}
		public void makeDecision(String decision){
			this.decision=decision;
		}
		public void stopGettingReplies(){
			getNextReply=false;
		}
		public abstract void handleReply(String reply);
				
	}
	
	public abstract static class CallableQueriesEachParticipantUniquelyAndMakesDecison implements Callable<String>{
		Hashtable<InetSocketAddress,String> serversNQueries;
		String decision=null;
		boolean getNextReply=true;
		ExecutorService pool;
		public CallableQueriesEachParticipantUniquelyAndMakesDecison(Hashtable<InetSocketAddress,String> serversNQueries){
			this.serversNQueries=serversNQueries;

		}

		public String call() throws IOException{		
			   pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
	    	   contactAllAndGetReplies(serversNQueries);
		       evaluateRepliesAndMakeDecision();
		       pool.shutdownNow();
		       return decision;
			
		}

		void contactAllAndGetReplies(Hashtable<InetSocketAddress,String> serversNQueries) {
				Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
				Enumeration<InetSocketAddress> servers=serversNQueries.keys();
		        while(servers.hasMoreElements()){
		        	InetSocketAddress server=servers.nextElement();
		        	String query=serversNQueries.get(server);
		    	    QueryThread queryThread;
		    	    try {
		    	    	queryThread = new QueryThread(server,query);
			    	    Future<String> promisedReply=pool.submit(queryThread);
			    	    promisedReplies.put(server, promisedReply);
		    	    } catch (IOException e) {
					System.out.println("Could not open connection to "+server.toString());
		    	    }
		        }
		       try {
				Thread.sleep(Parameters.SERVER_LATENCY);
		       } catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		       }
		       servers=serversNQueries.keys();
		       while(servers.hasMoreElements()){
		    	   		InetSocketAddress server=servers.nextElement();
		    	   		if(!promisedReplies.containsKey(server))
		    	   			continue;
			    	   Future<String> promisedReply=promisedReplies.get(server);
			    	   
			    		   Tools.print("looking at promised reply");
			    		   String reply=null;
						try {
							reply = promisedReply.get(Parameters.RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							Tools.print("InterruptedException");
							e.printStackTrace();
						} catch (ExecutionException e) {
							Tools.print("ExecutionException");
							e.printStackTrace();
						} catch (TimeoutException e) {
							Tools.print("TimeoutException");
				    	     promisedReply.cancel(true);
				    	     Tools.print("cancelled reply");
				             
							e.printStackTrace();
							continue;
						}
			    		   handleReply(server,reply);

			    	   if(!getNextReply) break;
		       }
	       
			
		}

		public abstract void evaluateRepliesAndMakeDecision();
		public void makeDecisionHalfWay(String decision){
			this.decision=decision;
			getNextReply=false;
			pool.shutdownNow();
		}
		public void makeDecision(String decision){
			this.decision=decision;
		}
		public void stopGettingReplies(){
			getNextReply=false;
		}
		public abstract void handleReply(InetSocketAddress server,String reply);
				
	}
	public class Announcer extends Thread{
		public void run(){
			
			
		}
	}
	public static class Teacher{
		Vector<InetSocketAddress> servers;
		String value;
		long timeOut;
		public Teacher(Vector<InetSocketAddress> servers,String value,long timeOut){
			this.servers=servers;
			this.value=value;
			this.timeOut=timeOut;
		}
		public boolean teach(){

			long startTime=System.currentTimeMillis();
			 ExecutorService  pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
			 Vector<InetSocketAddress> remainingHosts=servers;
			 Hashtable<InetSocketAddress, Integer> hostStatuses=new Hashtable<InetSocketAddress, Integer>();
			 for (InetSocketAddress server:servers){
				 hostStatuses.put(server, 0);
			 }
			 int hostsLeft=servers.size();
			 while(hostsLeft>0){
				 Tools.print(Tools.serializeVectorAddress(remainingHosts));
				 if((System.currentTimeMillis()-startTime)>timeOut)
					 return false;
				Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
			    for (InetSocketAddress server:servers){
			    	if(hostStatuses.get(server)==1) break;
			    	   QueryThread queryThread;
			    	   try {
							queryThread = new QueryThread(server,value);
				    	   Future<String> promisedReply=pool.submit(queryThread);
				    	   promisedReplies.put(server, promisedReply);
			    	   } catch (IOException e) {
			    		   System.out.println("Caught IOexception at teachValue "+e);
			    	   }

			       }
		       for (InetSocketAddress server:servers){
		    	   if(hostStatuses.get(server)==1) break;
			    	   Future<String> promisedReply=promisedReplies.get(server);
			    	   try {
			    		   promisedReply.get(Parameters.RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
			    		   hostsLeft--;
			    		   hostStatuses.put(server, 1);
			    	   } 
			    	   catch(Exception e){
			    		   System.out.println("Caught exception at teachValue "+e);
			    	   }
		       }
			}
			 pool.shutdownNow();
			 return true;
		
			
		}
	}
	public  synchronized boolean teachValue(Vector<InetSocketAddress> servers,String value,long timeOut) {

		long startTime=System.currentTimeMillis();
		 ExecutorService  pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
		 Vector<InetSocketAddress> remainingHosts=servers;
		 Hashtable<InetSocketAddress, Integer> hostStatuses=new Hashtable<InetSocketAddress, Integer>();
		 for (InetSocketAddress server:servers){
			 hostStatuses.put(server, 0);
		 }
		 int hostsLeft=servers.size();
		 while(hostsLeft>0){
			 Tools.print(Tools.serializeVectorAddress(remainingHosts));
			 if((System.currentTimeMillis()-startTime)>timeOut)
				 return false;
			Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
		    for (InetSocketAddress server:servers){
		    	if(hostStatuses.get(server)==1) break;
		    	   QueryThread queryThread;
		    	   try {
						queryThread = new QueryThread(server,value);
			    	   Future<String> promisedReply=pool.submit(queryThread);
			    	   promisedReplies.put(server, promisedReply);
		    	   } catch (IOException e) {
		    		   System.out.println("Caught IOexception at teachValue "+e);
		    	   }

		       }
	       for (InetSocketAddress server:servers){
	    	   if(hostStatuses.get(server)==1) break;
		    	   Future<String> promisedReply=promisedReplies.get(server);
		    	   try {
		    		   promisedReply.get(Parameters.RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
		    		   hostsLeft--;
		    		   hostStatuses.put(server, 1);
		    	   } 
		    	   catch(Exception e){
		    		   System.out.println("Caught exception at teachValue "+e);
		    	   }
	       }
		}
		 pool.shutdownNow();
		 return true;
	
		
	}
	

	
	
	public static Hashtable<InetSocketAddress,String> queryAllSimultaneouslyAndExtractReplies(Vector<InetSocketAddress> servers,String query) throws IOException{
		   ExecutorService pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
	       Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
	       Hashtable<InetSocketAddress,String> replies=new Hashtable<InetSocketAddress, String>();
	       for (InetSocketAddress server:servers){
	    	   QueryThread queryThread=new QueryThread(server,query);
	    	   Future<String> promisedReply=pool.submit(queryThread);
	    	   promisedReplies.put(server, promisedReply);
	       }
	       try {
			Thread.sleep(Parameters.SERVER_LATENCY);
	       } catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	       }
	       for (InetSocketAddress server:servers){
		    	   Future<String> promisedReply=promisedReplies.get(server);
		    	   try {
		    		   String reply=promisedReply.get(Parameters.RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
			    	   replies.put(server, reply);
		    	   } 
		    	   catch (Exception e) {
		    	     promisedReply.cancel(true);
		             continue;
		    	   }
	       }
	       pool.shutdownNow();
	       return replies;
	}
	
	public static Hashtable<InetSocketAddress,Future<String> > queryAllSimultaneouslyAndExtractPromises(Vector<InetSocketAddress> servers,InetSocketAddress fromAddress,String query) throws IOException{
		   ExecutorService pool=Executors.newFixedThreadPool(Parameters.MAX_SIMULTANEOUS_CONNECTIONS);
	       Hashtable<InetSocketAddress,Future<String>> promisedReplies=new Hashtable<InetSocketAddress,Future<String>>();
	       for (InetSocketAddress server:servers){
	    	   QueryThread queryThread=new QueryThread(server,query);
	    	   Future<String> promisedReply=pool.submit(queryThread);
	    	   promisedReplies.put(server, promisedReply);
	       }
	       return promisedReplies;
	}
	public static class QueryThread implements Callable<String>{
    	RWChannel channel;
    	String reply;
    	String query;
    	public QueryThread(InetSocketAddress destination,String query) throws IOException{
    		channel=new RWChannel(destination,false);
    		this.query=query;
    	}
	    public String call() throws TimeoutException,IOException,InterruptedException{
	    	channel.write(query);
	    	reply=channel.read();
	    	channel.close();
	    	return reply;
	      }
	}




}
