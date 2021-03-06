package chat.server;


import chat.client.ChatPacket;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yuriy.gorbylev on 02.02.2015.
 */
public class ChatServer extends Thread{

    private ServerSocket ss;
    private List<ChatConnection> syncConnections = Collections.synchronizedList(new ArrayList());
    private List<String> syncUsers = Collections.synchronizedList(new ArrayList());
    private int port;
    private int maxUsers;
    private JList serverUserList;
    private String history = "";

    public ChatServer(int port, int maxUsers, JList serverUserList) {
        this.port = port;
        this.maxUsers = maxUsers;
        this.serverUserList = serverUserList;
    }

    @Override
    public void run() {

        try {
            ss = new ServerSocket(port);
            while(true) {
                Socket clientSocket = ss.accept();
                    if (syncUsers.size() < maxUsers){
                        ChatConnection chatConnection = new ChatConnection(clientSocket);
                        syncConnections.add(chatConnection);
                        new Thread(chatConnection).start();
                    } else{
                        System.out.println("Too many users!");
                    }
            }
        } catch (IOException e) {
            System.out.println("SERVER DOES NOT WORK");
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ChatConnection implements Runnable {

        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Socket clientSocket;
        private ChatPacket chatPacket;
        private ServerPacket serverPacket;
        private boolean isConnectingMessage = true;
        private String message;

        public ChatConnection(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {

            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss: ");

                while(true){
                    chatPacket = (ChatPacket) in.readObject();

                    if (isConnectingMessage){

                        message = date.format(new Date()) + chatPacket.getNick() + ". IP: " +
                                clientSocket.getInetAddress().getHostAddress() + ". Has just CONNECTED.";
                        isConnectingMessage = false;
                        syncUsers.add(chatPacket.getNick());
                    }else{
                        message = date.format(new Date()) + chatPacket.getNick() + ": " + chatPacket.getMessage();
                    }

                    if (chatPacket.getMessage().equals("/exit")){
                        message = date.format(new Date()) + chatPacket.getNick() + " has LEFT.";
                        syncUsers.remove(chatPacket.getNick());
                        history += message + "\n";
                        serverPacket = new ServerPacket(message, syncUsers);
                        for (ChatConnection connection : syncConnections){
                            connection.out.writeObject(serverPacket);
                            connection.out.flush();
                            serverUserList.setListData(syncUsers.toArray());
                        }
                        Thread.currentThread().interrupt();
                        break;

                    } else if(chatPacket.getMessage().equals("/history")){
                        message = "*************HISTORY******************\n" + history +
                                  "**************************************";
                        serverPacket = new ServerPacket(message, syncUsers);
                        out.writeObject(serverPacket);
                        out.flush();
                        serverUserList.setListData(syncUsers.toArray());
                    } else {

                        serverPacket = new ServerPacket(message, syncUsers);
                        for (ChatConnection connection : syncConnections){
                            connection.out.writeObject(serverPacket);
                            connection.out.flush();
                            serverUserList.setListData(syncUsers.toArray());
                        }
                        history += message + "\n";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}


