import java.io.*;
import java.net.*;
import java.util.*;

/**
 *This class is the server which implements the Runnable interface so a thread can be made for this
 * object and when run will have a server run infinitely and search for connections with clients.
 *
 * @version Dec 2, 2025
 */

public class RestaurantServer implements Runnable, RestaurantServerInterface {
    private ServerSocket serverSocket;
    private RestaurantDatabase rd;
    private UserDatabase ud;

    /**
     * This constructor initializes the portNumber for the socket, the restaurant database, and the user database
     * @param portNumber The port number for the socket
     * @param rd The restaurant database
     * @param ud The user database
     */
    public RestaurantServer(int portNumber, RestaurantDatabase rd, UserDatabase ud) {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (Exception e) {
            return;
        }

        this.rd = rd;
        this.ud = ud;
    }

    /**
     * The server runs forever and is constantly checking for connections with clients
     * so it can start a ClientHandler thread to handle input from the client.
     */
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Thread newThread = new Thread(new ClientHandler(socket, ud, rd));
                newThread.start();

            } catch (Exception e) {
                return;
            }
        }
    }
}
