package com.weem.epicinventor.network;

import com.weem.epicinventor.world.block.*;
import com.weem.epicinventor.utility.*;

import com.weem.epicinventor.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.nio.*;

public class TCPClientBlockManagerDataReceive extends Thread {

    private Registry registry;
    protected TCPClient client;
    protected ObjectOutputStream output;
    protected int currentStartCollumn;
    protected int collumnChunkSize;
    private short[][] blockChunk;
    private boolean sendMore;
    //private byte[] blockChunkByteArray;

    public TCPClientBlockManagerDataReceive(Registry r, TCPClient c, ObjectOutputStream o, Object d, int sc, int cs, boolean asThread, boolean more) {
        registry = r;
        client = c;
        output = o;
        blockChunk = (short[][]) d;
        //blockChunkByteArray = (byte[]) d;
        currentStartCollumn = sc;
        collumnChunkSize = cs;
        sendMore = more;
        if (asThread) {
            start();
        } else {
            sendBlockMangerData();
        }
    }

    @Override
    public void run() {
        try {
            sendBlockMangerData();
        } catch (Exception e) {
            EIError.debugMsg("Error Receiving Block Manager Data..." + e.getMessage());
        }
    }

    private void sendBlockMangerData() {
        try {
            EIError.debugMsg("Setting Block Manager...");
            //short[][] blockChunk = unzipBlockData(blockChunkByteArray);
            if (blockChunk.length >= collumnChunkSize) {
                registry.getBlockManager().setBlockCollumns(blockChunk, currentStartCollumn, currentStartCollumn + collumnChunkSize);
                EIError.debugMsg("Block Manager chunk " + currentStartCollumn + " " + (currentStartCollumn + collumnChunkSize));
                try {
                    sleep(250);
                } catch (InterruptedException ex) {
                }
                if(sendMore) {
                    output.writeObject("send block manager chunk");
                    EIError.debugMsg("Request: send block manager chunk");
                }
            } else {
                EIError.debugMsg("Block Manager set");
            }
            //blockChunkByteArray = null;
            blockChunk = null;
        } catch (Exception e) {
            EIError.debugMsg("Network Error: " + e.getMessage());
        }
    }
//    private short[][] unzipBlockData(byte[] data) {
//        Inflater decompressor = new Inflater();
//        decompressor.setInput(data);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
//        byte[] buf = new byte[1024];
//        while (!decompressor.finished()) {
//            try {
//                int count = decompressor.inflate(buf);
//                bos.write(buf, 0, count);
//            } catch (DataFormatException e) {
//                EIError.debugMsg("Error unzipBlockData "+e.getMessage());
//            }
//        }
//        try {
//            bos.close();
//        } catch (IOException e) {
//            EIError.debugMsg("Error unzipBlockData "+e.getMessage());
//        }
//        byte[] decompressedData = bos.toByteArray();
//        
//        int rows = registry.getBlockManager().getMapHeight() / registry.getBlockManager().getBlockHeight();
//        int columns = decompressedData.length / 2 / rows;
//        short[][] blockChunk = new short[columns][rows];
//        for(int x = 0; x < blockChunk.length; x++) {
//            for(int y = 0; y < blockChunk[0].length; y++) {
//                blockChunk[x][y] = (short)((decompressedData[(2*x*blockChunk[0].length)+(2*y)+1] & 0xff << 8) | decompressedData[(2*x*blockChunk[0].length)+(2*y)] & 0xff);
//            }
//        }
//        return blockChunk;
//    }
}
