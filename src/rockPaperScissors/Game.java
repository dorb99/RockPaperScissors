package rockPaperScissors;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONObject;

public class Game {
	
	static ArrayList<Game> games = new ArrayList<Game>();
	
	// NOT GOOD PRACTICE, USE GETTERS AND SETTERS!!!
	public HttpExchange player1;
	public HttpExchange player2;
	
	private String name1;
	private String name2;
	
	// NOT GOOD PRACTICE, USE GETTERS AND SETTERS!!!
	public String action1 = "";
	public String action2= "";
	
	private String id;
	
	
	public Game(HttpExchange player1, HttpExchange player2) throws IOException {
		this.player1 = player1;
		this.player2 = player2;
		this.name1 = getValueByKey(player1, "name");
		this.name2 = getValueByKey(player2, "name");
		createGameId();
		games.add(this);
	}


	private String getValueByKey(HttpExchange player, String key)  {
		byte[] data;
		try {
			data = player.getRequestBody().readAllBytes();
			String body = new String(data);
			JSONObject json = new JSONObject(body);
			String value = json.getString(key);
			if(value.isBlank()) {
				return key;
			} else {
				return value;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error while looking for key: "+key);
			return key;
		}	
	}


	private void createGameId() throws IOException {
		UUID id = UUID.randomUUID();
		this.id = id.toString();
	}
	
	public String getId() {
		return this.id;
	}
	
	static public boolean isValidId(String id) {
		for (Game game : games) {
			if(game.id == id) {
				return true;
			}
		}
		return false;
	}
	
	static public synchronized Game returnGame(String id) {
	    for (Game game : games) {
	        if (game.getId().equals(id)) {
	        	System.out.println("Found game");
	            return game;
	        }
	    }
	    return null;
	}

	
	public synchronized void submitMove(HttpExchange exchange) throws IOException {
		String playerName = getValueByKey(exchange, "name");
	    String move = getValueByKey(exchange, "action").toLowerCase();
		if (action1 == null) {
	    	action1 = move;
	        player1 = exchange;
	        // Optionally, send an acknowledgment to player 1 indicating their move has been received
	    } else if (action2 == null) {
	    	action2 = move;
	        player2 = exchange;
	        // Both moves are now available; determine the winner and respond
	        resolve();
	    } else {
	        // Handle the case where both moves have already been submitted
	        sendResponse(exchange, "Both players have already submitted their moves.", 400);
	    }
	}

	private void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
		exchange.sendResponseHeaders(status, response.length());
		exchange.getResponseBody().write(response.getBytes());
		exchange.getResponseBody().close();
	}
	
	public void play(HttpExchange exchange) throws IOException {
	    String playerName = getValueByKey(exchange, "name");
	    String action = getValueByKey(exchange, "action").toLowerCase();

	    System.out.println("player "+playerName+" action "+action);
	    System.out.println("saved data: 1-"+name1+" 2-"+name2);
	    if (this.name1.equals(playerName) && this.action1.isBlank()) {
	        this.action1 = action;
	        this.player1 = exchange;
	        System.out.println("Set action1 for player: " + playerName);
	    } else if (this.name2.equals(playerName) && this.action2.isBlank()) {
	        this.action2 = action;
	        this.player2 = exchange;
	        System.out.println("Set action2 for player: " + playerName);
	        resolve();
	    } else {
	        System.out.println("Invalid player or action already set for player: " + playerName);
	        sendResponse(exchange, "Invalid action submission.", 400);
	    }
	}



	private void resolve() throws IOException {
		// USING STATIC STRINGS AND NOT WRITING
		System.out.println("DEBUG DATA "+this.name1+" action: "+this.action1);
		System.out.println("DEBUG DATA "+this.name2+" action: "+this.action2);
		String winner = "TIE";
		switch(this.action1) {
			case "rock":
				if(action2.equals("paper")) {
					winner = name2;
				} else if (action2.equals("scissor")) {
					winner = name1;
				}
				break;
			case "paper":
				if(this.action2.equals("rock")) {
					winner = name1;
				} else if (this.action2.equals("scissor")) {
					winner = name2;
				}
				break;
			case "scissor":
				if(this.action2.equals("rock")) {
					winner = name2;
				} else if (this.action2.equals("paper")) {
					winner = name1;
				}
				break;
		}
		player1.sendResponseHeaders(400, winner.length());
		player1.getResponseBody().write(winner.getBytes());
		player2.sendResponseHeaders(400, winner.length());
		player2.getResponseBody().write(winner.getBytes());
		
		// Create as a method!
		games.remove(this);
		player1.getResponseBody().close();
		player2.getResponseBody().close();
		this.action1 = "";
		this.action2 = "";
		player1 = null;
		player2 = null;
	}
}
