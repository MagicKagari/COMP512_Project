package transaction;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import middleware.Middleware;
import middleware.RMmeta;

public class TransactionManager {

	/* random id generator */
	Random random = new Random();
	ConcurrentHashMap<Transaction,List<RMmeta.RMtype>> transactionTable; 
	TTLchecker transactionChecker;
	Middleware middleware;
	
	public TransactionManager(Middleware middleware){
		this.middleware = middleware;
		transactionTable = new ConcurrentHashMap<Transaction, List<RMmeta.RMtype>>();
		transactionChecker = new TTLchecker(this);
		transactionChecker.start();
	}
	
	/* start a transaction and add to transaction table */
	public Transaction start(){
		Transaction t = new Transaction(random.nextInt(Integer.MAX_VALUE));
		t.addCommand("start","start");
		ArrayList<RMmeta.RMtype> types = new ArrayList<RMmeta.RMtype>();
		transactionTable.put(t,types);
		types.add(null);
		
		List<RMmeta> rms = middleware.resourceManagers;
		for(RMmeta rm : rms){
		    Socket s = rm.getSocket();
		    synchronized (s) {
		        try{
                    BufferedReader inFromServer = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                    outToServer.writeBytes("start,"+t.getId() + '\n');      
                    System.out.println(inFromServer.readLine());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
		}
		
		return t;
	}
	
	/* commit a transaction, return true if success, false if not */
	public boolean commit(Transaction t){
		if(!transactionTable.keySet().contains(t)){
			System.out.println("Oops where does this transaction come from. "
					+ t.toString());
			return false;
		}
		System.out.println("COMMIT: "+t.toString());
		t.addCommand("commit","commit");
		
		List<RMmeta> rms = middleware.resourceManagers;
        for(RMmeta rm : rms){
            Socket s = rm.getSocket();
            synchronized (s) {
                try{
                    BufferedReader inFromServer = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                    outToServer.writeBytes("commit,"+t.getId() + '\n');      
                    System.out.println(inFromServer.readLine());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
		
		transactionTable.remove(t);
		return true;
	}
	
	/* abort a transaction, return true if success, false if not */
	public boolean abort(Transaction t){
		if(!transactionTable.keySet().contains(t)){
			System.out.println("Oops where does this transaction come from. "
					+ t.toString());
			return false;
		}
		this.middleware.lockManager.UnlockAll(t.getId());
		System.out.println("ABORTING: "+t.toString());
		
		List<RMmeta> rms = middleware.resourceManagers;
        for(RMmeta rm : rms){
            Socket s = rm.getSocket();
            synchronized (s) {
                try{
                    BufferedReader inFromServer = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                    outToServer.writeBytes("abort,"+t.getId() + '\n');      
                    System.out.println(inFromServer.readLine());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
		/*
		//rollback commands
		for(int i=0; i<t.entireCommands.size(); i++){
			String command = t.entireCommands.get(i);
			RMmeta.RMtype type = transactionTable.get(t).get(i);
			String offset;
			if(command.contains("New")){
				offset = command.replace("New","Delete");
				String offsets[] = offset.split(",");
				offset = String.format("%s,%s,%s",offsets[0], offsets[1], offsets[2]);
				RMmeta rm = middleware.getResourceManagerOfType(type);
				Socket handler = rm.getSocket();
				synchronized (handler) {
					try{
						BufferedReader inFromServer = new BufferedReader(
				    			new InputStreamReader(handler.getInputStream()));
				   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
				   		outToServer.writeBytes(offset + '\n'); 		
				   		String ret = inFromServer.readLine();
				   	}catch(Exception e){
				   		e.printStackTrace();
				   	}
				} 
				continue;
			}
			
			if(command.contains("Delete")){
				offset = command.replace("Delete","New")+",1,1";
				RMmeta rm = middleware.getResourceManagerOfType(type);
				Socket handler = rm.getSocket();
				synchronized (handler) {
					try{
						BufferedReader inFromServer = new BufferedReader(
				    			new InputStreamReader(handler.getInputStream()));
				   		DataOutputStream outToServer = new DataOutputStream(handler.getOutputStream());
				   		outToServer.writeBytes(offset + '\n'); 		
				   		String ret = inFromServer.readLine();
				   	}catch(Exception e){
				   		e.printStackTrace();
				   	}
				}
				continue;
			}
		}
		*/
		t.addCommand("abort","abort");
		transactionTable.remove(t);
		return true;
	}
	
	/* record a operation to a transaction */
	public boolean addOperation(Transaction t, boolean read, RMmeta.RMtype type, String command){
		if(!transactionTable.keySet().contains(t)){
			System.out.println("Oops where does this transaction come from. "
					+ t.toString());
			return false;
		}
		String cmd;
		if(read){
			cmd = String.format("read(%s)",type.toString());
		}else{
			cmd = String.format("write(%s)",type.toString());
		}
		t.addCommand(cmd, command);
		transactionTable.get(t).add(type);
		return true;
	}
}

class TTLchecker extends Thread{	
	TransactionManager tm;
	ConcurrentHashMap<Transaction,List<RMmeta.RMtype>> transactionTable; 
	TTLchecker(TransactionManager tm){
		this.tm = tm;
		this.transactionTable = tm.transactionTable;
	}
	
	public void run(){
		while(true){
		    /* critical section, check TTL for each transaction */
		    for(Transaction t : transactionTable.keySet()){
			    if(t.isExpired()) tm.abort(t);
		    }
		    try {
		    	sleep(Transaction.TTL);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	}
}
