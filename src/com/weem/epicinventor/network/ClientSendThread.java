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

class ClientSendThread extends Thread {

    private Registry registry;
    private GameController gameController;
    protected DatagramSocket socket;
    protected DatagramPacket packet = null;
    protected volatile InetAddress address;
    protected volatile int port;
    protected volatile boolean running = true;
    protected UDPKeys oldUK;

    public ClientSendThread(Registry r, GameController gc, DatagramSocket s, InetAddress a, int p) {
        registry = r;
        gameController = gc;
        socket = s;
        address = a;
        port = p;
        
        System.out.println("ClientSendThread started");

        String data = "Go!";
        try {
            sendPacket(data);
        } catch (IOException e) {
            e.printStackTrace();
            //running = false;
        }
    }

    public void run() {
        while (running) {
            sendKeyData();
            try {
                sleep(20);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public void sendKeyData() {
        PlayerManager playerManager = registry.getPlayerManager();
        if (playerManager != null) {
            Player p = registry.getPlayerManager().getCurrentPlayer();
            if (p != null) {
                UDPKeys uk = new UDPKeys(
                        p.getId(),
                        gameController.getGamePanel().keySpacePressed,
                        gameController.getGamePanel().keyRightPressed,
                        gameController.getGamePanel().keyLeftPressed,
                        gameController.getGamePanel().keyGatherPressed,
                        gameController.getGamePanel().keyRobotPressed);
                if(uk.hasChange(oldUK)) {
                try {
                    sendPacket(uk);
                    oldUK = null;
                    oldUK = uk;
                } catch (IOException e) {
                    e.printStackTrace();
                    //running = false;
                }
                }
            }
        }
    }

    public void sendPacket(Object o) throws IOException {
        //System.out.println("Client Sending Data: " + o + "(" + address.toString() + ":" + port + ")");
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));

        os.flush();
        os.writeObject(o);
        os.flush();

        byte[] sendBuf = byteStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        int byteCount = packet.getLength();
        socket.send(packet);
        os.close();
    }
}