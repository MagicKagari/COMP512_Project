package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.catalina.Server;

import client.Client;

import com.sun.xml.ws.api.pipe.Fiber.Listener;

import middleware.RMmeta.RMtype;
import server.ResourceManagerImpl;
import server.ws.ResourceManager;


public class Middleware extends ResourceManagerImpl{

    int _port;
	String _host;
    List<RMmeta> resourceManagers;
	ServerSocket mainListener;

	/* constructor */
	public Middleware(String host, int port){
		
		super(host, port);
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
					break;
				}
				
				System.out.println("Received: " + clientCommand);            
				
				if(clientCommand.split(",").length > 0){
					//decode which RM to send to
					RMmeta desiredRM = getResourceManagerOfType(
							getRMtype(clientCommand.split(",")[0]));
					
					if(desiredRM == null){
						String firstWord = clientCommand.split(",")[0];
						
						//special case where the option is related to customer
						//it will be broadcast to all RMs
						if(firstWord.contains("Customer") || firstWord.contains("customer")){
							//another special case is where we newCustomer
							//in this case middleware generate a id and use newcustomerid for other RMs
							if(firstWord.compareToIgnoreCase("newcustomer") == 0){
								int id = 0;
						        String location;
						        Vector arguments = new Vector();
						        //remove heading and trailing white space
						        clientCommand = clientCommand.trim();
						        arguments = Client.parse(clientCommand);
						        try{
						        	id = Client.getInt(arguments.elementAt(1));
						        }catch(Exception e){
						        	e.printStackTrace();
						        }
								int customerId = Integer.parseInt(String.valueOf(id) +
						                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
						                String.valueOf(Math.round(Math.random() * 100 + 1)));
								//call newCustomerID to all other RMs
								String ret = "";
								for(RMmeta rm : resourceManagers){
									Socket handler = rm.getSocket();
									BufferedReader inFromServer = new BufferedReader(
							    			new InputStreamReader(handler.getInputStream()));
							   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
							   		outToServer.writeBytes(String.format("newCustomerID,%d,%d", id, customerId) + "\n");
							   		ret += inFromServer.readLine();
								}
								outToClient.writeBytes(String.format("Customer %d id : %d", id, customerId) + "\n");
								continue;
								
								
							}else{
								String ret = "";
								for(RMmeta rm : resourceManagers){
									Socket handler = rm.getSocket();
									BufferedReader inFromServer = new BufferedReader(
							    			new InputStreamReader(handler.getInputStream()));
							   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
							   		outToServer.writeBytes(clientCommand + "\n");
							   		ret += inFromServer.readLine();
								}
								outToClient.writeBytes(ret + "\n");
								continue;
							}
						}
						
						//special case where we reserve itinerary
						if(firstWord.compareToIgnoreCase("itinerary") == 0 ){
							 int id;
					         int flightNumber;
					         boolean room;
					         boolean car;
					         String location;
					         Vector arguments = new Vector();
					         //remove heading and trailing white space
					         clientCommand = clientCommand.trim();
					         arguments = Client.parse(clientCommand);
					         System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
				             System.out.println("Customer id: " + arguments.elementAt(2));
				             for (int i = 0; i<arguments.size()-6; i++)
				                 System.out.println("Flight number: " + arguments.elementAt(3 + i));
				             System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
				             System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
				             System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
				             try {
				            	 
				                 id = Client.getInt(arguments.elementAt(1));
				                 int customer = Client.getInt(arguments.elementAt(2));
				                 Vector flightNumbers = new Vector();
				                 for (int i = 0; i < arguments.size()-6; i++)
				                     flightNumbers.addElement(arguments.elementAt(3 + i));
				                 location = Client.getString(arguments.elementAt(arguments.size()-3));
				                 car = Client.getBoolean(arguments.elementAt(arguments.size()-2));
				                 room = Client.getBoolean(arguments.elementAt(arguments.size()-1));
				                 
				                 //go through the itinernary
				                 String command = "";
				                 String ret = "";
				                 Socket handler;

				                 RMmeta rm = getResourceManagerOfType(getRMtype("Flight"));
				                 if(rm != null){
				                	 System.out.println("Handle flight itinerary");
				                	 handler = rm.getSocket();
					             	 BufferedReader inFromServer = new BufferedReader(
							    			new InputStreamReader(handler.getInputStream()));
							   		 DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
							   		 for(int i = 0; i < flightNumbers.size(); i++){
					                 	 flightNumber = Client.getInt(flightNumbers.elementAt(i));
					                 	 command = String.format("ReserveFlight,%d,%d,%d", id, customer, flightNumber);
					                 	 outToServer.writeBytes(command + '\n');
					                 	 ret += inFromServer.readLine();                      
					                 }
				                 }
				                 
				                 if(car){
				                	 rm = getResourceManagerOfType(getRMtype("Car"));
				                	 if(rm != null){
				                		 System.out.println("Handle car itinerary");
					                	 
				                		 handler = rm.getSocket();
				                		 BufferedReader inFromServer = new BufferedReader(
									    			new InputStreamReader(handler.getInputStream()));
				                		 DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
									   	 command = String.format("ReserveCar,%d,%d,%s", id, customer, location);
									   	 outToServer.writeBytes(command + '\n');
									   	 ret += inFromServer.readLine();
				                	 }
				                 }
				                 
				                 if(room){
				                	 rm = getResourceManagerOfType(getRMtype("Room"));
				                	 if(rm != null){
				                		 System.out.println("Handle room itinerary");
					                	 
				                		 handler = rm.getSocket();
				                		 BufferedReader inFromServer = new BufferedReader(
									    			new InputStreamReader(handler.getInputStream()));
				                		 DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
									   	 command = String.format("ReserveRoom,%d,%d,%s", id, customer, location);
									   	 outToServer.writeBytes(command + '\n');
									   	 ret += inFromServer.readLine();
				                	 }
				                 }
				                 outToClient.writeBytes(ret + '\n');
				                 continue;//continue to next waiting loop
				             }
				             catch(Exception e) {
				                 System.out.println("EXCEPTION: ");
				                 System.out.println(e.getMessage());
				                 e.printStackTrace();
				             }
						}//end of itenerary
						else{
							//or simply no where to send
							outToClient.writeBytes("Server is down.\n");
							continue;
						}
						continue;
					}//end of special case handling
					
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
