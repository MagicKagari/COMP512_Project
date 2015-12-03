package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import LockManager.DeadlockException;
import LockManager.LockManager;
import transaction.Transaction;
import transaction.TransactionManager;
import client.Client;

public class ClientHandler implements Callable{

	Socket clientSocket;
	Middleware middleware;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	Transaction transaction;
	TransactionManager transactionManager;
	LockManager lockManager;

	public ClientHandler(Socket socket, Middleware mw){
		middleware = mw;
		clientSocket = socket;
		transactionManager = mw.transactionManager;
		lockManager = mw.lockManager;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToClient = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	@Override
	public Object call() throws Exception {
		while(true){
			//read in command send from client  
			String clientCommand = inFromClient.readLine();  
			if(clientCommand == null){
				break;
			}
			
			System.out.println("Received: " + clientCommand);            
			//start a transaction
			if(clientCommand.equals("start")){
				transaction = transactionManager.start();
				synchronized (outToClient) {
				    outToClient.writeBytes("start transaction\n");
                }
				continue;
			}
			
			//inform customer to start transaction first
			if(transaction == null){
				transaction = transactionManager.start();
				synchronized (outToClient) {
				    outToClient.writeBytes("start transaction\n");
                }
				continue;
			}
			
			if(clientCommand.equals("commit")){
				boolean ret = transactionManager.commit(transaction);
				synchronized (outToClient) {
				    if(ret){
    					outToClient.writeBytes("commit transaction success\n");
    				}else{
    					outToClient.writeBytes("commit transaction failed\n");
    				}
				}
				lockManager.UnlockAll(transaction.getId());
				transaction = null;
				continue;
			}
			
			if(clientCommand.equals("abort")){
				boolean ret = transactionManager.abort(transaction);
				synchronized (outToClient) {
				    if(ret){
    					outToClient.writeBytes("abort transaction success\n");
    				}else{
    					outToClient.writeBytes("abort transaction failed\n");
    				}
    			}
				lockManager.UnlockAll(transaction.getId());
				transaction = null;
				continue;
			}
			
			//switch second argument id to transaction id
			String clientCmds[] = clientCommand.split(",");
			clientCmds[1] = String.valueOf(transaction.getId());
			clientCommand = clientCmds[0];
			for(int i=1; i<clientCmds.length;i++){
			    clientCommand += ','+clientCmds[i];
			}
			
			if(clientCmds.length > 0){
			    //test command for print rm
			    if(clientCmds[0].equals("printRM")){
			        String rm_to_print = clientCmds[1];
			        RMmeta rm_to_print_meta = middleware.getResourceManagerOfType(rm_to_print);
			        Socket s = rm_to_print_meta.getSocket();
			        synchronized (s) {
                        BufferedReader inFromServer = new BufferedReader(
                                new InputStreamReader(s.getInputStream()));
                        DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                        outToServer.writeBytes("printRM\n");
                        String ret = inFromServer.readLine();
                        outToClient.writeBytes("printRM\n");
                    }
			        continue;
			    }
			    //crash command for crashing a component
			    if(clientCmds[0].equals("crash")){
			        
			        RMmeta rm_to_crash;
			        Socket s;
			        switch(clientCmds[2]){
			            case "middleware":
			                break;
			            case "car":
			                rm_to_crash = middleware.getResourceManagerOfType("car");
			                s = rm_to_crash.getSocket();
			                synchronized (s) {
			                    BufferedReader inFromServer = new BufferedReader(
		                                new InputStreamReader(s.getInputStream()));
		                        DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
		                        outToServer.writeBytes("crash");
		                        inFromServer.readLine();
                            }
			                break;
			            case "room":
			                rm_to_crash = middleware.getResourceManagerOfType("room");
                            s = rm_to_crash.getSocket();
                            synchronized (s) {
                                BufferedReader inFromServer = new BufferedReader(
                                        new InputStreamReader(s.getInputStream()));
                                DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                                outToServer.writeBytes("crash");
                                inFromServer.readLine();
                            }
			                break;
			            case "flight":
			                rm_to_crash = middleware.getResourceManagerOfType("flight");
                            s = rm_to_crash.getSocket();
                            synchronized (s) {
                                BufferedReader inFromServer = new BufferedReader(
                                        new InputStreamReader(s.getInputStream()));
                                DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                                outToServer.writeBytes("crash");
                                inFromServer.readLine();
                            }
			                break;
			            default:
			                break;
			        }
			        continue;
			    }
			    
				//decode which RM to send to
				RMmeta desiredRM = middleware.getResourceManagerOfType(clientCmds[0]);
				//if command is relate to customer or iternary reserve
				if(desiredRM == null){
					String firstWord = clientCmds[0];
					//special case where the option is related to customer
					//it will be broadcast to all RMs
					if(firstWord.contains("Customer") || firstWord.contains("customer")){
						//another special case is where we newCustomer
						//in this case, generate a id and use newcustomerid for other RMs
						if(firstWord.compareToIgnoreCase("newcustomer") == 0){
							String customerId = String.valueOf((new Random()).nextInt(Integer.MAX_VALUE));
							//call newCustomerID to all other RMs
							String ret = "";
							for(RMmeta rm : middleware.resourceManagers){
								Socket handler = rm.getSocket();
								synchronized (handler) {
									BufferedReader inFromServer = new BufferedReader(
							    			new InputStreamReader(handler.getInputStream()));
							   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
							   		outToServer.writeBytes(String.format("newCustomerID,%d,%s", transaction.getId(), customerId) + "\n");
							   		ret += inFromServer.readLine();
								}
							}				
							outToClient.writeBytes(String.format("Customer %d id : %s", transaction.getId(), customerId) + "\n");
							continue;
						}else{
							String ret = "";
							for(RMmeta rm : middleware.resourceManagers){
								Socket handler = rm.getSocket();
								synchronized (handler) {
									BufferedReader inFromServer = new BufferedReader(
							    			new InputStreamReader(handler.getInputStream()));
							   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
							   		outToServer.writeBytes(clientCommand + "\n");
							   		ret += inFromServer.readLine();
								}
								
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
//				         System.out.println("Reserving an Itinerary using id: " + arguments.elementAt(1));
//			             System.out.println("Customer id: " + arguments.elementAt(2));
//			             for (int i = 0; i<arguments.size()-6; i++)
//			                 System.out.println("Flight number: " + arguments.elementAt(3 + i));
//			             System.out.println("Location for car/room booking: " + arguments.elementAt(arguments.size()-3));
//			             System.out.println("car to book?: " + arguments.elementAt(arguments.size()-2));
//			             System.out.println("room to book?: " + arguments.elementAt(arguments.size()-1));
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

			                 RMmeta rm = middleware.getResourceManagerOfType("Flight");
			                 if(rm != null){
			                	 System.out.println("Handle flight itinerary");
			                	 handler = rm.getSocket();
			                	 synchronized (handler) {
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
				             	 
			                 }
			                 
			                 if(car){
			                	 rm = middleware.getResourceManagerOfType("Car");
			                	 if(rm != null){
			                		 System.out.println("Handle car itinerary");
				                	 
			                		 handler = rm.getSocket();
			                		 synchronized (handler) {
			                			 BufferedReader inFromServer = new BufferedReader(
									    			new InputStreamReader(handler.getInputStream()));
				                		 DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
									   	 command = String.format("ReserveCar,%d,%d,%s", id, customer, location);
									   	 outToServer.writeBytes(command + '\n');
									   	 ret += inFromServer.readLine();
									}
			                		 
			                	 }
			                 }
			                 
			                 if(room){
			                	 rm = middleware.getResourceManagerOfType("Room");
			                	 if(rm != null){
			                		 System.out.println("Handle room itinerary");
				                	 
			                		 handler = rm.getSocket();
			                		 synchronized (handler) {
			                			 BufferedReader inFromServer = new BufferedReader(
									    			new InputStreamReader(handler.getInputStream()));
				                		 DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
									   	 command = String.format("ReserveRoom,%d,%d,%s", id, customer, location);
									   	 outToServer.writeBytes(command + '\n');
									   	 ret += inFromServer.readLine();
									}
			                		 
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
				
				//otherwise is the general client command
				//record operation in transaction
				System.out.println("Add operation to transaction: "+ transaction.getId());
				boolean read = false;
				if(clientCommand.contains("query") || clientCommand.contains("Query")){
					read = true;
				}
				transactionManager.addOperation(transaction,read,desiredRM.getRMtype(),clientCommand);
				System.out.println(transaction.toString());
				
				//start requesting RM
				System.out.println("Requesting RM: " + desiredRM.toString());
				Socket handler = desiredRM.getSocket();
				//get the lock
				System.out.println("Obtaining lock.");
				try{
				    if(read){
					    lockManager.Lock(transaction.getId(),desiredRM.getRMtype().toString(),
						    	LockManager.READ);
				    }else{
					    lockManager.Lock(transaction.getId(),desiredRM.getRMtype().toString(),
						    	LockManager.WRITE);
				    }
				}catch(DeadlockException e){
					System.out.println("Deadlock");
					transactionManager.abort(transaction);
					lockManager.UnlockAll(transaction.getId());
				}
				//get RM's response	
				System.out.println("Requesting RM response.");
				String ret = null;
				synchronized (handler) {
					BufferedReader inFromServer = new BufferedReader(
			    			new InputStreamReader(handler.getInputStream()));
			   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
			   		outToServer.writeBytes(clientCommand + '\n'); 		
			   		ret = inFromServer.readLine();
				} 
	   		System.out.println("FROM RM SERVER: " + ret);
				
		   		if(ret == null) ret = "empty";
				outToClient.writeBytes(ret + '\n');
				System.out.println("Finish writing back to client.");
				
			}else{
				System.out.println("Wrong command.");
				break;
			}
			
		}
		return null;
		
	}
}
