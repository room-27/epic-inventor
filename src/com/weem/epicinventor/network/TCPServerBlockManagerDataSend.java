package com.weem.epicinventor.network;

import com.weem.epicinventor.world.block.*;
import com.weem.epicinventor.utility.*;

import com.weem.epicinventor.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.nio.*;

public class TCPServerBlockManagerDataSend extends Thread {

    private Registry registry;
    protected TCPServer server;
    protected int currentStartCollumn;
    protected int collumnChunkSize;

    public TCPServerBlockManagerDataSend(Registry r, TCPServer s, int sc, int cs) {
        registry = r;
        server = s;
        currentStartCollumn = sc;
        collumnChunkSize = cs;
    }

    @Override
    public void run() {
        try {
            sendBlockMangerData();
        } catch (Exception e) {
            EIError.debugMsg("Error Sending Block Manager Data..." + e.getMessage());
        }
    }

    private void sendBlockMangerData() {
        EIError.debugMsg("Sending Block Manager Data...");
        short[][] blockChunk = registry.getBlockManager().getBlockCollumns(currentStartCollumn, currentStartCollumn + collumnChunkSize);
        
        //byte[] blockChunkByteArray = zipBlockData(blockChunk);
        //EIError.debugMsg("Now...");
        server.sendData(blockChunk);
        //EIError.debugMsg("Sent...");
        //blockChunkByteArray = null;
        //blockChunk = null;
        EIError.debugMsg("Block Manager Data Sent "+currentStartCollumn+".."+(currentStartCollumn + collumnChunkSize));
    }

//    private byte[] zipBlockData(short[][] blockChunk) {
//        ByteBuffer byteBuf = ByteBuffer.allocate(blockChunk.length*blockChunk[0].length*2);
//        for(int x = 0; x < blockChunk.length; x++) {
//            for(int y = 0; y < blockChunk[0].length; y++) {
//                byteBuf.put((byte)(blockChunk[x][y] & 0xff));
//                byteBuf.put((byte)((blockChunk[x][y] >> 8) & 0xff));
//            }
//        }
//        byte[] input = byteBuf.array();
//        Deflater compressor = new Deflater();
//        compressor.setLevel(Deflater.BEST_COMPRESSION);
//        compressor.setInput(input);
//        compressor.finish();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
//        byte[] buf = new byte[1024];
//        while (!compressor.finished()) {
//            int count = compressor.deflate(buf);
//            bos.write(buf, 0, count);
//        }
//        try {
//            bos.close();
//        } catch (Exception e) {
//            EIError.debugMsg("Error zipBlockData "+e.getMessage());
//        }
//        byte[] compressedData = bos.toByteArray();
//        return compressedData;
//    }
}
