package com.weem.epicinventor.network;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.inventory.*;

import java.awt.*;
import java.io.*;

public class UpdatePlayer implements Serializable {
    
    protected static final long serialVersionUID = 10000L;
    public String id = "";
    public String name = "";
    public String action = "None";
    public int dataInt;
    public Actor actor;
    public Point dataPoint = new Point(0,0);
    public String dataString = "";
    public boolean dataBoolean;
    public long dataLong;
    public Inventory inventory;
    
    public UpdatePlayer(String pid) {
        id = pid;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
