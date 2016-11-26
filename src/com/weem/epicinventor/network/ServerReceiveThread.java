package com.weem.epicinventor.network;

import com.weem.epicinventor.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.hud.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.world.block.*;

import java.io.*;
import java.net.*;
import java.util.*;

class ServerReceiveThread extends Thread {

    private Registry registry;
    private GameController gameController;
    private ServerSendThread serverSendThread;
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected volatile InetAddress address;
    protected volatile int port;
    public volatile boolean running = true;
    protected int totalReceived = 0;
    protected int totalReceivedCount = 0;

    public ServerReceiveThread(Registry r, GameController gc, DatagramSocket s, int p) {
        registry = r;
        gameController = gc;
        socket = s;
        port = p;

        System.out.println("ServerReceiveThread started" + socket.getPort());

        serverSendThread = new ServerSendThread(registry, gameController, socket);
        serverSendThread.start();
    }

    public void run() {
        receivePacket();
        address = packet.getAddress();
        port = packet.getPort();

        serverSendThread.address = address;
        serverSendThread.port = port;

        while (running) {
            receivePacket();
        }
        
        System.out.println("ServerReceiveThread stopped: " + port);
    }

    public void setRunning(boolean r) {
        System.out.println("Set Running ServerReceiveThread stopped: " + r + ":" + port);
        if (r == false && serverSendThread != null) {
            serverSendThread.setRunning(false);
        }
        running = r;
    }

    /*
     * public synchronized void receivePacket() { try { //receive server packet
     * byte[] buf = new byte[2056]; packet = new DatagramPacket(buf,
     * buf.length); socket.receive(packet);
     *
     * //translate packet String received = new String(packet.getData(), 0,
     * packet.getLength()); } catch (IOException e) { e.printStackTrace();
     * System.out.println("Failed to Receive"); running = false; } catch
     * (Exception e) { e.printStackTrace(); running = false; }
    }
     */
    public synchronized void receivePacket() {
        try {
            //receive server packet
            byte[] buf = new byte[4112];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            int byteCount = packet.getLength();
            //totalReceived += byteCount;
            //totalReceivedCount++;
            //System.out.println(totalReceivedCount + ": " + totalReceived);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
            Object o = is.readObject();

            //System.out.println("Server Receiving Data: " + o);

                //System.out.println("Server Receive: " + port);
            if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
                if (o.getClass().equals(UDPKeys.class)) {
                    registry.getPlayerManager().processKeysUpdateUDP((UDPKeys) o);
                } else {
                    System.out.println("Data: " + o);
                }
            } else {
                System.out.println("Port (" + port + "): " + gameController.multiplayerMode + ":" + registry.getNetworkThread());
                setRunning(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to Receive");
            //running = false;
        } catch (Exception e) {
            e.printStackTrace();
            //running = false;
        }
    }
}