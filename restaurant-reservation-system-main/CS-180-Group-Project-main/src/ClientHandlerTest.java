import org.junit.jupiter.api.*;
import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * This class provides JUnit tests for ClientHandler. No I/O is tested.
 * The tests ensure ClientHandler can be constructed properly.
 *
 * @version November 19, 2025
 *
 */

public class ClientHandlerTest {

    private Socket mockSocket;
    private UserDatabase mockUserDB;
    private RestaurantDatabase mockRestaurantDB;
    private ClientHandler mockClientHandler;

    @BeforeEach
    public void setUp() {
        mockSocket = null;
        mockUserDB = new UserDatabase("textUsers.txt");
        mockRestaurantDB = new RestaurantDatabase();
    }


    @Test
    public void testClientHandlerConstructor() {
        ClientHandler clientHandler = new ClientHandler(mockSocket, mockUserDB, mockRestaurantDB);
        assertNotNull(clientHandler);
    }

    @Test
    public void testConstructorWithValidSocket() {
        try {
            Socket socket = new Socket();
            mockClientHandler = new ClientHandler(socket, mockUserDB, mockRestaurantDB);

            assertNotNull(mockClientHandler);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConstructorWithNullSocket() {
        ClientHandler mockClientHandler2 = new ClientHandler(null, mockUserDB, mockRestaurantDB);
        assertNotNull(mockClientHandler2);
    }

    /**
     *
     * This class mimics a socket by acting as a mock socket to test client handler to admin handler handoff.
     *
     * @author Sahil Jain
     * @version December 4, 2025
     *
     */

    private static class MockSocket extends Socket {
        private final InputStream input;
        private final ByteArrayOutputStream output;
        private boolean closedFlag = false;

        public MockSocket(String inputData) {
            this.input = new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
            this.output = new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }

        @Override
        public synchronized void close() {
            closedFlag = true;
        }

        public boolean isClosedFlag() {
            return closedFlag;
        }

        public String getOutputAsString() {
            return output.toString();
        }
    }

    /**
     *
     * This class mimics the user database to test client handler to admin handler handoff
     *
     * @author Sahil Jain
     * @version December 4, 2025
     *
     */
    private static class AdminUserDBStub extends UserDatabase {
        public AdminUserDBStub() {
            super("dummyFile.txt");
        }

        @Override
        public boolean login(String username, String password) {
            return true; // Always succeed for simplicity
        }
    }

    // Tests that when an admin logs in, the handler hands off to AdminHandler and does not close the socket.
    @Test
    public void testAdminLoginHandsOffAndDoesNotCloseSocket() {
        String inputData = "Logging in\nAdmin\npassword\n";
        MockSocket socket = new MockSocket(inputData);

        UserDatabase adminUserDB = new AdminUserDBStub();
        RestaurantDatabase restaurantDB = new RestaurantDatabase();

        ClientHandler handler = new ClientHandler(socket, adminUserDB, restaurantDB);
        handler.run();

        assertFalse(socket.isClosedFlag());
    }

    // Tests that a normal user login does not trigger admin handoff and the socket is closed when finished.
    @Test
    public void testNonAdminLoginClosesSocket() {
        String inputData = "Logging in\nUser\npassword\n";
        MockSocket socket = new MockSocket(inputData);

        UserDatabase adminUserDB = new AdminUserDBStub();
        RestaurantDatabase restaurantDB = new RestaurantDatabase();

        ClientHandler handler = new ClientHandler(socket, adminUserDB, restaurantDB);
        handler.run();

        assertTrue(socket.isClosedFlag());
    }
}
