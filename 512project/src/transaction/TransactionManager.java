package transaction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import middleware.RMmeta;

public class TransactionManager {

	/* random id generator */
	Random random = new Random();
	ConcurrentHashMap<Transaction,List<RMmeta.RMtype>> transactionTable; 
	TTLchecker transactionChecker;
	
	public TransactionManager(){
		transactionTable = new ConcurrentHashMap<Transaction, List<RMmeta.RMtype>>();
		transactionChecker = new TTLchecker(this);
		transactionChecker.run();
	}
	
	/* start a transaction and add to transaction table */
	public Transaction start(){
		Transaction t = new Transaction(random.nextInt(Integer.MAX_VALUE));
		t.addCommand("start");
		transactionTable.put(t,new ArrayList<RMmeta.RMtype>());
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
		t.addCommand("commit");
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
		t.addCommand("abort");
		transactionTable.remove(t);
		return true;
	}
	
	/* record a operation to a transaction 
	 * 
	 * 
	 */
	
	public boolean addOperation(Transaction t, boolean read, RMmeta.RMtype type){
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
		t.addCommand(cmd);
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
			    sleep(10000);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	}
}
