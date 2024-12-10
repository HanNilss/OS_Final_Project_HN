import java.io.*;
import java.net.*;

/**
 * Connects to the game server and interacts with it.
 * Listens for messages from the server and responds to game prompts.
 * 
 * @author Hannes Nilsson
 * @version 1.0
 */
public class Client {
    private static final String HOST = "127.0.0.1";//Host address
    private static final int PORT = 1014;//Host port

    /**
     * Main method connects to the server, listens for messages, and sends player actions.
     */
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println(in.readLine());

            String response; // message from server

            while ((response = in.readLine()) != null) {
                System.out.println(response);

                // Check if the game has ended
                if (response.contains("WINS") || response.contains("TIE")) {
                	break;
                }//end if

                // Wait for a prompt to roll the dice
                if (response.contains("Type 'ROLL'")) {
                    System.out.print("Enter 'Roll': ");
                    String command = console.readLine(); 
                    out.println(command);  
                }//end if
            }//end while
        }//end try
        catch (IOException e) {
            e.printStackTrace();
        }//end catch
    }//end main
}//end class
