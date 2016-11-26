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

class ServerSendThread extends Thread {

    private Registry registry;
    private GameController gameController;
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    public volatile InetAddress address;
    public volatile int port;
    protected volatile boolean running = true;
    protected volatile int playerUpdateID;

    public ServerSendThread(Registry r, GameController gc, DatagramSocket s) {
        registry = r;
        gameController = gc;
        socket = s;

        System.out.println("ServerSendThread started" + socket.getPort());
    }

    public void run() {
        int i = 0;
        while (running) {
            if (address != null && port > 0) {
                sendPlayerData();
                sendRobotData();
                sendOobabooData();
                sendMonsterData();
                if (i % 25 == 0) {
                    sendPlaceableData();
                }
                sendResourceData();
                try {
                    sleep(30);
                } catch (InterruptedException ex) {
                }
                i++;
                if (i > 100) {
                    i = 0;
                }
            }
        }
        System.out.println("ServerSendThread stopped: " + port);
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public void sendPlayerData() {
        HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    UDPPlayer up = p.createUpdate();
                    if (up != null) {
                        try {
                            up.playerUpdateID = playerUpdateID;
                            sendPacket(up);
                            playerUpdateID++;
                        } catch (IOException e) {
                            e.printStackTrace();
                            //running = false;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendRobotData() {
        HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null && p.getRobot().getIsActivated()) {
                    UDPRobot up = p.createRobotUpdate();
                    if (up != null) {
                        try {
                            sendPacket(up);
                        } catch (IOException e) {
                            e.printStackTrace();
                            //running = false;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendOobabooData() {
        HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null && p.getOobaboo() != null) {
                    UDPOobaboo uo = p.createOobabooUpdate();
                    if (uo != null) {
                        try {
                            sendPacket(uo);
                        } catch (IOException e) {
                            e.printStackTrace();
                            //running = false;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendPlaceableData() {
        try {
            HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
            HashMap<String, Placeable> placeables = new HashMap<String, Placeable>(registry.getPlaceableManager().getPlaceables());
            for (String key : placeables.keySet()) {
                Placeable p = (Placeable) placeables.get(key);
                if (p != null) {
                    boolean close = false;
                    for (String playerKey : players.keySet()) {
                        Player player = (Player) players.get(playerKey);
                        if (player != null && player != registry.getPlayerManager().getCurrentPlayer()) {
                            //only send monsters that are near client players
                            if (player.getCenterPoint().distance(p.getCenterPoint()) <= 1000) {
                                close = true;
                            }
                        }
                    }
                    if (close) {
                        UDPPlaceable up = p.createUpdate();
                        if (up != null) {
                            try {
                                sendPacket(up);
                            } catch (IOException e) {
                                e.printStackTrace();
                                //running = false;
                            }
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendResourceData() {
        //only get the resouces 
        try {
            HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
            HashMap<String, Resource> resources = new HashMap<String, Resource>(registry.getResourceManager().getResources());
            for (String key : resources.keySet()) {
                Resource r = (Resource) resources.get(key);
                if (r != null) {
                    boolean close = false;
                    for (String playerKey : players.keySet()) {
                        Player player = (Player) players.get(playerKey);
                        if (player != null && player != registry.getPlayerManager().getCurrentPlayer()) {
                            //only send monsters that are near client players
                            if (player.getCenterPoint().distance(r.getCenterPoint()) <= 1000) {
                                close = true;
                            }
                        }
                    }
                    if (close) {
                        UDPResource up = r.createUpdate();
                        if (up != null) {
                            try {
                                sendPacket(up);
                            } catch (IOException e) {
                                e.printStackTrace();
                                //running = false;
                            }
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendMonsterData() {
        try {
            HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());
            HashMap<String, Monster> monsters = new HashMap<String, Monster>(registry.getMonsterManager().getMonsters());
            for (String key : monsters.keySet()) {
                Monster m = (Monster) monsters.get(key);
                if (m != null) {
                    if (!m.getName().equals("BlueThorn") && !m.getName().equals("VineThorn")) {
                        boolean close = false;
                        for (String playerKey : players.keySet()) {
                            Player player = (Player) players.get(playerKey);
                            if (player != null && player != registry.getPlayerManager().getCurrentPlayer()) {
                                //only send monsters that are near client players
                                if (player.getCenterPoint().distance(m.getCenterPoint()) <= 1000) {
                                    close = true;
                                }
                            }
                        }
                        if (close) {
                            UDPMonster up = m.createUpdate();
                            if (up != null) {
                                try {
                                    sendPacket(up);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    //running = false;
                                }
                            }
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void sendPacket(Object o) throws IOException {
        //System.out.println("Server Sending Data: " + o + "(" + address.toString() + ":" + port + ")");
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));

            os.flush();
            os.writeObject(o);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);
            //int byteCount = packet.getLength();
            //System.out.println(byteCount);
            socket.send(packet);
            os.close();
        } catch (Exception e) {
            EIError.debugMsg("Network Error: " + e.getMessage());
        }
    }
}