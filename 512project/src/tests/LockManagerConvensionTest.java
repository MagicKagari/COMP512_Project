package tests;
import LockManager.*;

public class LockManagerConvensionTest {

	public static void main (String[] args) {
		TestThread t1, t2;
		LockManager lm = new LockManager ();
		t1 = new TestThread (lm, 1);
		t1.start ();
		
    }
}

class TestThread extends Thread{
	LockManager lm;
    int threadId;

    public TestThread (LockManager lm, int threadId) {
        this.lm = lm;
        this.threadId = threadId;
    }

    public void run () {
    	try {
    		
		lm.Lock (1, "a", LockManager.READ);
		lm.Lock (1, "a", LockManager.WRITE);	    
    	
    	}
	    catch (DeadlockException e) { 
	        System.out.println ("Deadlock.... ");
	    }	    
	    lm.UnlockAll (2);
    }
}