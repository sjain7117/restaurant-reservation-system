/**
 *This class provides a main method to run the static method initializeFiles which
 * initializes the restaurant database files. It also starts a server thread so we have
 * a server running.
 *
 * @version Nov 20, 2025
 */

public class RestaurantDriver {
    public static void main(String[] args) {
        InitializeRestaurantFiles.initializeFiles();
        RestaurantDatabase rd = new RestaurantDatabase();
        UserDatabase ud = new UserDatabase("users.txt");
        //Start server
        Thread serverThread = new Thread(new RestaurantServer(4242, rd, ud));
        serverThread.start();
    }
}
