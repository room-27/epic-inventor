package com.weem.epicinventor.network;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.actor.oobaboo.*;
import com.weem.epicinventor.hud.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.world.block.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer extends Thread {

    private Registry registry;
    private GameController gameController;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private TCPServerBlockManagerDataSend blockManagerSend;
    protected int currentStartCollumn;
    public boolean keepRunning = true;
    public boolean readyForUpdates = false;
    public ServerReceiveThread serverReceiveThread;
    protected static int collumnChunkSize;
    protected int udpPort;

    public TCPServer(Registry r, GameController gc, Socket s, int u) {
        registry = r;
        gameController = gc;
        socket = s;
        udpPort = u;
        currentStartCollumn = 0;
        collumnChunkSize = 50;
    }

    @Override
    public void run() // read server messages and act on them.
    {
        try {
            DatagramSocket socket = new DatagramSocket(udpPort);
            serverReceiveThread = new ServerReceiveThread(registry, gameController, socket, udpPort);
            serverReceiveThread.start();
        } catch (IOException e) {
            gameController.showMessage("Error", "Couldn't accept connection...");
            return;
        }

        doServer();

        if (serverReceiveThread != null) {
            serverReceiveThread.setRunning(false);
        }

        return;
    }

    private void doServer() {
        Object data;
        String playerId = "";

        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    EIError.debugMsg("Creating output stream");

                    output = new ObjectOutputStream(socket.getOutputStream());

                    EIError.debugMsg("Creating input stream");
                    input = new ObjectInputStream(socket.getInputStream());

                    EIError.debugMsg("IO Created");

                    while (keepRunning && socket.isConnected()) {
                        data = input.readObject();
                        EIError.debugMsg("Received data: " + data.toString());

                        if (data.getClass().equals(String.class)) {
                            data = (String) data;
                            if (data.toString().equals("goodbye")) {
                                keepRunning = false;
                            } else if (data.toString().equals("send block manager")) {
                                EIError.debugMsg("Sending Block Manager Data...");
                                BlockManager bm = (BlockManager) (registry.getBlockManager().clone());
                                bm.clearBlockArray();
                                sendData(bm);
                                bm = null;
                                EIError.debugMsg("Block Manager Data Sent");

                                System.out.println("Setting new client UDP to: " + udpPort);
                                sendData("udp " + udpPort);
                            } else if (data.toString().equals("send block manager chunk")) {
                                //EIError.debugMsg("Received block man request (" + currentStartCollumn + ":" + collumnChunkSize);
                                blockManagerSend = new TCPServerBlockManagerDataSend(registry, this, currentStartCollumn, collumnChunkSize);
                                blockManagerSend.start();
                                currentStartCollumn += collumnChunkSize;
                                //EIError.debugMsg("Thread Made");

//                                EIError.debugMsg("Sending Block Manager Data...");
//                                //BlockManager bm = registry.getBlockManager();
//                                short[][] blockChunk = registry.getBlockManager().getBlockCollumns(currentStartCollumn, currentStartCollumn + collumnChunkSize);
//                                currentStartCollumn += collumnChunkSize;
//                                sendData(blockChunk);
//                                blockChunk = null;
//                                EIError.debugMsg("Block Manager Data Sent 0..200");
//                                try {
//                                    sleep(10);
//                                } catch (InterruptedException ex) {
//                                }
                            } else if (data.toString().equals("send placable manager")) {
                                EIError.debugMsg("Sending Placeable Manager Data...");
                                PlaceableManager pm = registry.getPlaceableManager();
                                sendData(pm);
                                pm = null;
                                EIError.debugMsg("Placeable Manager Data Sent");
                            } else if (data.toString().equals("send resource manager")) {
                                EIError.debugMsg("Sending Resource Manager Data...");
                                ResourceManager rm = registry.getResourceManager();
                                sendData(rm);
                                rm = null;
                                EIError.debugMsg("Resource Manager Data Sent");
                            } else if (data.toString().equals("send monster manager")) {
                                EIError.debugMsg("Sending Monster Manager Data...");
                                MonsterManager mm = registry.getMonsterManager();
                                sendData(mm);
                                mm = null;
                                EIError.debugMsg("Monster Manager Data Sent");
                            } else if (data.toString().equals("send player")) {
                                HashMap<String, Player> players = new HashMap<String, Player>(registry.getPlayerManager().getPlayers());

                                try {
                                    for (String key : players.keySet()) {
                                        Player p = (Player) players.get(key);
                                        if (p != null) {
                                            EIError.debugMsg("Sending Player Data...");
                                            sendData(p);
                                            EIError.debugMsg("Player Data Sent");
                                        }
                                    }
                                } catch (ConcurrentModificationException concEx) {
                                    //another thread was trying to modify players while iterating
                                    //we'll continue and the new item can be grabbed on the next update
                                }
                            } else if (data.toString().length() >= 6 && data.toString().substring(0, 6).equals("place ")) {
                                String parts[] = data.toString().split(" ");
                                if (parts.length == 4) {
                                    EIError.debugMsg("Adding placeable...");
                                    registry.getPlaceableManager().loadPlaceable(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Placeable.State.Placed);
                                }
                            } else if (data.toString().length() >= 18 && data.toString().substring(0, 18).equals("send monster data:")) {
                                String id = data.toString().substring(19);
                                Monster monster = registry.getMonsterManager().getMonsterById(id);
                                if (monster != null) {
                                    EIError.debugMsg("Sending Monster Data (" + monster.getId() + ")...");
                                    sendData(monster);
                                    EIError.debugMsg("Monster Data Sent");
                                }
                            } else if (data.toString().length() >= 18 && data.toString().substring(0, 18).equals("send oobaboo data:")) {
                                String id = data.toString().substring(19);
                                Player player = registry.getPlayerManager().getPlayerById(id);
                                if (player != null) {
                                    EIError.debugMsg("Sending Oobaboo Data (" + player.getId() + ")...");
                                    sendData(player.getOobaboo());
                                    EIError.debugMsg("Oobaboo Data Sent");
                                }
                            } else if (data.toString().length() >= 20 && data.toString().substring(0, 20).equals("send placeable data:")) {
                                String id = data.toString().substring(21);
                                Placeable placeable = registry.getPlaceableManager().getPlaceableById(id);
                                if (placeable != null) {
                                    EIError.debugMsg("Sending Placeable Data (" + placeable.getId() + ")...");
                                    sendData(placeable);
                                    EIError.debugMsg("Placeable Data Sent");
                                }
                            } else {
                                
                            EIError.debugMsg("Other Data:" + data);
                                if (!data.toString().isEmpty()) {
                                    gameController.showMessage("Success", "Message: " + data);
                                }
                            }
                        } else if (data.getClass().equals(Player.class)) {
                            EIError.debugMsg("Adding Player...");
                            Player p = (Player) data;
                            p.setTransient(registry);
                            registry.getPlayerManager().registerPlayer(p);
                            playerId = p.getId();
                            EIError.debugMsg("Player Added");
                            readyForUpdates = true;
                        } else if (data.getClass().equals(OobabooGatherer.class)) {
                            OobabooGatherer o = (OobabooGatherer) data;
                            registry.getPlayerManager().assignOobaboo(o);
                        } else if (data.getClass().equals(OobabooHealer.class)) {
                            OobabooHealer o = (OobabooHealer) data;
                            registry.getPlayerManager().assignOobaboo(o);
                        } else if (data.getClass().equals(OobabooWarrior.class)) {
                            OobabooWarrior o = (OobabooWarrior) data;
                            registry.getPlayerManager().assignOobaboo(o);
                        } else if (data.getClass().equals(UpdatePlayer.class)) {
                            UpdatePlayer up = (UpdatePlayer) data;
                            EIError.debugMsg("Updating Player (" + up.id + ")...");
                            registry.getPlayerManager().processPlayerUpdate(up);
                            EIError.debugMsg("Player Updated");
                        } else if (data.getClass().equals(UpdatePlaceable.class)) {
                            UpdatePlaceable up = (UpdatePlaceable) data;
                            EIError.debugMsg("Updating Placeable (" + up.id + ")...");
                            registry.getPlaceableManager().processPlaceableUpdate(up);
                            EIError.debugMsg("Placeable Updated");
                        } else {
                            EIError.debugMsg("Rec Data:" + data);
                        }

                        data = null;
                    }
                } catch (IOException e) {
                    EIError.debugMsg("Network IO Error: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    EIError.debugMsg("Network Class Error: " + e.getMessage());
                } catch (Exception e) {
                    EIError.debugMsg("Network Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        if (serverReceiveThread != null) {
            serverReceiveThread.setRunning(false);
        }

        try {
            socket.close();
        } catch (IOException e) {
            EIError.debugMsg("Socket Close Error: " + e.getMessage());
        }

        readyForUpdates = false;

        PlayerManager pm = registry.getPlayerManager();
        if (pm != null) {
            pm.removePlayer(playerId);
        }
    }

    public void close() {
        EIError.debugMsg("Closing");

        if (serverReceiveThread != null) {
            serverReceiveThread.setRunning(false);
        }

        if (socket != null) {
            try {
                sendData("goodbye");
                socket.close();
            } catch (IOException e) {
                //oh well, ain't no thang
            }
        }
        keepRunning = false;
        socket = null;
    }

    public boolean sendData(Object data) {
        if (output == null || !socket.isConnected()) {
            return false;
        }

        synchronized (this) {
            try {
                output.flush();
                output.writeObject(data);
                output.flush();
                //output.reset();

                if (data.getClass().equals(String.class)) {
                    if (data.equals("goodbye")) {
                        if (serverReceiveThread != null) {
                            serverReceiveThread.setRunning(false);
                        }

                        socket.close();
                        if (socket != null) {
                            socket.close();
                        }

                        keepRunning = false;
                    }
                }
            } catch (IOException e) {
                EIError.debugMsg("Network IO Error - Sending");
            }

            return true;
        }
    }
}
