package tests;

import java.util.ArrayList;
import java.util.Random;

import client.Client;

/*
 * test class for performance analysis
 */

public class ClientTest {

	ArrayList<ClientRunner> clients;
	final int CLIENT_SIZE = 2;
	
	public ClientTest(String host, int port, int waitTime){
		clients = new ArrayList<ClientRunner>(CLIENT_SIZE);
		for(int i=0; i<CLIENT_SIZE; i++){
			try {
				clients.add(new ClientRunner(new Client("client", host, port), i, waitTime));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void startTest(){
		for(ClientRunner c : clients){
			c.start();
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 4) {
           System.out.println("Usage: ClientTest <service-name> <service-host> <service-port> <wait-time>");
           System.exit(-1);
        }
            
        String serviceName = args[0];
        String serviceHost = args[1];
        int servicePort = Integer.parseInt(args[2]);
        int waitTime = Integer.parseInt(args[3]);
	        
		ClientTest clientTest = new ClientTest(serviceHost, servicePort, waitTime);
		clientTest.startTest();
		while(true){
			
		}
	}
}

class ClientRunner extends Thread{

	Client client;
	int id;
	int WAIT_TIME;
	ArrayList<String> commandSet;
	
	public ClientRunner(Client client, int id, int waitTime){
		this.client = client;
		this.id = id;
		WAIT_TIME = waitTime;
		commandSet = new ArrayList<String>();
	}
	
	private void generateCommands(int count){
		Random r = new Random();
		int i = id*100+count;
		commandSet.add("start");
		
		commandSet.add(String.format("newcustomerid,%d,%d",i,i));
		commandSet.add(String.format("newcar,%d,%d,%d,%d",i, i, i, i));
		commandSet.add(String.format("newflight,%d,%d,%d,%d", i, i, i, i));
		commandSet.add(String.format("newroom,%d,%d,%d,%d", i, i, i, i));
		
		commandSet.add(String.format("queryroom,%d,%d", i, i));
		commandSet.add(String.format("queryflight,%d,%d", i, i));
		commandSet.add(String.format("querycar,%d,%d", i, i));
		commandSet.add(String.format("queryroomprice,%d,%d", i, i));
		commandSet.add(String.format("querycarprice,%d,%d", i, i));
		
		commandSet.add(String.format("reserveroom,%d,%d,%d",i, i, i));
		commandSet.add(String.format("reservecar,%d,%d,%d",i, i, i));
		commandSet.add(String.format("reserveflight,%d,%d,%d",i, i, i));
		
		commandSet.add(String.format("querycustomer,%d,%d",i, i));
		
		// 75% chance we commit
		if(r.nextInt(20) > 5){
			commandSet.add("commit");
		}else{
			commandSet.add("abort");
		}
	}
	
	@Override
	public void run() {
		int counter = 0;
		Random r = new Random();
		while(counter < 100){
			this.generateCommands(counter);
			long startTime = System.currentTimeMillis();
			for(String s : commandSet){
				client.sendCommandToServer(s);
			}
			long time = System.currentTimeMillis() - startTime;
			System.out.println("finish test client: " + this.id + " in: " + time);	
			try {
				sleep(time + r.nextInt(WAIT_TIME) + WAIT_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
		}
	}
	
}
