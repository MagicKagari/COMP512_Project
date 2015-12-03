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
		synchronized (rms) {
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
		synchronized (rms) {
		    boolean result = true;
		    //ask each rm to vote
            for(RMmeta rm : rms){
                Socket s = rm.getSocket();
                synchronized (s) {
                    try{
                        BufferedReader inFromServer = new BufferedReader(
                                new InputStreamReader(s.getInputStream()));
                        DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                        outToServer.writeBytes("vote,"+t.getId()+'\n');
                        String ret = inFromServer.readLine();
                        //if any return null(crash) or no then result is false
                        if(ret==null || ret.compareToIgnoreCase("no")==0) result = false;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            
            //if all vote yes, then commit
            if(result){
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
                t.addCommand("commit","commit");
            //if any vote no, then abort    
            }else{
                System.out.println("ABORTING: "+t.toString());
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
                this.middleware.lockManager.UnlockAll(t.getId());
                t.addCommand("abort","abort");
                
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
		System.out.println("ABORTING: "+t.toString());
		
		List<RMmeta> rms = middleware.resourceManagers;
		synchronized (rms) {
           
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
        }
		
		this.middleware.lockManager.UnlockAll(t.getId());
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
