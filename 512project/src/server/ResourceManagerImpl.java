// -------------------------------
// Adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.jws.WebService;

import client.Client;


@WebService(targetNamespace = "comp512", endpointInterface = "server.ws.ResourceManager")
public class ResourceManagerImpl implements server.ws.ResourceManager {
    
	protected RMHashtable m_itemHT = new RMHashtable();
    ServerSocket resourceManagerServerSocket;
    String _host;
    int _port;
    
    public ResourceManagerImpl(String host, int port){
    	_host = host;
    	_port = port;
    }
    
    public void initSocket(){
    	try {
			resourceManagerServerSocket = new ServerSocket(_port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
    	try {
    		if (args.length != 3) {
    			System.out.println("Usage: MyServer <service-name> <service-host> <service-port>");
    	        System.exit(-1);
    	    }
    	            
    	    String serviceName = args[0];
    	    String serviceHost = args[1];
    	    int servicePort = Integer.parseInt(args[2]);
    	       
    	    System.out.println("Starting RM on " + serviceHost + " " + servicePort);
    		ResourceManagerImpl rManagerImpl = new ResourceManagerImpl(serviceHost, servicePort);
    		rManagerImpl.initSocket();
    		while(true){
    			rManagerImpl.run();
    		}
    	}catch(Exception e) {
            e.printStackTrace();
        }
    	
	}
    
    public void run() throws IOException{
    	Socket middlewareSocket = resourceManagerServerSocket.accept();
    	System.out.println("RM: Connection come in.");
    	BufferedReader inFromClient = new BufferedReader(
				new InputStreamReader(middlewareSocket.getInputStream())); 
		DataOutputStream outToClient = new DataOutputStream(
				middlewareSocket.getOutputStream());  

		while(true){
			synchronized (inFromClient) {
				synchronized (outToClient) {
					String middlewareCommand = inFromClient.readLine();     
					System.out.println("Received: " + middlewareCommand);            
					outToClient.writeBytes(decodeCommand(middlewareCommand) + "\n");
				}
			}
		}
    	
    }
    
    /*
     * decode the command and execute it
     * same parsing style as Client class
     */
    public String decodeCommand(String command){
    	
    	String ret = "";
    	
    	 int id;
         int flightNumber;
         int flightPrice;
         int numSeats;
         boolean room;
         boolean car;
         int price;
         int numRooms;
         int numCars;
         String location;

         Vector arguments = new Vector();

         //remove heading and trailing white space
         command = command.trim();
         arguments = Client.parse(command);
         
         if(arguments.size() == 0){
        	 return "Empty Command";
         }else{
        	 for(int i=1; i<arguments.size();i++){
        		 String s = (String)arguments.get(i);
        		 try{
        			 Integer.parseInt(s);
        		 }catch(NumberFormatException e){
        			 return "Wrong command format";
        		 }
        	 }
         }
         System.out.println("Command: " + command);
         //decide which of the commands this was
         switch(Client.findChoice((String) arguments.elementAt(0))) {

         case 1: //help section
             System.out.println("No such thing.");
             break;
             
         case 2:  //new flight
             if (arguments.size() != 5) {
                 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
             System.out.println("Flight number: " + arguments.elementAt(2));
             System.out.println("Add Flight Seats: " + arguments.elementAt(3));
             System.out.println("Set Flight Price: " + arguments.elementAt(4));
             
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 flightNumber = Client.getInt(arguments.elementAt(2));
                 numSeats = Client.getInt(arguments.elementAt(3));
                 flightPrice = Client.getInt(arguments.elementAt(4));
                 
                 if(addFlight(id, flightNumber, numSeats, flightPrice)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
                 
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 3:  //new car
        	 if (arguments.size() != 5) {
        		 ret = "Wrong argument number.";
        		 break;
        	 }
             System.out.println("Adding a new car using id: " + arguments.elementAt(1));
             System.out.println("car Location: " + arguments.elementAt(2));
             System.out.println("Add Number of cars: " + arguments.elementAt(3));
             System.out.println("Set Price: " + arguments.elementAt(4));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));
                 numCars = Client.getInt(arguments.elementAt(3));
                 price = Client.getInt(arguments.elementAt(4));

                 if(addCars(id, location, numCars, price)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 4:  //new room
             if (arguments.size() != 5) {
                 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Adding a new room using id: " + arguments.elementAt(1));
             System.out.println("room Location: " + arguments.elementAt(2));
             System.out.println("Add Number of rooms: " + arguments.elementAt(3));
             System.out.println("Set Price: " + arguments.elementAt(4));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));
                 numRooms = Client.getInt(arguments.elementAt(3));
                 price = Client.getInt(arguments.elementAt(4));

                 if(addRooms(id, location, numRooms, price)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {	
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 5:  //new Customer
             if (arguments.size() != 2) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Adding a new Customer using id: " + arguments.elementAt(1));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int clientID = newCustomer(id);
                 ret = String.format("Customer ID generated: %d", clientID);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 6: //delete Flight
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Deleting a flight using id: " + arguments.elementAt(1));
             System.out.println("Flight Number: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 flightNumber = Client.getInt(arguments.elementAt(2));

                 if(deleteFlight(id, flightNumber)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 7: //delete car
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
             System.out.println("car Location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 if(deleteCars(id, location)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
              
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 8: //delete room
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
             System.out.println("room Location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 if(deleteRooms(id, location)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 9: //delete Customer
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 
                 break;
             }
             System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));

                 if(deleteCustomer(id, customer)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 10: //querying a flight
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 
                 break;
             }
             System.out.println("Querying a flight using id: " + arguments.elementAt(1));
             System.out.println("Flight number: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 flightNumber = Client.getInt(arguments.elementAt(2));
                 
                 int seats = queryFlight(id, flightNumber);
                 ret = String.format("%d seats available.", seats);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 11: //querying a car Location
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 
                 break;
             }
             System.out.println("Querying a car location using id: " + arguments.elementAt(1));
             System.out.println("car location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 int cars = queryCars(id, location);
                 ret = String.format("%d cars available.", cars);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 12: //querying a room location
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Querying a room location using id: " + arguments.elementAt(1));
             System.out.println("room location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 int rooms = queryRooms(id, location);
                 ret = String.format("%d rooms available.", rooms);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 13: //querying Customer Information
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));

                 ret = "Customer info : " + queryCustomerInfo(id, customer);
                 
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;               
             
         case 14: //querying a flight Price
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
             System.out.println("Flight number: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 flightNumber = Client.getInt(arguments.elementAt(2));

                 ret = "Flight price : " + queryFlightPrice(id, flightNumber);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 15: //querying a car Price
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Querying a car price using id: " + arguments.elementAt(1));
             System.out.println("car location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 ret = "Car price : " + queryCarsPrice(id, location);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }                
             break;

         case 16: //querying a room price
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Querying a room price using id: " + arguments.elementAt(1));
             System.out.println("room Location: " + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 location = Client.getString(arguments.elementAt(2));

                 ret += "Room price: " + queryRoomsPrice(id, location);
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 17:  //reserve a flight
             if (arguments.size() != 4) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             System.out.println("Flight number: " + arguments.elementAt(3));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));
                 flightNumber = Client.getInt(arguments.elementAt(3));

                 if (reserveFlight(id, customer, flightNumber)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 18:  //reserve a car
             if (arguments.size() != 4) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             System.out.println("Location: " + arguments.elementAt(3));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));
                 location = Client.getString(arguments.elementAt(3));
                 
                 if (reserveCar(id, customer, location)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
                 
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 19:  //reserve a room
             if (arguments.size() != 4) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             System.out.println("Location: " + arguments.elementAt(3));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));
                 location = Client.getString(arguments.elementAt(3));
                 
                 if (reserveRoom(id, customer, location)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         case 20:  //reserve an Itinerary
        	 /*
             if (arguments.size()<7) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
             System.out.println("Customer id: " + arguments.elementAt(2));
             for (int i = 0; i<arguments.size()-6; i++)
                 System.out.println("Flight number" + arguments.elementAt(3 + i));
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
                 
                 //go through the iternary
                 boolean result = false;
                 for(int i = 0; i < flightNumbers.size(); i++){
                 	flightNumber = Client.getInt(flightNumbers.elementAt(i));
                 	result = reserveFlight(id, customer, flightNumber);                            
                 }
                 if(car){
                	 result = reserveCar(id, customer, location);
                 }
                 if(room){
                	 result = reserveRoom(id, customer, location);
                 }
                 
                 if(result){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
                 
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             */
        	 ret = "Wrong command.";
             break;
                         
         case 21: //quit the client
        	 ret = "Wrong command.";
        	 break;
             
         case 22:  //new Customer given id
             if (arguments.size() != 3) {
            	 ret = "Wrong argument number.";
                 break;
             }
             System.out.println("Adding a new Customer using id: "
                     + arguments.elementAt(1)  +  " and cid "  + arguments.elementAt(2));
             try {
                 id = Client.getInt(arguments.elementAt(1));
                 int customer = Client.getInt(arguments.elementAt(2));

                 
                 if (newCustomerId(id, customer)){
                	 ret = "Operation success.";
                 }else{
                	 ret = "Operation failed.";
                 }
             }
             catch(Exception e) {
                 System.out.println("EXCEPTION: ");
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
             break;
             
         default:
             System.out.println("The interface does not support this command.");
             break;
         }//end switch
		return ret;
     }//end function
    
    
    // Basic operations on RMItem //
    
    // Read a data item.
    private RMItem readData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Write a data item.
    private void writeData(int id, String key, RMItem value) {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage.
    protected RMItem removeData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.remove(key);
        }
    }
    
    
    // Basic operations on ReservableItem //
    
    // Delete the entire item.
    protected boolean deleteItem(int id, String key) {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called.");
        ReservableItem curObj = (ReservableItem) readData(id, key);
        // Check if there is such an item in the storage.
        if (curObj == null) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed: " 
                    + " item doesn't exist.");
            return false;
        } else {
            if (curObj.getReserved() == 0) {
                removeData(id, curObj.getKey());
                Trace.info("RM::deleteItem(" + id + ", " + key + ") OK.");
                return true;
            }
            else {
                Trace.info("RM::deleteItem(" + id + ", " + key + ") failed: "
                        + "some customers have reserved it.");
                return false;
            }
        }
    }
    
    // Query the number of available seats/rooms/cars.
    protected int queryNum(int id, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called.");
        ReservableItem curObj = (ReservableItem) readData(id, key);
        int value = 0;  
        if (curObj != null) {
            value = curObj.getCount();
        }
        Trace.info("RM::queryNum(" + id + ", " + key + ") OK: " + value);
        return value;
    }    
    
    // Query the price of an item.
    protected int queryPrice(int id, String key) {
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called.");
        ReservableItem curObj = (ReservableItem) readData(id, key);
        int value = 0; 
        if (curObj != null) {
            value = curObj.getPrice();
        }
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") OK: $" + value);
        return value;
    }

    // Reserve an item.
    protected boolean reserveItem(int id, int customerId, 
                                  String key, String location) {
        Trace.info("RM::reserveItem(" + id + ", " + customerId + ", " 
                + key + ", " + location + ") called.");
        // Read customer object if it exists (and read lock it).
        Customer cust = (Customer) readData(id, Customer.getKey(customerId));
        if (cust == null) {
            Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " 
                   + key + ", " + location + ") failed: customer doesn't exist.");
            return false;
        } 
        
        // Check if the item is available.
        ReservableItem item = (ReservableItem) readData(id, key);
        if (item == null) {
            Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " 
                    + key + ", " + location + ") failed: item doesn't exist.");
            return false;
        } else if (item.getCount() == 0) {
            Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " 
                    + key + ", " + location + ") failed: no more items.");
            return false;
        } else {
            // Do reservation.
            cust.reserve(key, location, item.getPrice());
            writeData(id, cust.getKey(), cust);
            
            // Decrease the number of available items in the storage.
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved() + 1);
            
