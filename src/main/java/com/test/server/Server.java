package com.test.server;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int PORT = 5000;
	private ServerSocket server;
	private ExecutorService threadPool;
	private HashMap<String,PrintWriter> allout;

	public Server() throws IOException{
	    try{
            System.out.println("Server initialize start.");
            server = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(50);

            allout = new HashMap<>();

            System.out.println("Server initialized");
        } catch (IOException e){
	        e.printStackTrace();
	        throw e;
        }
    }

    public void start(){
	    try{
	        while(true){
                System.out.println("Waiting for client connection");
                Socket client = server.accept();

                Runnable handler = new ClientHandler(client);
                threadPool.execute(handler);
            }
        }catch (IOException e){
	        e.printStackTrace();
        }
    }

    private synchronized void sendMessage(String msg){
        for(PrintWriter pw : allout.values()){
            pw.println(msg);
            System.out.println("send to all:"+msg);
        }
    }

    public static void main(String[] args){
	    Server server;
	    try{
	        server = new Server();
	        server.start();
        } catch (IOException e){
	        e.printStackTrace();
            System.out.println("Server Starts failed.");
        }
    }

    class ClientHandler implements Runnable {
        private Socket socket;
        private String ip;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            InetAddress address = socket.getInetAddress();
            this.ip = address.getHostAddress();
            int port = socket.getPort();
            System.out.println(ip + ":" + port + " has connected");
        }

        private List<String> getAtUsers(String msg){
            if(msg.indexOf('@')==-1){
                return null;
            }
            List<String> users = new ArrayList<>();
            try {
                String[] ss = msg.split("@");
                for (String s : ss) {
                    if (s.length() == 0) {
                        continue;
                    }
                    s = s.split(" ")[0];
                    users.add(s);
                }
                if(users.size()==0){
                    return null;
                }
                return users;
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                return users.size()==0?null:users;
            }
        }

        @Override
        public void run() {
            PrintWriter pw = null;
            try {
                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                pw = new PrintWriter(osw, true);

                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                this.username = br.readLine();
                System.out.println(username+" has joined.");
                synchronized (allout){
                    allout.put(username,pw);
                }
                sendMessage("["+username+"] is online now.");

                String message = null;
                while((message=br.readLine())!=null){
                    List<String> atUsers = getAtUsers(message);
                    if(atUsers == null){
                        sendMessage("["+username+"]: "+message);
                    } else{
                        for(String user : atUsers){
                            if(allout.containsKey(user)){
                                allout.get(user).println("["+username+"] mentioned you: "+message);
                            }
                        }
                    }
                }

            } catch (IOException e) {

            }finally {
                synchronized(allout){
                    allout.remove(username);
                }
                System.out.println("["+username+"]: has left.");
                System.out.println("Current user number: "+allout.size());
                try{
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}

