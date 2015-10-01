package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.catalina.Server;

import com.sun.xml.ws.api.pipe.Fiber.Listener;

import middleware.RMmeta.RMtype;
import server.ResourceManagerImpl;
import server.ws.ResourceManager;


public class Middleware {

    int _port;
	String _host;
    List<RMmeta> resourceManagers;
	ServerSocket mainListener;

	/* constructor */
	public Middleware(String host, int port){
		
		resourceManagers = new LinkedList<RMmeta>();
		_port = port;
		_host = host;
		
		try {
			mainListener = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addResourceManager(){
		/* accepting incoming cmd lines for information */
		System.out.println("Enter RM server information or finish, example: car,localhost,8082");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		boolean ctr = true;
		while(ctr){
			String input = "";
			try{
				input = stdin.readLine();
			}catch(IOException io){
				System.out.println("Unable to read.");
				System.exit(1);
			}
			
			/* 
			 * the line is a comma seperated string
			 * Example: car,localhost,8082
			 * or "finish" to end inputing
			 */
		
			switch (input) {
			case "finish":
				ctr = false;
				break;
			default:
				String inputArray[] = input.split(",");
				if(inputArray.length == 3){
					resourceManagers.add(new RMmeta(inputArray[0], inputArray[1], inputArray[2]));
				}else{
					System.out.println("Wrong format, example: car,localhost,8082 ");
				}
				break;
			}
		}
		System.out.println("Finish gather information for RMs");
	}
	
	/*
	 * function to get a RMmeta with desired type
	 */
	public RMmeta getResourceManagerOfType(RMtype type){
		for(RMmeta rm : resourceManagers){
			if(rm.getRMtype().equals(type)){
				return rm;
			}
		}
		return null;
	}
	
	/*
	 * loop function that waiting for client connection,
	 * create socket to handle it then 
	 */
	public void run() throws IOException{
		while(true){
			/*
			 * waiting for a connection from client
			 */
			Socket connectionSocket = mainListener.accept();
			System.out.println("MS: Connection come in");
			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream())); 
			DataOutputStream outToClient = new DataOutputStream(
					connectionSocket.getOutputStream());
			while(true){
				//read in command send from client  
				String clientCommand = inFromClient.readLine();  
				if(clientCommand == null){
					System.out.println("Received null cmd, close connection.");
					break;
				}
				
				System.out.println("Received: " + clientCommand);            
				
				if(clientCommand.split(",").length > 0){
					//decode which RM to send to
					RMmeta desiredRM = getResourceManagerOfType(
							getRMtype(clientCommand.split(",")[0]));
					
					if(desiredRM == null){
						outToClient.writeBytes("Server is down.\n");
						break;
					}
					
					Socket handler = desiredRM.getSocket();
					
					//get RM's response	
					BufferedReader inFromServer = new BufferedReader(
			    			new InputStreamReader(handler.getInputStream()));
			   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
			   		outToServer.writeBytes(clientCommand + '\n');
			   		
			   		String ret = inFromServer.readLine();
			   		System.out.println("FROM RM SERVER: " + ret);
			   		
			   		if(ret == null) ret = "empty";
			   		
					outToClient.writeBytes(ret + '\n');
					System.out.println("Finish writing back to client.");
				}else{
					System.out.println("Wrong command.");
					break;
				}
			}
		}
	}
	
	/*
	 * return a RMtype according to sendMessage defined in Client class
	 */
	public RMtype getRMtype(String str){
		if(str.contains("Flight")){
			return RMtype.flight;
		}else if(str.contains("Car")){
			return RMtype.car;
		}else if(str.contains("Room")){
			return RMtype.room;
		}else if(str.contains("Customer")){
			return RMtype.customer;
		}else{
			return null;
		}
	}
	
	@Override
	public String toString(){
		String tmp = "";
		for(RMmeta rm : resourceManagers){
			tmp += rm.toString()+"\n";
		}
		return tmp;
	}
	
	/* 
	 * run the middleware  
	 */
	public static void main(String[] args) {
		
		
		if (args.length != 3){
			System.out.println("Usage: Middleware <service-name> <service-host> <service-port>");
			System.exit(-1);
		}
		
		String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);
        Middleware middleware = new Middleware(serviceHost,servicePort);
		
		middleware.addResourceManager();
		System.out.println(String.format("Starting middleware services on %s %s", 
				middleware.getHost(), middleware.getPort()));
		
		try {
			middleware.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public String getHost(){
		return _host;
	}
	
	public int getPort(){
		return _port;
	}
}
