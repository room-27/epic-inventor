package com.weem.epicinventor.network;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.armor.*;

import java.io.*;

public class UDPPlaceable implements Serializable {
    
    protected static final long serialVersionUID = 10000L;
    public String id = "";
    //public int mapX, mapY;
    //public int width, height;
    //public String standardImage;
    //public String animationImage;
    //public State currentState;
    //public int numAnimationFrames;
    //public int currentAnimationFrame;
    //public int animationFrameDuration;
    //public long animationFrameUpdateTime = 0;
    //public boolean isAnimating;
    public boolean isActive;
    public boolean isBuilding;
    public long buildingTime;
    public int totalBuildTime;
    public boolean isDirty;
    public boolean isDestroying;
    public int totalHitPoints;
    public int hitPoints;
    //public boolean isPowered;
    //public boolean noPowerShow;
    //public float noPowerTotalTime;
    //public int powerRequired;
    //public int powerGenerated;
    //public int fearGenerated;
    //public int fearDistance;
    //public long fearDuration;
    //public int touchDamage;
    public long destroyingTime;
    //public String type = "";
    //public boolean facingRight = true;
    //public Rectangle spriteRect;
    //public long lastDamage = 0;
    
    public UDPPlaceable(String pid) {
        id = pid;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
