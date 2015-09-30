package middleware;

/* a meta data class to hold information of a Resource Manager used by Middleware */
public class RMmeta {

	enum RMtype {car, room, flight};
	
	RMtype _type;
	String _host;
	int _port;
	
	public RMmeta(String type, String host, String port){
	    _type = RMtype.valueOf(type);
	    _host = host;
	    _port = Integer.valueOf(port).intValue();
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
	
	@Override
	public String toString(){
		return String.format("RM: %s %s %d", _type.toString(),_host, _port);
	}
}
