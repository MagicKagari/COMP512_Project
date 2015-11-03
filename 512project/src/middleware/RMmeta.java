package middleware;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/* a meta data class to hold information of a Resource Manager used by Middleware */
public class RMmeta {

	public enum RMtype {car, room, flight, customer};
	
	RMtype _type;
	String _host;
	int _port;
	Socket _rmSocket;
	
	public RMmeta(String type, String host, String port){
	    _type = RMtype.valueOf(type);
	    _host = host;
	    _port = Integer.valueOf(port).intValue();
	    
	    try {
			_rmSocket = new Socket(_host, _port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RMtype getRMtype(){
		return _type;
	}
	
	public String getHost(){
		return _host;
	}
	
	public int getPort(){
		return _port;
	}
	
	public Socket getSocket(){
		return _rmSocket;
	}
	
	@Override
	public String toString(){
		return String.format("RM: %s %s %d", _type.toString(),_host, _port);
	}
}
