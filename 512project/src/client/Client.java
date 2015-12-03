package client;

import java.util.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;


public class Client{

	Socket clientSocket;
	String _host;
	int _port;
	BufferedReader inFromServer;
	DataOutputStream outToServer;



    public Client(String serviceName, String serviceHost, int servicePort)  throws Exception {
    	_host = serviceHost;
    	_port = servicePort;
		clientSocket = new Socket(_host, _port);
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outToServer = new DataOutputStream(clientSocket.getOutputStream());

    }

    public static void main(String[] args) {
        if (args.length != 3) {
           System.out.println("Usage: MyClient <service-name> <service-host> <service-port>");
           System.exit(-1);
        }

        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);

        try{

        	Client client = new Client(serviceName, serviceHost, servicePort);
            client.run();

        }catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*
     * establish a tcp connection, send the command to server
     * display server output
     */
    public void sendMessage(String str){
    	//try to read and return
    	try{
    		outToServer.writeBytes(str + '\n');
    		String ret = inFromServer.readLine();
    		System.out.println("FROM SERVER: " + ret);
    	}catch (IOException e){
    		e.printStackTrace();
    		System.exit(-1);
    	}
    }

    public void run() {

        int id=0;
        int flightNumber;
        int flightPrice;
        int numSeats;
        boolean room;
        boolean car;
        int price;
        int numRooms;
        int numCars;
        String location;

        String command = "";
        Vector arguments = new Vector();

        BufferedReader stdin =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Client Interface");
        System.out.println("Type \"help\" for list of supported commands");

        while (true) {

            try {
                //read the next command
            	System.out.println("Waiting for next command.");
                command = stdin.readLine();
            }
            catch (IOException io) {
                System.out.println("Unable to read from standard in");
                System.exit(1);
            }
            //remove heading and trailing white space
            command = command.trim();
            arguments = parse(command);

            System.out.println("Command: " + command);
            //decide which of the commands this was
            switch(findChoice((String) arguments.elementAt(0))) {

            case 1: //help section
                if (arguments.size() == 1)   //command was "help"
                    listCommands();
                else if (arguments.size() == 2)  //command was "help <commandname>"
                    listSpecific((String) arguments.elementAt(1));
                else  //wrong use of help command
                    System.out.println("Improper use of help command. Type help or help, <commandname>");
                break;

            case 2:  //new flight
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Flight");
                System.out.println("Flight number: " + arguments.elementAt(1));
                System.out.println("Add Flight Seats: " + arguments.elementAt(2));
                System.out.println("Set Flight Price: " + arguments.elementAt(3));

                try {
                    flightNumber = getInt(arguments.elementAt(1));
                    numSeats = getInt(arguments.elementAt(2));
                    flightPrice = getInt(arguments.elementAt(3));

                    sendMessage(String.format("NewFlight,%d,%d,%d,%d",
                    		id, flightNumber, numSeats, flightPrice));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 3:  //new car
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new car using");
                System.out.println("car Location: " + arguments.elementAt(1));
                System.out.println("Add Number of cars: " + arguments.elementAt(2));
                System.out.println("Set Price: " + arguments.elementAt(3));
                try {
                    location = getString(arguments.elementAt(1));
                    numCars = getInt(arguments.elementAt(2));
                    price = getInt(arguments.elementAt(3));

                    sendMessage(String.format("NewCar,%d,%s,%d,%d",
                    		id, location, numCars, price));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 4:  //new room
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new room using");
                System.out.println("room Location: " + arguments.elementAt(1));
                System.out.println("Add Number of rooms: " + arguments.elementAt(2));
                System.out.println("Set Price: " + arguments.elementAt(3));
                try {
                    location = getString(arguments.elementAt(1));
                    numRooms = getInt(arguments.elementAt(2));
                    price = getInt(arguments.elementAt(3));

                    sendMessage(String.format("NewRoom,%d,%s,%d,%d",
                    		id, location, numRooms, price));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 5:  //new Customer
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Customer using");
                try {
                    sendMessage(String.format("NewCustomer,%d",id));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 6: //delete Flight
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting a flight using");
                System.out.println("Flight Number: " + arguments.elementAt(1));
                try {
                    flightNumber = getInt(arguments.elementAt(1));
                    sendMessage(String.format("DeleteFlight,%d,%d",
                    		id, flightNumber));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 7: //delete car
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting the cars from a particular location");
                System.out.println("car Location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));

                    sendMessage(String.format("DeleteCar,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 8: //delete room
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting all rooms from a particular location");
                System.out.println("room Location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));
                    sendMessage(String.format("DeleteRoom,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 9: //delete Customer
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting a customer from the database");
                System.out.println("Customer id: " + arguments.elementAt(1));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    sendMessage(String.format("DeleteCustomer,%d,%d",
                    		id, customer));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 10: //querying a flight
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a flight");
                System.out.println("Flight number: " + arguments.elementAt(1));
                try {
                    flightNumber = getInt(arguments.elementAt(1));
                    sendMessage(String.format("QueryFlight,%d,%d",
                    		id, flightNumber));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 11: //querying a car Location
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a car location");
                System.out.println("car location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));
                    sendMessage(String.format("QueryCar,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 12: //querying a room location
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a room location");
                System.out.println("room location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));
                    sendMessage(String.format("QueryRoom,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 13: //querying Customer Information
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying Customer information");
                System.out.println("Customer id: " + arguments.elementAt(1));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    sendMessage(String.format("QueryCustomer,%d,%d",
                    		id, customer));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 14: //querying a flight Price
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a flight Price");
                System.out.println("Flight number: " + arguments.elementAt(1));
                try {
                    flightNumber = getInt(arguments.elementAt(1));
                    sendMessage(String.format("QueryFlightPrice,%d,%d",
                    		id, flightNumber));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 15: //querying a car Price
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a car price");
                System.out.println("car location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));
                    sendMessage(String.format("QueryCarPrice,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 16: //querying a room price
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a room price");
                System.out.println("room Location: " + arguments.elementAt(1));
                try {
                    location = getString(arguments.elementAt(1));
                    sendMessage(String.format("QueryRoomPrice,%d,%s",
                    		id, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 17:  //reserve a flight
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a seat on a flight");
                System.out.println("Customer id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    flightNumber = getInt(arguments.elementAt(2));

                    sendMessage(String.format("ReserveFlight,%d,%d,%d",
                    		id, customer, flightNumber));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 18:  //reserve a car
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a car at a location");
                System.out.println("Customer id: " + arguments.elementAt(1));
                System.out.println("Location: " + arguments.elementAt(2));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));

                    sendMessage(String.format("ReserveCar,%d,%d,%s",
                    		id, customer, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 19:  //reserve a room
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a room at a location");
                System.out.println("Customer id: " + arguments.elementAt(1));
                System.out.println("Location: " + arguments.elementAt(2));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    location = getString(arguments.elementAt(2));

                    sendMessage(String.format("ReserveRoom,%d,%d,%s",
                    		id, customer, location));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 20:  //reserve an Itinerary
                if (arguments.size()<6) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving an Itinerary");
                System.out.println("Customer id: " + arguments.elementAt(1));
               for (int i = 0; i<arguments.size()-5; i++)
                    System.out.println("Flight number: " + arguments.elementAt(2 + i));
                System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
                System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
                System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
                try {
                    int customer = getInt(arguments.elementAt(1));
                    Vector flightNumbers = new Vector();
                    for (int i = 0; i < arguments.size()-5; i++)
                        flightNumbers.addElement(arguments.elementAt(2 + i));
                    location = getString(arguments.elementAt(arguments.size()-3));
                    car = getBoolean(arguments.elementAt(arguments.size()-2));
                    room = getBoolean(arguments.elementAt(arguments.size()-1));

                    //Separate flightNumbers as comma
                    String flightNumberString = "";
                    for(int i = 0; i < flightNumbers.size(); i++){
                    	flightNumberString += getInt(flightNumbers.elementAt(i)) + ",";
                    }
                    System.out.println("flightNumberString: " + flightNumberString);
                    if(flightNumberString.length() > 1)
                    	flightNumberString = flightNumberString.substring(0,flightNumberString.length()-1);
                    System.out.println("flightNumberString: " + flightNumberString);

                    sendMessage(String.format("itinerary,%d,%d,%s,%s,%s,%s",
                    		id, customer, flightNumberString, location,
                    		(car ? "true" : "false"), (room ? "true" : "false")));

                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 21:  //quit the client
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Quitting client.");
                return;

            case 22:  //new Customer given id
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Customer" + arguments.elementAt(1));
                try {
                    int customer = getInt(arguments.elementAt(1));

                    sendMessage(String.format("NewCustomerID,%d,%d",
                    		id, customer));
                }
                catch(Exception e) {
                    System.out.println("EXCEPTION: ");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case 23:
            	sendMessage("start");
            	break;
            case 24:
            	sendMessage("commit");
            	break;
            case 25:
            	sendMessage("abort");
            	break;
            case 26:
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                    String rm;
                    try {
                        rm = getString(arguments.elementAt(1));
                        sendMessage(String.format("printRM,%s",rm));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                break;
            default:
                System.out.println("The interface does not support this command.");
                break;
            }
        }
    }

    static public Vector parse(String command) {
        Vector arguments = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(command, ",");
        String argument = "";
        while (tokenizer.hasMoreTokens()) {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }

    static public int findChoice(String argument) {
        if (argument.compareToIgnoreCase("help") == 0)
            return 1;
        else if (argument.compareToIgnoreCase("newflight") == 0)
            return 2;
        else if (argument.compareToIgnoreCase("newcar") == 0)
            return 3;
        else if (argument.compareToIgnoreCase("newroom") == 0)
            return 4;
        else if (argument.compareToIgnoreCase("newcustomer") == 0)
            return 5;
        else if (argument.compareToIgnoreCase("deleteflight") == 0)
            return 6;
        else if (argument.compareToIgnoreCase("deletecar") == 0)
            return 7;
        else if (argument.compareToIgnoreCase("deleteroom") == 0)
            return 8;
        else if (argument.compareToIgnoreCase("deletecustomer") == 0)
            return 9;
        else if (argument.compareToIgnoreCase("queryflight") == 0)
            return 10;
        else if (argument.compareToIgnoreCase("querycar") == 0)
            return 11;
        else if (argument.compareToIgnoreCase("queryroom") == 0)
            return 12;
        else if (argument.compareToIgnoreCase("querycustomer") == 0)
            return 13;
        else if (argument.compareToIgnoreCase("queryflightprice") == 0)
            return 14;
        else if (argument.compareToIgnoreCase("querycarprice") == 0)
            return 15;
        else if (argument.compareToIgnoreCase("queryroomprice") == 0)
            return 16;
        else if (argument.compareToIgnoreCase("reserveflight") == 0)
            return 17;
        else if (argument.compareToIgnoreCase("reservecar") == 0)
            return 18;
        else if (argument.compareToIgnoreCase("reserveroom") == 0)
            return 19;
        else if (argument.compareToIgnoreCase("itinerary") == 0)
            return 20;
        else if (argument.compareToIgnoreCase("quit") == 0)
            return 21;
        else if (argument.compareToIgnoreCase("newcustomerid") == 0)
            return 22;
        else if (argument.compareToIgnoreCase("start") == 0)
        	return 23;
        else if (argument.compareToIgnoreCase("commit") == 0)
        	return 24;
        else if (argument.compareToIgnoreCase("abort") == 0)
        	return 25;
        else if (argument.compareToIgnoreCase("printRM") == 0)
          //for test only
            return 26;
        else if (argument.compareToIgnoreCase("vote") == 0)
            //for middleware and rm use only
            return 27;
				else if (argument.compareToIgnoreCase("setCrashCaseRM") == 0)
						return 28;
        else
            return 666;
    }

    public void listCommands() {
        System.out.println("\nWelcome to the client interface provided to test your project.");
        System.out.println("Commands accepted by the interface are: ");
        System.out.println("help");
        System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
        System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
        System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
        System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
        System.out.println("start\ncommit\nabort");
        System.out.println("quit");
        System.out.println("\ntype help, <commandname> for detailed info (note the use of comma).");
    }


    public void listSpecific(String command) {
        System.out.print("Help on: ");
        switch(findChoice(command)) {
            case 1:
            System.out.println("Help");
            System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
            System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
            break;

            case 2:  //new flight
            System.out.println("Adding a new Flight.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewflight, <id>, <flightnumber>, <numSeats>, <flightprice>");
            break;

            case 3:  //new car
            System.out.println("Adding a new car.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewcar, <id>, <location>, <numberofcars>, <pricepercar>");
            break;

            case 4:  //new room
            System.out.println("Adding a new room.");
            System.out.println("Purpose: ");
            System.out.println("\tAdd information about a new room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tnewroom, <id>, <location>, <numberofrooms>, <priceperroom>");
            break;

            case 5:  //new Customer
            System.out.println("Adding a new Customer.");
            System.out.println("Purpose: ");
            System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
            System.out.println("\nUsage: ");
            System.out.println("\tnewcustomer, <id>");
            break;


            case 6: //delete Flight
            System.out.println("Deleting a flight");
            System.out.println("Purpose: ");
            System.out.println("\tDelete a flight's information.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeleteflight, <id>, <flightnumber>");
            break;

            case 7: //delete car
            System.out.println("Deleting a car");
            System.out.println("Purpose: ");
            System.out.println("\tDelete all cars from a location.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeletecar, <id>, <location>, <numCars>");
            break;

            case 8: //delete room
            System.out.println("Deleting a room");
            System.out.println("\nPurpose: ");
            System.out.println("\tDelete all rooms from a location.");
            System.out.println("Usage: ");
            System.out.println("\tdeleteroom, <id>, <location>, <numRooms>");
            break;

            case 9: //delete Customer
            System.out.println("Deleting a Customer");
            System.out.println("Purpose: ");
            System.out.println("\tRemove a customer from the database.");
            System.out.println("\nUsage: ");
            System.out.println("\tdeletecustomer, <id>, <customerid>");
            break;

            case 10: //querying a flight
            System.out.println("Querying flight.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain Seat information about a certain flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryflight, <id>, <flightnumber>");
            break;

            case 11: //querying a car Location
            System.out.println("Querying a car location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain number of cars at a certain car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycar, <id>, <location>");
            break;

            case 12: //querying a room location
            System.out.println("Querying a room Location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain number of rooms at a certain room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryroom, <id>, <location>");
            break;

            case 13: //querying Customer Information
            System.out.println("Querying Customer Information.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain information about a customer.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycustomer, <id>, <customerid>");
            break;

            case 14: //querying a flight for price
            System.out.println("Querying flight.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain flight.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryflightprice, <id>, <flightnumber>");
            break;

            case 15: //querying a car Location for price
            System.out.println("Querying a car location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain car location.");
            System.out.println("\nUsage: ");
            System.out.println("\tquerycarprice, <id>, <location>");
            break;

            case 16: //querying a room location for price
            System.out.println("Querying a room Location.");
            System.out.println("Purpose: ");
            System.out.println("\tObtain price information about a certain room location.");
            System.out.println("\nUsage: ");
            System.out.println("\tqueryroomprice, <id>, <location>");
            break;

            case 17:  //reserve a flight
            System.out.println("Reserving a flight.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a flight for a customer.");
            System.out.println("\nUsage: ");
            System.out.println("\treserveflight, <id>, <customerid>, <flightnumber>");
            break;

            case 18:  //reserve a car
            System.out.println("Reserving a car.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a given number of cars for a customer at a particular location.");
            System.out.println("\nUsage: ");
            System.out.println("\treservecar, <id>, <customerid>, <location>, <nummberofcars>");
            break;

            case 19:  //reserve a room
            System.out.println("Reserving a room.");
            System.out.println("Purpose: ");
            System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
            System.out.println("\nUsage: ");
            System.out.println("\treserveroom, <id>, <customerid>, <location>, <nummberofrooms>");
            break;

            case 20:  //reserve an Itinerary
            System.out.println("Reserving an Itinerary.");
            System.out.println("Purpose: ");
            System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
            System.out.println("\nUsage: ");
            System.out.println("\titinerary, <id>, <customerid>, "
                    + "<flightnumber1>....<flightnumberN>, "
                    + "<LocationToBookcarsOrrooms>, <NumberOfcars>, <NumberOfroom>");
            break;


            case 21:  //quit the client
            System.out.println("Quitting client.");
            System.out.println("Purpose: ");
            System.out.println("\tExit the client application.");
            System.out.println("\nUsage: ");
            System.out.println("\tquit");
            break;

            case 22:  //new customer with id
            System.out.println("Create new customer providing an id");
            System.out.println("Purpose: ");
            System.out.println("\tCreates a new customer with the id provided");
            System.out.println("\nUsage: ");
            System.out.println("\tnewcustomerid, <id>, <customerid>");
            break;

            case 23: //start transaction
            System.out.println("Create new transaction");
            break;

            case 24: //commit transaction
            System.out.println("Commit transaction");
            break;

            case 25: //abort transaction
            System.out.println("Abort transaction");
            break;

            case 26:
            System.out.println("Print information abourt RM");
            break;

            default:
            System.out.println(command);
            System.out.println("The interface does not support this command.");
            break;
        }
    }

    public void wrongNumber() {
        System.out.println("The number of arguments provided in this command are wrong.");
        System.out.println("Type help, <commandname> to check usage of this command.");
    }

    static public int getInt(Object temp) throws Exception {
        try {
            return (new Integer((String)temp)).intValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    static public boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
        }
        catch(Exception e) {
        	throw e;
        }
    }

    static public String getString(Object temp) throws Exception {
        try {
            return (String)temp;
        }
        catch (Exception e) {
            throw e;
        }
    }

    /*
     * method used for client test, where it parse a cmd string and send to server
     */
    public void sendCommandToServer(String cmd){
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

        String command = cmd;
        Vector arguments = new Vector();
        //remove heading and trailing white space
        command = command.trim();
        arguments = parse(command);
        //decide which of the commands this was
        switch(findChoice((String) arguments.elementAt(0))) {

        case 1: //help section
            if (arguments.size() == 1)   //command was "help"
                listCommands();
            else if (arguments.size() == 2)  //command was "help <commandname>"
                listSpecific((String) arguments.elementAt(1));
            else  //wrong use of help command
                System.out.println("Improper use of help command. Type help or help, <commandname>");
            break;

        case 2:  //new flight
            if (arguments.size() != 5) {
                wrongNumber();
                break;
            }
            System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
            System.out.println("Flight number: " + arguments.elementAt(2));
            System.out.println("Add Flight Seats: " + arguments.elementAt(3));
            System.out.println("Set Flight Price: " + arguments.elementAt(4));

            try {
                id = getInt(arguments.elementAt(1));
                flightNumber = getInt(arguments.elementAt(2));
                numSeats = getInt(arguments.elementAt(3));
                flightPrice = getInt(arguments.elementAt(4));

                sendMessage(String.format("NewFlight,%d,%d,%d,%d",
                		id, flightNumber, numSeats, flightPrice));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 3:  //new car
            if (arguments.size() != 5) {
                wrongNumber();
                break;
            }
            System.out.println("Adding a new car using id: " + arguments.elementAt(1));
            System.out.println("car Location: " + arguments.elementAt(2));
            System.out.println("Add Number of cars: " + arguments.elementAt(3));
            System.out.println("Set Price: " + arguments.elementAt(4));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numCars = getInt(arguments.elementAt(3));
                price = getInt(arguments.elementAt(4));

                sendMessage(String.format("NewCar,%d,%s,%d,%d",
                		id, location, numCars, price));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 4:  //new room
            if (arguments.size() != 5) {
                wrongNumber();
                break;
            }
            System.out.println("Adding a new room using id: " + arguments.elementAt(1));
            System.out.println("room Location: " + arguments.elementAt(2));
            System.out.println("Add Number of rooms: " + arguments.elementAt(3));
            System.out.println("Set Price: " + arguments.elementAt(4));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numRooms = getInt(arguments.elementAt(3));
                price = getInt(arguments.elementAt(4));

                sendMessage(String.format("NewRoom,%d,%s,%d,%d",
                		id, location, numRooms, price));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 5:  //new Customer
            if (arguments.size() != 2) {
                wrongNumber();
                break;
            }
            System.out.println("Adding a new Customer using id: " + arguments.elementAt(1));
            try {
                id = getInt(arguments.elementAt(1));
                sendMessage(String.format("NewCustomer,%d",id));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 6: //delete Flight
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Deleting a flight using id: " + arguments.elementAt(1));
            System.out.println("Flight Number: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                flightNumber = getInt(arguments.elementAt(2));

                sendMessage(String.format("DeleteFlight,%d,%d",
                		id, flightNumber));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 7: //delete car
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
            System.out.println("car Location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("DeleteCar,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 8: //delete room
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
            System.out.println("room Location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("DeleteRoom,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 9: //delete Customer
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));

                sendMessage(String.format("DeleteCustomer,%d,%d",
                		id, customer));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 10: //querying a flight
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a flight using id: " + arguments.elementAt(1));
            System.out.println("Flight number: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                flightNumber = getInt(arguments.elementAt(2));

                sendMessage(String.format("QueryFlight,%d,%d",
                		id, flightNumber));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 11: //querying a car Location
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a car location using id: " + arguments.elementAt(1));
            System.out.println("car location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("QueryCar,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 12: //querying a room location
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a room location using id: " + arguments.elementAt(1));
            System.out.println("room location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("QueryRoom,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 13: //querying Customer Information
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));

                sendMessage(String.format("QueryCustomer,%d,%d",
                		id, customer));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 14: //querying a flight Price
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
            System.out.println("Flight number: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                flightNumber = getInt(arguments.elementAt(2));

                sendMessage(String.format("QueryFlightPrice,%d,%d",
                		id, flightNumber));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 15: //querying a car Price
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a car price using id: " + arguments.elementAt(1));
            System.out.println("car location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("QueryCarPrice,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 16: //querying a room price
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Querying a room price using id: " + arguments.elementAt(1));
            System.out.println("room Location: " + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                sendMessage(String.format("QueryRoomPrice,%d,%s",
                		id, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 17:  //reserve a flight
            if (arguments.size() != 4) {
                wrongNumber();
                break;
            }
            System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            System.out.println("Flight number: " + arguments.elementAt(3));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));
                flightNumber = getInt(arguments.elementAt(3));

                sendMessage(String.format("ReserveFlight,%d,%d,%d",
                		id, customer, flightNumber));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 18:  //reserve a car
            if (arguments.size() != 4) {
                wrongNumber();
                break;
            }
            System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            System.out.println("Location: " + arguments.elementAt(3));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));
                location = getString(arguments.elementAt(3));

                sendMessage(String.format("ReserveCar,%d,%d,%s",
                		id, customer, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 19:  //reserve a room
            if (arguments.size() != 4) {
                wrongNumber();
                break;
            }
            System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            System.out.println("Location: " + arguments.elementAt(3));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));
                location = getString(arguments.elementAt(3));

                sendMessage(String.format("ReserveRoom,%d,%d,%s",
                		id, customer, location));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 20:  //reserve an Itinerary
            if (arguments.size()<7) {
                wrongNumber();
                break;
            }
            System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
            System.out.println("Customer id: " + arguments.elementAt(2));
            for (int i = 0; i<arguments.size()-6; i++)
                System.out.println("Flight number: " + arguments.elementAt(3 + i));
            System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
            System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
            System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));
                Vector flightNumbers = new Vector();
                for (int i = 0; i < arguments.size()-6; i++)
                    flightNumbers.addElement(arguments.elementAt(3 + i));
                location = getString(arguments.elementAt(arguments.size()-3));
                car = getBoolean(arguments.elementAt(arguments.size()-2));
                room = getBoolean(arguments.elementAt(arguments.size()-1));

                //Separate flightNumbers as comma
                String flightNumberString = "";
                for(int i = 0; i < flightNumbers.size(); i++){
                	flightNumberString += getInt(flightNumbers.elementAt(i)) + ",";
                }
                System.out.println("flightNumberString: " + flightNumberString);
                if(flightNumberString.length() > 1)
                	flightNumberString = flightNumberString.substring(0,flightNumberString.length()-1);
                System.out.println("flightNumberString: " + flightNumberString);

                sendMessage(String.format("itinerary,%d,%d,%s,%s,%s,%s",
                		id, customer, flightNumberString, location,
                		(car ? "true" : "false"), (room ? "true" : "false")));

            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 21:  //quit the client
            if (arguments.size() != 1) {
                wrongNumber();
                break;
            }
            System.out.println("Quitting client.");
            return;

        case 22:  //new Customer given id
            if (arguments.size() != 3) {
                wrongNumber();
                break;
            }
            System.out.println("Adding a new Customer using id: "
                    + arguments.elementAt(1)  +  " and cid "  + arguments.elementAt(2));
            try {
                id = getInt(arguments.elementAt(1));
                int customer = getInt(arguments.elementAt(2));

                sendMessage(String.format("NewCustomerID,%d,%d",
                		id, customer));
            }
            catch(Exception e) {
                System.out.println("EXCEPTION: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            break;

        case 23:
        	sendMessage("start");
        	break;
        case 24:
        	sendMessage("commit");
        	break;
        case 25:
        	sendMessage("abort");
        	break;
        case 26:
            if (arguments.size() != 2) {
                wrongNumber();
                break;
            }
                String rm;
                try {
                    rm = getString(arguments.elementAt(1));
                    sendMessage(String.format("printRM,%s",rm));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            break;
        default:
            System.out.println("The interface does not support this command.");
            break;
        }
    }
}
