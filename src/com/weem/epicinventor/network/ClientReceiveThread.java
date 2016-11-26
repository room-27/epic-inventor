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

class ClientReceiveThread extends Thread {

    private Registry registry;
    private GameController gameController;
    private ClientSendThread clientSendThread;
    protected DatagramSocket socket;
    protected DatagramPacket packet = null;
    protected volatile InetAddress address;
    protected volatile int port;
    protected volatile boolean running = true;
    protected int totalReceived = 0;
    protected int totalReceivedCount = 0;
    protected int pl = 0;
    protected int pl_count = 0;
    protected int rb = 0;
    protected int rb_count = 0;
    protected int oo = 0;
    protected int oo_count = 0;
    protected int pb = 0;
    protected int pb_count = 0;
    protected int mb = 0;
    protected int mb_count = 0;
    protected int rs = 0;
    protected int rs_count = 0;

    public ClientReceiveThread(Registry r, GameController gc, DatagramSocket s, String a, int p) {
        registry = r;
        gameController = gc;
        socket = s;
        try {
            address = InetAddress.getByName(a);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to Send");
        }
        port = p;

        System.out.println("ClientReceiveThread started");

        clientSendThread = new ClientSendThread(registry, gameController, socket, address, port);
        clientSendThread.start();
    }

    public void run() {
        while (running) {
            receivePacket();
        }
    }

    public void setRunning(boolean r) {
        if (r == false && clientSendThread != null) {
            clientSendThread.setRunning(false);
        }
        running = r;
    }

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

            //System.out.println("Client Receiving Data: " + o);

            if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
                //System.out.println("Client Receive: " + port);
                if (o.getClass().equals(UDPPlayer.class)) {
                    //pl += byteCount;
                    //pl_count++;
                    registry.getPlayerManager().processPlayerUpdateUDP((UDPPlayer) o);
                } else if (o.getClass().equals(UDPRobot.class)) {
                    //rb += byteCount;
                    //rb_count++;
                    registry.getPlayerManager().processRobotUpdateUDP((UDPRobot) o);
                } else if (o.getClass().equals(UDPOobaboo.class)) {
                    //oo += byteCount;
                    //oo_count++;
                    registry.getPlayerManager().processOobabooUpdateUDP((UDPOobaboo) o);
                } else if (o.getClass().equals(UDPPlaceable.class)) {
                    //pb += byteCount;
                    //pb_count++;
                    registry.getPlaceableManager().processPlaceableUpdateUDP((UDPPlaceable) o);
                } else if (o.getClass().equals(UDPMonster.class)) {
                    //mb += byteCount;
                    //mb_count++;
                    registry.getMonsterManager().processMonsterUpdateUDP((UDPMonster) o);
                } else if (o.getClass().equals(UDPResource.class)) {
                    //rs += byteCount;
                    //rs_count++;
                    registry.getResourceManager().processResourceUpdateUDP((UDPResource) o);
                }
            } else {
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
        /*
         * System.out.println("(" + pl_count + ": " + pl + ")" + "(" + rb_count
         * + ": " + rb + ")" + "(" + oo_count + ": " + oo + ")" + "(" + pb_count
         * + ": " + pb + ")" + "(" + mb_count + ": " + mb + ")" + "(" + rs_count
         * + ": " + rs + ")");
         */
    }
}