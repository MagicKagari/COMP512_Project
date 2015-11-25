package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import LockManager.LockManager;
import middleware.RMmeta.RMtype;
import transaction.TransactionManager;


public class Middleware {

	final int CONNECTION_LIMIT = 100;
	
    int _port;
	String _host;
    public List<RMmeta> resourceManagers;
    
    ExecutorService executorService;
    public LockManager lockManager;
    TransactionManager transactionManager;
	ServerSocket mainListener;

	/* constructor */
	public Middleware(String host, int port){
		
		resourceManagers = new LinkedList<RMmeta>();
		transactionManager = new TransactionManager(this);
		executorService = Executors.newFixedThreadPool(CONNECTION_LIMIT);
		lockManager = new LockManager();
		_port = port;
		_host = host;
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
	
	public RMmeta getResourceManagerOfType(String string){
		return getResourceManagerOfType(getRMtype(string));
	}
	
	/*
	 * loop function that waiting for client connection,
	 * create socket to handle it then 
	 */
	@SuppressWarnings("unchecked")
	public void run() throws IOException{
		try {
			mainListener = new ServerSocket(_port, CONNECTION_LIMIT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Waiting for client connection.");
		
		while(true){
			/*
			 * waiting for a connection from client
			 */
			Socket connectionSocket = mainListener.accept();
			System.out.println("MS: Connection come in");
			executorService.submit(new ClientHandler(connectionSocket, this));
		}	
	}
	
	/*
	 * return a RMtype according to sendMessage defined in Client class
	 */
	public RMtype getRMtype(String str){
		if(str.contains("Flight")||str.contains("flight")){
			return RMtype.flight;
		}else if(str.contains("Car")||str.contains("car")){
			return RMtype.car;
		}else if(str.contains("Room")||str.contains("room")){
			return RMtype.room;
		}else if(str.contains("Customer")||str.contains("customer")){
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
        
        System.out.println(String.format("Starting middleware services on %s %s",
        		serviceHost, servicePort));
		
        Middleware middleware = new Middleware(serviceHost,servicePort);
		
		middleware.addResourceManager();
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
