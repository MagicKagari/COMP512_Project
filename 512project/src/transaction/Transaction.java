package transaction;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.tomcat.jni.Time;

public class Transaction {

	/* TTL constant in ms */
	final long TTL = 200000; //200 secs
	
	long startTime;
	int t_id;
	boolean isFinished;
	CopyOnWriteArrayList<String> commands;
	CopyOnWriteArrayList<String> entireCommands;
	
	public Transaction(int id){
		t_id = id;
		isFinished = false;
		commands = new CopyOnWriteArrayList<String>();
		entireCommands = new CopyOnWriteArrayList<String>();
	}
	
	public void addCommand(String cmd, String command){
		if(cmd.equals("commit")){
			isFinished = true;
		}
		commands.add(cmd);
		entireCommands.add(command);
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public String toString(){
		String ret = String.valueOf(t_id)+';';
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
