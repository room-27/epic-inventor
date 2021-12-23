package com.weem.epicinventor.network;

import java.io.*;

public class UDPKeys implements Serializable {
    
    protected static final long serialVersionUID = 10000L;
    public String id = "";
    public boolean keySpacePressed;
    public boolean keyRightPressed;
    public boolean keyLeftPressed;
    public boolean keyGatherPressed;
    public boolean keyRobotPressed;
    
    public UDPKeys(String pid, boolean space, boolean right, boolean left, boolean gather, boolean robot) {
        id = pid;
        keySpacePressed = space;
        keyRightPressed = right;
        keyLeftPressed = left;
        keyGatherPressed = gather;
        keyRobotPressed = robot;
    }
    
    public boolean hasChange(UDPKeys old) {
        if(old == null) return true;
        
        if(this.keySpacePressed != old.keySpacePressed) return true;
        if(this.keyRightPressed != old.keyRightPressed) return true;
        if(this.keyLeftPressed != old.keyLeftPressed) return true;
        if(this.keyGatherPressed != old.keyGatherPressed) return true;
        if(this.keyRobotPressed != old.keyRobotPressed) return true;
        
        return false;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
