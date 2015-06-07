package dos.common.util;

import java.net.InetSocketAddress;
import java.util.StringTokenizer;


public class Envelope {
InetSocketAddress fromAddress;
String message;
static String fromAddressMessageSplitter="$";
public Envelope(InetSocketAddress fromAddress,String message){
	this.fromAddress=fromAddress;
	this.message=message;
}
public Envelope(int port,String message){
	this.fromAddress=Tools.generateAddress(port);
	this.message=message;
}
public static Envelope toEnvelope(String serializedEnvelope){
	StringTokenizer tokenizer=new StringTokenizer(serializedEnvelope,fromAddressMessageSplitter);
	InetSocketAddress fromAddress=Tools.toInetSocketAddress(tokenizer.nextToken());
	String message=tokenizer.nextToken();
	return new Envelope(fromAddress,message);
}
public static InetSocketAddress extractAddress(String serializedEnvelope ){
	return Tools.toInetSocketAddress(serializedEnvelope.substring(0, serializedEnvelope.indexOf(fromAddressMessageSplitter)));
}
public static String extractMessage(String serializedEnvelope){
	return serializedEnvelope.substring(serializedEnvelope.indexOf(fromAddressMessageSplitter)+1);
}
public static String concatPortNoToMessage(int port,String message){
	return Integer.toString(port).concat(fromAddressMessageSplitter).concat(message);
}
public static int extractPortNoFromMessage(String message){
	return Integer.parseInt(message.substring(0,message.indexOf(fromAddressMessageSplitter)));
}

public InetSocketAddress fromAddress(){
	return fromAddress;
}
public String message(){
	return message;
}
public String toString(){
	return fromAddress.toString().concat(fromAddressMessageSplitter).concat(message);
}

}
