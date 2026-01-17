import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *This class is the handler for if an admin logs into the system. This class processes inputs from
 * the admin client so they can close early or close late.
 *
 * @version Dec 2, 2025
 */

public class AdminHandler implements Runnable, AdminHandlerInterface {
    private Socket socket;
    private RestaurantDatabase restaurantDB;
    private UserDatabase userDB;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * This constructor initializes the socket, database fields, and the reader and writer
     * @param socket The socket to communicate with admin
     * @param userDB The user database
     * @param restaurantDB The restaurant database
     * @param in The reader for the socket
     * @param out The writer for the socket
     */
    public AdminHandler(Socket socket, UserDatabase userDB,
                         RestaurantDatabase restaurantDB, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.userDB = userDB;
        this.restaurantDB = restaurantDB;
        this.in = in;
        this.out = out;
    }

    /**
     * /This method is where the main action happens as a command is read in for whether the admin wants to close late
     * or close early and then the necessary action is taken in the restaurant database.
     */
    public void run() {
        try {
            String day = in.readLine();
            String command = in.readLine();

            String result;
            if (command.equals("Close Late")) {
                result = restaurantDB.adminChange(day, true);
            } else {
                result = restaurantDB.adminChange(day, false);
            }

            if (result.equals("Change Successful")) {
                out.println("Success");
            } else {
                out.println("Failure");
            }

            socket.close();
        } catch (Exception e) {
            System.out.println("Client disconnected unexpectedly.");
        }
    }
}
