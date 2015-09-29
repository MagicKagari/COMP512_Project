package middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import middleware.RMmeta.RMtype;
import server.ResourceManagerImpl;
import server.ws.ResourceManager;


public class Middleware extends ResourceManagerImpl{

	
    int _port;
	String _host;
    List<RMmeta> resourceManagers;	

	
	/* constructor */
	public Middleware(String host, int port){
		resourceManagers = new LinkedList<RMmeta>();
		_port = port;
		_host = host;
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
		
		//if (args.length != 2){
		//	System.out.println("Wrong number of pairs of arguments.");
		//}
		
		/* the first pair is the Middleware host and port */
		//Middleware middleware = new Middleware(args[0], Integer.valueOf(args[1]).intValue());
		Middleware middleware = new Middleware("localhost",15080);
		
		/* accepting incoming cmd lines for information */
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
		
		System.out.println(middleware.toString());
		
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
