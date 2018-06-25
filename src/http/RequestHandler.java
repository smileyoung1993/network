package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class RequestHandler extends Thread { // 서버에는 많은 사람이 접속하면,thread단위로 동시처리가 가능하게 하기위함
	private static final String DOCUMENT_ROOT = "./webapp";

	private Socket socket;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
	      try {
	         // logging Remote Host IP Address & Port
	         InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
	         consoleLog("connected from " + inetSocketAddress.getAddress().getHostAddress() + ":"
	               + inetSocketAddress.getPort());

	         // get IOStream
	         // 소켓에있는 stream 객체를 그대로 읽지않고 input stream 으로 감싸준다,-> byte단뒤로 읽는것을 char 단위로 읽기위해서
	         // 3byte -> char -> bufferedReader의 readline을 이용해서 한줄씩 읽는다.(개행이 올때까지 읽는다)

	         BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	         // 다시 바이트단위로 쓰기위해서 --> char로 형변환 , 한 라인씩읽어오기위함
	         
	         OutputStream os = socket.getOutputStream();
	         
	         String request = null;
	         
	         String[] tokens =null;
	         
	         while (true) {
	        	 
	            String line = br.readLine();
	            
	            if (line == null || "".equals(line)) // 헤더의 마지막라인은 빈스트링이다. -> 바디부분은 읽지않겠다
	               break;
	            // consoleLog(line);

	            if (request == null) {// 헤더에서 한라인만 읽음
	               request = line;
	               consoleLog(request);
	               
	               tokens = request.split(" ");
	   

	               break; // string을 split 해가지고 파일을 읽고 그대로 응답해주기 -> 헤더 /r 찍고,index.html
	            }

	         }
//	         
//	         os.write("HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
//	         os.write("Content-Type:text/html; charset=utf-8\r\n".getBytes("UTF-8"));
//	         os.write("\r\n".getBytes());
//             os.write("<h1>이 페이지가 잘 보이면 실습과제 SimpleHttpServer를 시작할 준비가 된 것입니다.</h1>".getBytes("UTF-8"));
//	      
//	         //응답데이터 보내기 
	         if(tokens[0].equals("GET") ==false)response400Error(os,tokens[2]);
	         
	         else if(tokens[1] != null &&  tokens[2] != null ) responseStaticResource(os,tokens[1], tokens[2]);

//	         // 예제 응답입니다.
//	         // 서버 시작과 테스트를 마친 후, 주석 처리 합니다.
//	/*       os.write("HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
//	         os.write("Content-Type:text/html; charset=utf-8\r\n".getBytes("UTF-8"));
//	         os.write("\r\n".getBytes());
//	         os.write("<h1>이 페이지가 잘 보이면 실습과제 SimpleHttpServer를 시작할 준비가 된 것입니다.</h1>".getBytes("UTF-8"));*/

	      } catch (Exception ex) {
	         consoleLog("error:" + ex);
	      } finally {
	         // clean-up
	         try {
	            if (socket != null && socket.isClosed() == false) {
	               socket.close();
	            }
	         } catch (IOException ex) {
	            consoleLog("error:" + ex);
	         }
	      }
	   }

	   private void consoleLog(String message) {
	      System.out.println("[RequestHandler#" + getId() + "] " + message);
	   }
	   
	   private void response400Error(OutputStream outputStream, String protocol)throws IOException  {
	      File file = new File("./webapp/error/400.html");
	      Path path = file.toPath();
	      
	      // 400 파일에 있는 바디 내용을 읽어온다.
	      byte[] body = Files.readAllBytes( path ); 
	      outputStream.write((protocol +"400 Bad Request\r\n").getBytes());
	      outputStream.write( "Content-Type:text/html\r\n".getBytes() );
	      outputStream.write( "\r\n".getBytes() ); 
	      
	      outputStream.write( body );
	   
	   
	   
	   }
	   private void response404Error(OutputStream outputStream, String protocol) throws IOException  {
	      File file = new File("./webapp/error/404.html");
	      Path path = file.toPath();
	      
	      byte[] body = Files.readAllBytes( path ); 
	      outputStream.write( ( protocol + " 404 File Not Found\r\n" ).getBytes() );
	      outputStream.write( "Content-Type:text/html\r\n".getBytes() );
	      outputStream.write( "\r\n".getBytes() ); 
	      outputStream.write( body );


	      
	      
	   }
	   private void responseStaticResource(OutputStream outputStream, String url, String protocol) throws IOException {
	      File file = new File( "./webapp" + url );
	       if ( file.exists() == false ) {
	           response404Error( outputStream, protocol );
	           return;
	      }

	      // css 적용
	      Path path = file.toPath();
	      String mimeType = Files.probeContentType( path );
	      String content="Content-Type:"+mimeType+"\r\n";
	      byte[] body = Files.readAllBytes( path ); 

	      outputStream.write( "HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
	      outputStream.write( content.getBytes( "UTF-8" ));
	      outputStream.write( "\r\n".getBytes() ); //헤더의 마지막에 개행문자를 넣어준다.
	      outputStream.write( body );

	   }
	}
