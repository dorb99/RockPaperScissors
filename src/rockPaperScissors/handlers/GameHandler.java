package rockPaperScissors.handlers;

import java.io.IOException;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import rockPaperScissors.Game;

public class GameHandler  implements HttpHandler{
	
	static private String POST_METHOD = "POST";
	static private HttpExchange waitingPlayerExchange = null;
	static private boolean waitingPlayer = false;
	
	// POST /game/join
	//		{"", "game", "join"}
	// POST /game/play/{id}
	//		{"", "game", "play", "{id}"}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
//		CorsAdded.implemetntCors(exchange);
		if(exchange.getRequestMethod().equalsIgnoreCase(POST_METHOD)) {
			String path = exchange.getRequestURI().getPath();
			String[] pathParts = path.split("/");
			
			
			if(pathParts[0].isEmpty() && pathParts[1].equalsIgnoreCase("game") && pathParts[2].equalsIgnoreCase("join")) {
				joinGame(exchange);
			} else if(pathParts[0].isEmpty() && pathParts[1].equalsIgnoreCase("game") && pathParts[2].equalsIgnoreCase("play")) {
				String id = pathParts[3];
//				if(!Game.isValidId(id)){
//					sendResponse(exchange, "Not a real game!");
//				}
				playAction(exchange, id);
			} else {
				sendResponse(exchange, "Not a route!", 400);
			}
			
		} else {
			sendResponse(exchange, "Not a valid method", 400);
		}
	}

	private void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
		exchange.sendResponseHeaders(status, response.length());
		exchange.getResponseBody().write(response.getBytes());
		exchange.getResponseBody().close();
	}

	private void playAction(HttpExchange exchange, String id) throws IOException {
		// go to the game, find my game, and play the action. return result when finihsed
	    System.out.println("Received play request with ID: " + id);

        Game myGame = Game.returnGame(id);
        if (myGame == null) {
            sendResponse(exchange, "Game not found", 404);
            return;
        }
        System.out.println("Game found. Processing action...");
        myGame.play(exchange);
//		Game myGame = Game.returnGame(id);
//		if (myGame == null) {
//	        sendResponse(exchange, "Game not found", 404);
//	        return;
//	    }
//		myGame.submitMove(exchange);		
	}

	private void joinGame(HttpExchange exchange) throws IOException {
		// TODO Auto-generated method stub
		// if another user is waiting - if yes, create a game with him. else, wait
		if(!waitingPlayer) {
			waitingPlayer = true;
			waitingPlayerExchange = exchange;
		} else {
			Game currentGame = new Game(exchange, waitingPlayerExchange);
			sendResponse(exchange, currentGame.getId(), 200);
			sendResponse(waitingPlayerExchange, currentGame.getId(), 200);
			waitingPlayer = false;
			waitingPlayerExchange = null;
		}
	}

}