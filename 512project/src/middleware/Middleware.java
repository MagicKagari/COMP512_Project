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


public class Middleware implements server.ws.ResourceManager{

	static int REVERSED_PORT_NUMBER = 5; //reserve next ports used to handle multiple client
	int serverPorts[];
	HashMap<Integer, Socket> clientMap;
	
	
    int _port;
	String _host;
    List<RMmeta> resourceManagers;
	ServerSocket mainListener;
	Socket resourceManagerSocket;

	HashMap<Socket, ResourceManager> socketMap;
	
	/* constructor */
	public Middleware(String host, int port){
		
		resourceManagers = new LinkedList<RMmeta>();
		_port = port;
		_host = host;
		socketMap = new HashMap<Socket, ResourceManager>();
		
		serverPorts = new int[REVERSED_PORT_NUMBER];
		for(int i = 0; i< REVERSED_PORT_NUMBER; i++){
			serverPorts[i] = port + i + 1;
		}
		
		try {
			mainListener = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addRM(String type, String host, String port){
		resourceManagers.add(new RMmeta(type, host, port));
	}
	
	public RMmeta getRM(RMtype type){
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
			Socket connectionSocket = mainListener.accept();
			System.out.println("MS: Connection come in");
			while(true){
				BufferedReader inFromClient = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream())); 
				DataOutputStream outToClient = new DataOutputStream(
						connectionSocket.getOutputStream());  
				String clientCommand = inFromClient.readLine();     
				System.out.println("Received: " + clientCommand);            
				
				for( RMmeta rm : resourceManagers){
					String rmHost = rm.getHost();
					int rmPort = rm.getPort();
					Socket handler = new Socket(rmHost, rmPort);
					
					BufferedReader inFromServer = new BufferedReader(
		    				new InputStreamReader(handler.getInputStream()));
		    		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
		    		outToServer.writeBytes(clientCommand + '\n');
		    		String ret = inFromServer.readLine();
		    		System.out.println("FROM RM SERVER: " + ret);
					outToClient.writeBytes(ret);
				}
				outToClient.writeBytes("finish.\n");
			}
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
					middleware.addRM(inputArray[0],inputArray[1],inputArray[2]);
				}
				break;
			}
		}
		System.out.println(String.format("Finish gather information for RMs,"
				+ "starting middleware services on %s %s", middleware.getHost(), middleware.getPort()));
		
		try {
			middleware.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getHost(){
		return _host;
	}
	
	public int getPort(){
		return _port;
	}
	/* implentation */
	
	@Override
	public boolean addFlight(int id, int flightNumber, int numSeats,
			int flightPrice) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFlight(int id, int flightNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int carPrice) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryCars(int id, String location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCarsPrice(int id, String location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int roomPrice) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryRooms(int id, String location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRoomsPrice(int id, String location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int newCustomer(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newCustomerId(int id, int customerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customerId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String queryCustomerInfo(int id, int customerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reserveFlight(int id, int customerId, int flightNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveCar(int id, int customerId, String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customerId, String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveItinerary(int id, int customerId,
			Vector flightNumbers, String location, boolean car, boolean room) {
		// TODO Auto-generated method stub
		return false;
	}
}