            Trace.warn("RM::reserveItem(" + id + ", " + customerId + ", " 
                    + key + ", " + location + ") OK.");
            return true;
        }
    }
    
    
    // Flight operations //
    
    // Create a new flight, or add seats to existing flight.
    // Note: if flightPrice <= 0 and the flight already exists, it maintains 
    // its current price.
    @Override
    public boolean addFlight(int id, int flightNumber, 
                             int numSeats, int flightPrice) {
        Trace.info("RM::addFlight(" + id + ", " + flightNumber 
                + ", $" + flightPrice + ", " + numSeats + ") called.");
        Flight curObj = (Flight) readData(id, Flight.getKey(flightNumber));
        if (curObj == null) {
            // Doesn't exist; add it.
            Flight newObj = new Flight(flightNumber, numSeats, flightPrice);
            writeData(id, newObj.getKey(), newObj);
            Trace.info("RM::addFlight(" + id + ", " + flightNumber 
                    + ", $" + flightPrice + ", " + numSeats + ") OK.");
        } else {
            // Add seats to existing flight and update the price.
            curObj.setCount(curObj.getCount() + numSeats);
            if (flightPrice > 0) {
                curObj.setPrice(flightPrice);
            }
            writeData(id, curObj.getKey(), curObj);
            Trace.info("RM::addFlight(" + id + ", " + flightNumber 
                    + ", $" + flightPrice + ", " + numSeats + ") OK: "
                    + "seats = " + curObj.getCount() + ", price = $" + flightPrice);
        }
        return(true);
    }

    @Override
    public boolean deleteFlight(int id, int flightNumber) {
        return deleteItem(id, Flight.getKey(flightNumber));
    }

    // Returns the number of empty seats on this flight.
    @Override
    public int queryFlight(int id, int flightNumber) {
        return queryNum(id, Flight.getKey(flightNumber));
    }

    // Returns price of this flight.
    public int queryFlightPrice(int id, int flightNumber) {
        return queryPrice(id, Flight.getKey(flightNumber));
    }

    /*
    // Returns the number of reservations for this flight. 
    public int queryFlightReservations(int id, int flightNumber) {
        Trace.info("RM::queryFlightReservations(" + id 
                + ", #" + flightNumber + ") called.");
        RMInteger numReservations = (RMInteger) readData(id, 
                Flight.getNumReservationsKey(flightNumber));
        if (numReservations == null) {
            numReservations = new RMInteger(0);
       }
        Trace.info("RM::queryFlightReservations(" + id + 
                ", #" + flightNumber + ") = " + numReservations);
        return numReservations.getValue();
    }
    */
    
    /*
    // Frees flight reservation record. Flight reservation records help us 
    // make sure we don't delete a flight if one or more customers are 
    // holding reservations.
    public boolean freeFlightReservation(int id, int flightNumber) {
        Trace.info("RM::freeFlightReservations(" + id + ", " 
                + flightNumber + ") called.");
        RMInteger numReservations = (RMInteger) readData(id, 
                Flight.getNumReservationsKey(flightNumber));
        if (numReservations != null) {
            numReservations = new RMInteger(
                    Math.max(0, numReservations.getValue() - 1));
        }
        writeData(id, Flight.getNumReservationsKey(flightNumber), numReservations);
        Trace.info("RM::freeFlightReservations(" + id + ", " 
                + flightNumber + ") OK: reservations = " + numReservations);
        return true;
    }
    */


    // Car operations //

    // Create a new car location or add cars to an existing location.
    // Note: if price <= 0 and the car location already exists, it maintains 
    // its current price.
    @Override
    public boolean addCars(int id, String location, int numCars, int carPrice) {
        Trace.info("RM::addCars(" + id + ", " + location + ", " 
                + numCars + ", $" + carPrice + ") called.");
        Car curObj = (Car) readData(id, Car.getKey(location));
        if (curObj == null) {
            // Doesn't exist; add it.
            Car newObj = new Car(location, numCars, carPrice);
            writeData(id, newObj.getKey(), newObj);
            Trace.info("RM::addCars(" + id + ", " + location + ", " 
                    + numCars + ", $" + carPrice + ") OK.");
        } else {
            // Add count to existing object and update price.
            curObj.setCount(curObj.getCount() + numCars);
            if (carPrice > 0) {
                curObj.setPrice(carPrice);
            }
            writeData(id, curObj.getKey(), curObj);
            Trace.info("RM::addCars(" + id + ", " + location + ", " 
                    + numCars + ", $" + carPrice + ") OK: " 
                    + "cars = " + curObj.getCount() + ", price = $" + carPrice);
        }
        return(true);
    }

    // Delete cars from a location.
    @Override
    public boolean deleteCars(int id, String location) {
        return deleteItem(id, Car.getKey(location));
    }

    // Returns the number of cars available at a location.
    @Override
    public int queryCars(int id, String location) {
        return queryNum(id, Car.getKey(location));
    }

    // Returns price of cars at this location.
    @Override
    public int queryCarsPrice(int id, String location) {
        return queryPrice(id, Car.getKey(location));
    }
    

    // Room operations //

    // Create a new room location or add rooms to an existing location.
    // Note: if price <= 0 and the room location already exists, it maintains 
    // its current price.
    @Override
    public boolean addRooms(int id, String location, int numRooms, int roomPrice) {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " 
                + numRooms + ", $" + roomPrice + ") called.");
        Room curObj = (Room) readData(id, Room.getKey(location));
        if (curObj == null) {
            // Doesn't exist; add it.
            Room newObj = new Room(location, numRooms, roomPrice);
            writeData(id, newObj.getKey(), newObj);
            Trace.info("RM::addRooms(" + id + ", " + location + ", " 
                    + numRooms + ", $" + roomPrice + ") OK.");
        } else {
            // Add count to existing object and update price.
            curObj.setCount(curObj.getCount() + numRooms);
            if (roomPrice > 0) {
                curObj.setPrice(roomPrice);
            }
            writeData(id, curObj.getKey(), curObj);
            Trace.info("RM::addRooms(" + id + ", " + location + ", " 
                    + numRooms + ", $" + roomPrice + ") OK: " 
                    + "rooms = " + curObj.getCount() + ", price = $" + roomPrice);
        }
        return(true);
    }

    // Delete rooms from a location.
    @Override
    public boolean deleteRooms(int id, String location) {
        return deleteItem(id, Room.getKey(location));
    }

    // Returns the number of rooms available at a location.
    @Override
    public int queryRooms(int id, String location) {
        return queryNum(id, Room.getKey(location));
    }
    
    // Returns room price at this location.
    @Override
    public int queryRoomsPrice(int id, String location) {
        return queryPrice(id, Room.getKey(location));
    }


    // Customer operations //

    @Override
    public int newCustomer(int id) {
        Trace.info("INFO: RM::newCustomer(" + id + ") called.");
        // Generate a globally unique Id for the new customer.
        int customerId = Integer.parseInt(String.valueOf(id) +
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf(Math.round(Math.random() * 100 + 1)));
        Customer cust = new Customer(customerId);
        writeData(id, cust.getKey(), cust);
        Trace.info("RM::newCustomer(" + id + ") OK: " + customerId);
        return customerId;
    }

    // This method makes testing easier.
    @Override
    public boolean newCustomerId(int id, int customerId) {
        Trace.info("INFO: RM::newCustomerId(" + id + ", " + customerId + ") called.");
        Customer cust = (Customer) readData(id, Customer.getKey(customerId));
        if (cust == null) {
            cust = new Customer(customerId);
            writeData(id, cust.getKey(), cust);
            Trace.info("INFO: RM::newCustomerId(" + id + ", " + customerId + ") OK.");
            return true;
        } else {
            Trace.info("INFO: RM::newCustomeIdr(" + id + ", " + 
                    customerId + ") failed: customer already exists.");
            return true;
        }
    }

    // Delete customer from the database. 
    @Override
    public boolean deleteCustomer(int id, int customerId) {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") called.");
        Customer cust = (Customer) readData(id, Customer.getKey(customerId));
        if (cust == null) {
            Trace.warn("RM::deleteCustomer(" + id + ", " 
                    + customerId + ") failed: customer doesn't exist.");
            return true;
        } else {            
            // Increase the reserved numbers of all reservable items that 
            // the customer reserved. 
            RMHashtable reservationHT = cust.getReservations();
            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
                String reservedKey = (String) (e.nextElement());
                ReservedItem reservedItem = cust.getReservedItem(reservedKey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerId + "): " 
                        + "deleting " + reservedItem.getCount() + " reservations "
                        + "for item " + reservedItem.getKey());
                ReservableItem item = 
                        (ReservableItem) readData(id, reservedItem.getKey());
                item.setReserved(item.getReserved() - reservedItem.getCount());
                item.setCount(item.getCount() + reservedItem.getCount());
                Trace.info("RM::deleteCustomer(" + id + ", " + customerId + "): "
                        + reservedItem.getKey() + " reserved/available = " 
                        + item.getReserved() + "/" + item.getCount());
            }
            // Remove the customer from the storage.
            removeData(id, cust.getKey());
            Trace.info("RM::deleteCustomer(" + id + ", " + customerId + ") OK.");
            return true;
        }
    }

    // Return data structure containing customer reservation info. 
    // Returns null if the customer doesn't exist. 
    // Returns empty RMHashtable if customer exists but has no reservations.
    public RMHashtable getCustomerReservations(int id, int customerId) {
        Trace.info("RM::getCustomerReservations(" + id + ", " 
                + customerId + ") called.");
        Customer cust = (Customer) readData(id, Customer.getKey(customerId));
        if (cust == null) {
            Trace.info("RM::getCustomerReservations(" + id + ", " 
                    + customerId + ") failed: customer doesn't exist.");
            return null;
        } else {
            return cust.getReservations();
        }
    }

    // Return a bill.
    @Override
    public String queryCustomerInfo(int id, int customerId) {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + ") called.");
        Customer cust = (Customer) readData(id, Customer.getKey(customerId));
        if (cust == null) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " 
                    + customerId + ") failed: customer doesn't exist.");
            // Returning an empty bill means that the customer doesn't exist.
            return "";
        } else {
            String s = cust.printBill();
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerId + "): \n");
            System.out.println(s);
            return s;
        }
    }

    // Add flight reservation to this customer.  
    @Override
    public boolean reserveFlight(int id, int customerId, int flightNumber) {
        return reserveItem(id, customerId, 
                Flight.getKey(flightNumber), String.valueOf(flightNumber));
    }

    // Add car reservation to this customer. 
    @Override
    public boolean reserveCar(int id, int customerId, String location) {
        return reserveItem(id, customerId, Car.getKey(location), location);
    }

    // Add room reservation to this customer. 
    @Override
    public boolean reserveRoom(int id, int customerId, String location) {
        return reserveItem(id, customerId, Room.getKey(location), location);
    }
    

    // Reserve an itinerary.
    @Override
    public boolean reserveItinerary(int id, int customerId, Vector flightNumbers,
                                    String location, boolean car, boolean room) {
        return false;
    }

}
