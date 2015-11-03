package transaction;

import java.util.*;

import org.apache.tomcat.jni.Time;

public class Transaction {

	/* TTL constant in ms */
	final long TTL = 2000000; //2000 secs
	
	long startTime;
	int t_id;
	boolean isFinished;
	ArrayList<String> commands;
	
	public Transaction(int id){
		t_id = id;
		isFinished = false;
		commands = new ArrayList<String>();
	}
	
	public void addCommand(String cmd){
		if(cmd.equals("commit")){
			isFinished = true;
		}
		commands.add(cmd);
		startTime = System.currentTimeMillis();
	}
	
	public String toString(){
		String ret = String.format("%d;");
		for(String s : commands){
			ret += s+';';
		}
		return ret;
	}
	
	public int getId(){
		return t_id;
	}
	
	/* check if the transaction's TTL is valid, true if expired, false if not */
	public boolean isExpired(){
		if((System.currentTimeMillis() - startTime) > TTL) return true;
		else return false;
	}
}
