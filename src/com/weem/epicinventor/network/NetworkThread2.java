package com.weem.epicinventor.network;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.hud.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.world.block.*;

import java.io.*;
import java.net.*;

public class NetworkThread2 {

    private TCPServerManager TCPServerManager;
    private TCPClient tcpClient;

    public NetworkThread2(TCPServerManager s, TCPClient c) {
        TCPServerManager = s;
        tcpClient = c;
    }

    public void close() {
        if(TCPServerManager != null)
        {
            TCPServerManager.close();
        }
        
        if(tcpClient != null)
        {
            tcpClient.close();
        }
    }

    public boolean sendData(Object data) {
        if(TCPServerManager != null)
        {
            return TCPServerManager.sendData(data);
        }
        
        if(tcpClient != null)
        {
            return tcpClient.sendData(data);
        }
        
        return false;
    }

    public boolean readyForUpdates() {
        if(TCPServerManager != null)
        {
            return TCPServerManager.readyForUpdates;
        }
        
        if(tcpClient != null)
        {
            return tcpClient.readyForUpdates;
        }
        
        return false;
    }
}
