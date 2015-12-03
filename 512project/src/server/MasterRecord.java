package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MasterRecord implements Serializable {

    private boolean isAMaster;
    private int transactionID;
    private String fileA = "./records/fileA.rm";
    private String fileB = "./records/fileB.rm";
    String RMtype;
    String name;
    
    MasterRecord(String name) {
        this.name = name;
        this.isAMaster = true;
        fileA = "./records/"+name+"fileA.rm";
        fileB = "./records/"+name+"fileB.rm";
        checkRMfiles();
    }

    MasterRecord(int ID) {
        this.setTransactionID(ID);
        this.isAMaster = true;
        checkRMfiles();
    }

    MasterRecord(int ID, boolean A) {
        this.isAMaster = A;
        this.setTransactionID(ID);
        checkRMfiles();
    }

    public void setPathA (String newPath) {
        this.fileA = newPath;
    }

    public void setPathB (String newPath) {
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
    
    public String getPointerPath(){
        if(this.isAMaster) {
            return fileA;
        }
        else {
            return fileB;
        }
    }

    public void togglePointer() {
        this.isAMaster = !this.isAMaster;
    }

    public void setID(int ID) {
        this.setTransactionID(ID);
    }
    
    public boolean updateMasterRecord(){
        //write the current master record to file
        String path = "./records/MasterRecord.rm";
        File f = new File(path);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void checkRMfiles(){
        try{
            File rmA = new File(fileA);
            File rmB = new File(fileB);
            if(!rmA.exists()){
                rmA.getParentFile().mkdirs();
                rmA.createNewFile();
            }
            if(!rmB.exists()){
                rmB.getParentFile().mkdirs();
                rmB.createNewFile();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }
}
