import java.net.*;
import java.io.*;

/**
 * Handles individual player actions in the game.
 * Implements Runnable to support multithreading.
 * 
 * @author Hannes Nilsson
 * @version 1.0
 */
public class PlayerHandler implements Runnable {
    private Socket socket;//The player's socket connection
    private int playerId;//player ID (0 or 1)

    /**
     * Initializes a PlayerHandler with a socket and player ID.
     * 
     * @param socket   The player's socket connection
     * @param playerId The player's ID (0 or 1)
     */
    public PlayerHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
    }//end constructor

    /**
     * Handles the game logic for the connected player.
     * Manages game rounds, dice rolls, communication, and game results.
     */
    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.printf("Welcome, Player %d! Target: %d | Max Rolls: %d\n", playerId + 1, Server.getGOAL(), Server.getTURNS());

            // Wait until both players are connected
            while (Server.getPlayerCount() < 2) {
                Thread.sleep(100);
            }//end while

            // Game logic
            for (int i = 1; i <= Server.getTURNS(); i++) {
            	out.println("Type 'ROLL' to roll the dice.");

                String command = in.readLine();  // Wait for player input

                if ("ROLL".equalsIgnoreCase(command)) {
                    int roll = Server.syncRollDice();
                    Server.getScores()[playerId] += roll;
                    Server.incRoll(playerId);

                    out.printf("You rolled %d. Total Score: %d\n", roll, Server.getScores()[playerId]);

                    System.out.printf("Player %d rolled %d. Total Score: %d\n", playerId + 1, roll, Server.getScores()[playerId]);

                    broadcastMessage(String.format("Player %d rolled %d | Total Score: %d", playerId + 1, roll, Server.getScores()[playerId]));
                }//end if
                else {
                    out.println("Invalid command. Type 'ROLL' to play.");
                    i--; 
                }//end else
            }//end for

            // Announce winner when all players finish
            if (allPlayersFinished()) {
                announceWinner();
            }//end if
            else {
                while (!allPlayersFinished()) {
                    Thread.sleep(1000);
                }//end while
                announceWinner();
            }//end else
        }//end try
        catch (IOException e) {
            System.out.println("Player " + (playerId + 1) + " disconnected.");
        }//end catch
        catch (InterruptedException e) {
            e.printStackTrace();
        }//end catch
    }//end run

    /**
     * Sends a message to the opponent player.
     * 
     * @param message The message to be sent
     * @throws IOException If an error occurs while sending the message
     */
    private void broadcastMessage(String message) throws IOException {
            if(playerId == 0){
                PrintWriter clientOut = new PrintWriter(Server.getPlayer(1).getOutputStream(), true);
                clientOut.println(message);
            }//end if
            else {
              	PrintWriter clientOut = new PrintWriter(Server.getPlayer(0).getOutputStream(), true);
                clientOut.println(message);
            }//end else
    }//end broadcastMessage

    /**
     * Checks if both players have completed required rolls.
     * 
     * @return True if both players have completed all rolls, false otherwise
     */
    private boolean allPlayersFinished() {
        return Server.getRolls(0) == Server.getTURNS() && Server.getRolls(1) == Server.getTURNS();
    }//end allPlayersFinished

    /**
     * Announces the game winner to all players based on how close each player got to goal. Interrupts the thread once executed to signify end of game.
     * 
     * @throws IOException If an error occurs while sending the result messages
     */
    private void announceWinner() throws IOException {
        int diff1 = Math.abs(Server.getScores()[0] - Server.getGOAL());
        int diff2 = Math.abs(Server.getScores()[1] - Server.getGOAL());

        String result;
        if (diff1 < diff2) {
            result = "Player 1 WINS!";
        }//end if
        else if (diff2 < diff1) {
            result = "Player 2 WINS!";
        }//end else if
        else {
            result = "It's a TIE!";
        }//end else

        for (Socket client : Server.getPlayers()) {
            PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
            clientOut.printf("Game Over! Final Scores: Player 1: %d | Player 2: %d\n", Server.getScores()[0], Server.getScores()[1]);
            clientOut.println("Closest to the target wins!");
            clientOut.println(result);
        }//end for
        Server.getThread(playerId).interrupt();
    }//end announceWinner
}//end class
