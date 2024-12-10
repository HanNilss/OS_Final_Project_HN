import java.util.Random;
import java.net.*;
import java.io.*;

/**
 * Manages the game server, accepting player connections, 
 * generating game parameters, and coordinating game logic.
 * 
 * @author Hannes Nilsson
 * @version 1.0
 */
public class Server {

    private static Random randGen = new Random();// Random number generator
    private static final int PORT = 1014;// Server listening port
    private static final int TURNS = randGen.nextInt(6) + 5;// Number of turns to roll dice
    private static final int GOAL = randGen.nextInt(31) + 20;// Target score
    public static Socket[] players = new Socket[2];// Array to store player sockets
    private static int[] scores = {0, 0};// Players' scores
    private static int[] rolls = {0, 0};// Number of rolls per player, updated after each roll
    private static int playerCount = 0;// Current number of connected players (2 needed for game to work)
    private static Thread[] threads = new Thread[2];//Player's Threads
    private static Object mutex = new Object();//Mutual exclusion object for critical section

    /**
     * Starts the server, waits for player connections, and starts threads when players connect using PlayerHandler.
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting server...");
        System.out.printf("Target: %d | Turns: %d\n", GOAL, TURNS);

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for players...");

            // Wait for two players to connect
            while (playerCount < 2) {
                Socket playerSocket = ss.accept();
                players[playerCount] = playerSocket;
                System.out.println("Player " + (playerCount + 1) + " connected.");
                threads[playerCount] = new Thread(new PlayerHandler(playerSocket, playerCount)); //Creates individual thread for PlayerHandler stored in array
                threads[playerCount].start();
                playerCount++;
            }//end while
            ss.close();
        } catch (IOException e) {
            e.printStackTrace(); 
        }//end catch
    }//end main

    /**
     * Generates a synchronized dice roll using mutex object to ensure concurrency.
     * 
     * @return A random integer between 1 and 6 inclusive
     */
    public static int syncRollDice() {
        synchronized (mutex) {
            return randGen.nextInt(6) + 1;
        }//end synchronized
    }//end syncRollDice

    /**
     * Gets the maximum number of rolls.
     * 
     * @return The number of turns
     */
    public static int getTURNS() {
        return TURNS;
    }//end getTURNS

    /**
     * Gets the target score to reach.
     * 
     * @return The target goal score
     */
    public static int getGOAL() {
        return GOAL;
    }//end getGOAL

    /**
     * Gets the players' scores.
     * 
     * @return An array containing the scores of both players
     */
    public static int[] getScores() {
        return scores;
    }//end getScores

    /**
     * Gets the number of rolls made for a specific player.
     * 
     * @param n The player's index (0 or 1)
     * @return The number of rolls made by the player
     */
    public static int getRolls(int n) {
        return rolls[n];
    }//end getRolls

    /**
     * Gets the current number of connected players.
     * 
     * @return The number of connected players
     */
    public static int getPlayerCount() {
        return playerCount;
    }//end getPlayerCount

    /**
     * Gets all connected player sockets.
     * 
     * @return An array of player sockets
     */
    public static Socket[] getPlayers() {
        return players;
    }//end getPlayers

    /**
     * Gets a specific player's socket.
     * 
     * @param i The player's index in array (0 or 1)
     * @return The player's socket
     */
    public static Socket getPlayer(int i) {
        return players[i];
    }//end getPlayer
    
    /**
     * Gets specific player's thread
     * 
     * @param i The thread's index in array (0 or 1)
     * @return The player's thread
     */
    public static Thread getThread(int i) {
    	return threads[i];
    }//end getThread

    /**
     * Increments the number of rolls for a specific player.
     * 
     * @param n The player's index (0 or 1)
     */
    public static void incRoll(int n) {
        rolls[n]++;
    }//end incRoll
}//end class
