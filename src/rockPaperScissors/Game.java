package rockPaperScissors;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class Game {

    static ArrayList<Game> games = new ArrayList<>();

    private String name1 = null;
    private String name2 = null;
    private String action1 = null;
    private String action2 = null;
    private String id;
    
    private HttpExchange player1Ex = null;

    // Constructor for game
    public Game() {
        createGameId();
        games.add(this);
    }

    private void createGameId() {
        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();
    }

    public String getId() {
        return this.id;
    }

    // looking for the Game object we are playing on (passing the id)
    static public synchronized Game returnGame(String id) {
        for (Game game : games) {
            if (game.getId().equals(id)) {
            	return game;
            }
        }
        return null;
    }

    // method for submiting the action. synchronized for making sure that we wont receive problems when submitting action together
    public synchronized void play(HttpExchange exchange) throws IOException {
    	// getting the body as a json object
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(requestBody);

        // getting the name and action
        String playerName = json.optString("name", "");
        String action = json.optString("action", "").toLowerCase();

        if (playerName.isEmpty() || action.isEmpty()) {
            sendResponse(exchange, "Invalid request: name or action is missing.", 400);
            return;
        }

        // logging 
        System.out.println("Player: " + playerName + ", Action: " + action);

        // if this is the first action submitted it is resolved as the first user
        // NOTICE: we arent returning anything for our client, but we are finishing the method so we can rerun it with the second user
        if (name1 == null) {
            // Assign the first player
            name1 = playerName;
            action1 = action;
            System.out.println("Assigned as Player 1: " + playerName);
            // saving the first user exchange so we can return later the result
            this.player1Ex = exchange;
        } else if (name2 == null && !playerName.equals(name1)) {
            // Assign the second player
            name2 = playerName;
            action2 = action;
            System.out.println("Assigned as Player 2: " + playerName);
            // resolving the game because the second action was submitted 
            resolve(exchange);
        } else if (playerName.equals(name1) && action1 == null) {
            // Update action for Player 1 if missing
            action1 = action;
            System.out.println("Updated Action for Player 1: " + playerName);
            sendResponse(exchange, "Action updated for Player 1.", 200);
        } else if (playerName.equals(name2) && action2 == null) {
            // Update action for Player 2 if missing
            action2 = action;
            System.out.println("Updated Action for Player 2: " + playerName);
            resolve(exchange);
        } else {
            sendResponse(exchange, "Invalid or duplicate request.", 400);
        }
    }

    private void resolve(HttpExchange exchange) throws IOException {
        System.out.println("Resolving game...");

        // NOT STATIC STRINGS, NOT A GOOD PRACTICE
        String winner = "TIE";
        switch (action1) {
            case "rock":
                if (action2.equals("paper")) {
                    winner = name2;
                } else if (action2.equals("scissors")) {
                    winner = name1;
                }
                break;
            case "paper":
                if (action2.equals("rock")) {
                    winner = name1;
                } else if (action2.equals("scissors")) {
                    winner = name2;
                }
                break;
            case "scissors":
                if (action2.equals("rock")) {
                    winner = name2;
                } else if (action2.equals("paper")) {
                    winner = name1;
                }
                break;
        }

        String result = "Winner: " + winner;
        System.out.println(result);

        // returning result for both users, the second one who is the one running the current object and the first user who saved his exchange in the object
        sendResponse(this.player1Ex, result, 200);
        sendResponse(exchange, result, 200);
        cleanup();
    }

    private void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
        exchange.sendResponseHeaders(status, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    // cleaning the object (for future usage of regame, currently not used because we are just throwing and creating new game each time
    private void cleanup() {
        games.remove(this);
        name1 = null;
        name2 = null;
        action1 = null;
        action2 = null;
    }

	public static boolean isValidId(String id2) {
		for (Game game : games) {
			if(game.getId().equals(id2)) {
				return true;
			}
		}
		return false;
	}
}
