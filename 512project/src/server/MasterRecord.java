package server;

import java.io.Serializable;

public class MasterRecord implements Serializable {
    
    private boolean isAMaster;
    private int transactionID;
    private String fileA;
    private String fileB;
    
    MasterRecord() {
        this.isAMaster = true;
        this.fileA = "/records/fileA.rm";
        this.fileB = "/records/fileB.rm";
    }
    
    MasterRecord(int ID) {
        this.transactionID = ID;
        this.fileA = "/records/fileA.rm";
        this.fileB = "/records/fileB.rm";
    }
    
    MasterRecord(int ID, boolean A) {
        this.isAMaster = A;
        this.transactionID = ID;
        this.fileA = "/records/fileA.rm";
        this.fileB = "/records/fileB.rm";
    }
    
    public void setPathA (String newPath) {
        this.fileA = newPath;
    }
    
    public void setPathB (String newPaht) {
        this.fileA = newPath;
    }
    
    public String getPathA () {
        return this.fileA;
    }
    
    public String getPathB () {
        return this.fileB;
    }
    
    public String getPointer () {
        if(this.isAMaster) {
            return "A";
        }
        else {
            return "B";
        }
    }
    
    public void togglePointer() {
        this.isAMaster = !this.isAMaster;
    }
    
    public void setID(int ID) {
        this.transactionID = ID;
    }
}