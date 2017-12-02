package com.test.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	public final static String HOST="localhost";
    public final static int PORT=5000;
    
	private Socket socket;
	private Scanner scanner;
	private String username;

	public Client() throws IOException{
		try{
			System.out.println("Client init starts.");
			socket = new Socket(HOST,PORT);
			System.out.println("Client init done.");
		} catch (IOException e){
			throw e;
		}
	}
	public void start(){
		try {
			Runnable r = new ServerHandler();
			Thread t = new Thread(r);
			t.start();

			OutputStream out = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(out);
			PrintWriter pw = new PrintWriter(osw, true);
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please input your username.");
			while(true){
				username = scanner.nextLine().trim();
				if(username.length()>0){
					pw.println(username);
					break;
				} else{
					System.out.println("username cannot be empty.");
				}
			}
			System.out.println("Welcome "+username+" start chatting now.");
			while(true){
				String str = scanner.nextLine().trim();
				if (str.equals("QUIT")){
					break;
				} else {
					pw.println(str);
				}
			}
			socket.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		try{
			Client client = new Client();
			client.start();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	class ServerHandler implements Runnable{
		@Override
		public void run() {
			try{
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				String msg = null;
				while((msg=br.readLine())!=null){
					System.out.println(msg);
				}
			} catch (IOException e){

			}
		}
	}
}
