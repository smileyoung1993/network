package http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
	
	private static final int PORT = 8088;
	public static void main(String[] args) {

	      ServerSocket serverSocket = null;

	      try {
	         // 1. Create Server Socket
	         serverSocket = new ServerSocket();
	         
	            
	         // 2. Bind -- > 서버에 연결
	         String localhost = InetAddress.getLocalHost().getHostAddress();
	         serverSocket.bind( new InetSocketAddress( localhost, PORT ) ); // 어느 server의(localhost) 어떤 process(port) 인지 넣어줌
	         consoleLog("bind " + localhost + ":" + PORT);

	         while (true) {
	            // 3. Wait for connecting ( accept ) --> 요청을 때린다.
	            Socket socket = serverSocket.accept();// connect가 들어올때 accept 되면서 blocking 되어진다.

	            // 4. Delegate Processing Request --> 요청의 응답을 requestHandler에서 처리
	            new RequestHandler(socket).start();
	         }

	      } catch (IOException ex) {
	         consoleLog("error:" + ex);
	      } finally {
	         // 5. clean-up
	         try {
	            if (serverSocket != null && serverSocket.isClosed() == false) {
	               serverSocket.close();
	            }
	         } catch (IOException ex) {
	            consoleLog("error:" + ex);
	         }
	      }
	   }

	   public static void consoleLog(String message) {
	      System.out.println("[HttpServer#" + Thread.currentThread().getId()  + "] " + message);
	   }
}