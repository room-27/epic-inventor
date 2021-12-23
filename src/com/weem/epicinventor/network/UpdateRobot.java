package com.weem.epicinventor.network;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;

import java.io.*;

public class UpdateRobot implements Serializable {
    
    protected static final long serialVersionUID = 10000L;
    public String playerId = "";
    public String id = "";
    public String action = "None";
    public int dataInt;
    public Actor actor;
    public Goal currentGoal;
    public Goal previousGoal;
    
    public UpdateRobot(String pid, String rid) {
        playerId = pid;
        id = rid;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
