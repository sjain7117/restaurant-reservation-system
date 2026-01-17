import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles client connections and processes restaurant reservation system commands.
 * Each instance runs in a separate thread to manage an individual client session.
 *
 * @version 1.0
 */
public class ClientHandler implements Runnable, ClientHandlerInterface {

    private Socket socket;
    private RestaurantDatabase restaurantDB;
    private UserDatabase userDB;
    private BufferedReader in;
    private PrintWriter out;
    private boolean handOffToAdmin = false;

    /**
     * Constructs a new ClientHandler for managing a client connection.
     *
     * @param socket the client socket connection
     * @param userDB the user database for authentication and account management
     * @param restaurantDB the restaurant database for reservation management
     */
    public ClientHandler(Socket socket, UserDatabase userDB,
                         RestaurantDatabase restaurantDB) {
        this.socket = socket;
        this.userDB = userDB;
        this.restaurantDB = restaurantDB;
    }

    /**
     * Main execution method that listens for client commands and routes them
     * to appropriate handler methods. Runs continuously until the client
     * disconnects or deletes their account.
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String command = in.readLine();
                if (command == null) break;

                switch (command) {
                    case "Logging in":
                        handleLogin();
                        if (handOffToAdmin) return;
                        break;
                    case "Deleting account":
                        handleDeleteAccount();
                        return; // client ends after delete
                    case "Making account":
                        handleMakeAccount();
                        break;
                    case "Canceling Reservation":
                        handleCancelReservation();
                        break;
                    case "Getting All Available Tables":
                        handleGetTables();
                        break;
                    case "Making Reservation":
                        handleMakeReservation();
                        break;
                    default:
                        out.println("Failed");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected unexpectedly.");
        } finally {
            closeConnection();
        }
    }

    /**
     * Processes user login requests by reading credentials from the client
     * and validating them against the user database.
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleLogin() throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        boolean success = userDB.login(username, password);

        if (success && username.equals("Admin")) {
            out.println("Admin HandOff");
            handOffToAdmin = true;
            Thread newThread = new Thread(new AdminHandler(socket, userDB, restaurantDB, in, out));
            newThread.start();
            return;
        }
        if (success) out.println("Success");
        else out.println("Failed");
    }

    /**
     * Handles account deletion requests by verifying credentials and
     * removing the user from the database. Terminates the client connection
     * after deletion.
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleDeleteAccount() throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        String result = userDB.deleteUser(username, password);

        if (result.equals("Success"))
            out.println("Success");
        else
            out.println("Failed");
    }

    /**
     * Creates a new user account with the provided credentials.
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleMakeAccount() throws IOException {
        String username = in.readLine();
        String password = in.readLine();
        String result = userDB.addUser(username, password);

        if (result.equals("Success"))
            out.println("Success");
        else
            out.println("Failed");
    }

    /**
     * Cancels an existing reservation for a user on a specified day.
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleCancelReservation() throws IOException {
        String username = in.readLine();
        String day = in.readLine();
        String result = restaurantDB.cancelReservation(username, day);

        if (result.equals("Cancellation Made"))
            out.println("Success");
        else
            out.println("Failed");
    }

    /**
     * Retrieves and sends a list of available tables for a specific day and time.
     * Tables are returned as a semicolon-delimited string, or an empty string
     * if no tables are available.
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleGetTables() throws IOException {
        String day = in.readLine();
        int time = Integer.parseInt(in.readLine());
        ArrayList<String> tables =
                restaurantDB.getAllAvailableTablesForDayAndTime(day, time);

        if (tables == null || tables.isEmpty()) {
            out.println("");
            return;
        }

        // Construct "table;table;table"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tables.size(); i++) {
            sb.append(tables.get(i));
            if (i < tables.size() - 1) sb.append(";");
        }
        out.println(sb.toString());
    }

    /**
     * Creates a new reservation with the specified details including table number,
     * party size, time, and payment information. Automatically identifies special
     * table requests (table 8).
     *
     * @throws IOException if an I/O error occurs during communication
     */
    private void handleMakeReservation() throws IOException {
        String username = in.readLine();
        String day = in.readLine();
        int tableNum = Integer.parseInt(in.readLine());
        int partySize = Integer.parseInt(in.readLine());
        int time = Integer.parseInt(in.readLine());
        String creditCard = in.readLine();

        boolean isSpecial = tableNum == 8;
        String result = restaurantDB.makeReservation(username, day, tableNum,
                partySize, time, isSpecial, creditCard);

        out.println(result);
    }

    /**
     * Closes all I/O streams and the client socket connection.
     * Called when the client session ends to ensure proper resource cleanup.
     */
    private void closeConnection() {
        if (!handOffToAdmin) {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Connection already closed or error during cleanup
            }
        }
    }
}

