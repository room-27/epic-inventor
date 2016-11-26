package com.weem.epicinventor.network;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.hud.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.world.block.*;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPServerManager extends Thread {

    private Registry registry;
    private GameController gameController;
    private int port;
    private ArrayList<TCPServer> tcpServers;
    private static int maxConnections = 0;
    public boolean readyForUpdates = true;
    private ServerSocket listenerSocket;
    private boolean shuttingDown = false;

    public TCPServerManager(Registry r, GameController gc, int p) {
        registry = r;
        gameController = gc;
        port = p;

        tcpServers = new ArrayList<TCPServer>();
    }

    @Override
    public void run() {
        int i = 0;

        try {
            listenerSocket = new ServerSocket(port);
            Socket socket;

            EIError.debugMsg("Listening");

            gameController.showMessage("Success", "Waiting for incoming connection...");
            gameController.multiplayerMode = gameController.multiplayerMode.SERVER;

            EIError.debugMsg("Waiting for Connection...");

            while ((i++ < maxConnections) || (maxConnections == 0)) {
                socket = listenerSocket.accept();

                if (!shuttingDown) {
                    TCPServer connection = new TCPServer(registry, gameController, socket, port + (i - 1));
                    tcpServers.add(connection);
                    connection.start();

                    EIError.debugMsg("Connected");

                    gameController.setNetworkMode(true);
                    gameController.showMessage("Success", "Network Connection Established");
                }
            }
        } catch (IOException ioe) {
            if (!shuttingDown) {
                gameController.showMessage("Error", "Couldn't listen on port " + port);
                System.out.println("IOException on socket listen: " + ioe);
                ioe.printStackTrace();
            }
        }
    }

    public boolean sendData(Object data) {
        boolean status = true;

        TCPServer tcpServer;

        for (int i = 0; i < tcpServers.size(); i++) {
            tcpServer = tcpServers.get(i);
            if (tcpServer != null) {
                if (tcpServer.readyForUpdates) {
                    if (!tcpServer.sendData(data)) {
                        status = false;
                    }
                }
            }
        }

        return status;
    }

    public void close() {
        System.out.println("tcp servers closed");
        TCPServer tcpServer;

        for (int i = 0; i < tcpServers.size(); i++) {
            tcpServer = tcpServers.get(i);
            if (tcpServer != null) {
                tcpServer.keepRunning = false;
            }
        }

        shuttingDown = true;

        if (listenerSocket != null) {
            try {
                listenerSocket.close();
            } catch (IOException e) {
            }
        }

        gameController.setNetworkMode(false);
        gameController.multiplayerMode = gameController.multiplayerMode.NONE;
    }
}