package rockPaperScissors.handlers;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class CorsAdded{

	static public void implemetntCors(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders()
			.add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders()
			.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		exchange.getResponseHeaders()
			.add("Access-Control-Allow-Headers", "Content-Type");
	}
}