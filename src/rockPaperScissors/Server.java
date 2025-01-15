package rockPaperScissors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import rockPaperScissors.handlers.*;

public class Server {

	static private final int PORT = 8000;
	static private final int POOL = 10;
	
	public static void main(String[] args) {
		
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			
			// home route
			server.createContext("/");
			
			// game route
			server.createContext("/game", new GameHandler());
			
			// score route
			server.createContext("/score", new ScoreHandler());
			
			server.setExecutor(Executors.newFixedThreadPool(POOL));
			server.start();
			
			System.out.println("Server started on port: "+PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
