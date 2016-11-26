package com.weem.epicinventor.network;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.armor.*;
import com.weem.epicinventor.resource.*;

import java.io.*;

public class UDPResource implements Serializable {

    protected static final long serialVersionUID = 10000L;
    public String id = "";
    //public int mapX;
    //public int mapY;
    //public int xp;
    //public int width = 32;
    //public int height = 32;
    //public ResourceType resourceType;
    //public boolean isDirty;
    public boolean isCollecting;
    //public long collectionTime;
    //public int numAnimationFrames;
    //public int currentAnimationFrame;
    //public int animationFrameDuration;
    //public long animationFrameUpdateTime = 0;
    //public final static double DEFAULT_ANIMATION_DURATION = 0.20;
    //public boolean isAnimating;
    //public String imageName = "";

    public UDPResource(String pid) {
        id = pid;
    }

    private void readObject(ObjectInputStream aInputStream) throws Exception {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws Exception {
        aOutputStream.defaultWriteObject();
    }
}
