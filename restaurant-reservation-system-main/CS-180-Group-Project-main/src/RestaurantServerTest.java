import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.ServerSocket;

/**
 * Unit tests for RestaurantServer constructor and field initialization.
 * Verifies proper handling of ports, null databases, and field assignments.
 *
 * @version 1.0
 */
public class RestaurantServerTest {

    private RestaurantDatabase rd;
    private UserDatabase ud;

    @BeforeEach
    public void setup() throws Exception {
        rd = new RestaurantDatabase();
        ud = new UserDatabase("users.txt");
    }

    /** Test that constructor creates a non-null RestaurantServer */
    @Test
    public void testConstructorCreatesObject() {
        RestaurantServer rs = new RestaurantServer(5000, rd, ud);
        assertNotNull(rs);
    }

    /** Test that port creates an actual ServerSocket */
    @Test
    public void testServerSocketIsCreated() throws Exception {
        RestaurantServer rs = new RestaurantServer(5001, rd, ud);

        Field f = RestaurantServer.class.getDeclaredField("serverSocket");
        f.setAccessible(true);
        ServerSocket socket = (ServerSocket) f.get(rs);

        assertNotNull(socket);
        assertFalse(socket.isClosed());
    }

    /** Test that constructor stores the RestaurantDatabase reference */
    @Test
    public void testRestaurantDatabaseIsStored() throws Exception {
        RestaurantServer rs = new RestaurantServer(5002, rd, ud);

        Field f = RestaurantServer.class.getDeclaredField("rd");
        f.setAccessible(true);
        assertEquals(rd, f.get(rs));
    }

    /** Test that constructor stores the UserDatabase reference */
    @Test
    public void testUserDatabaseIsStored() throws Exception {
        RestaurantServer rs = new RestaurantServer(5003, rd, ud);

        Field f = RestaurantServer.class.getDeclaredField("ud");
        f.setAccessible(true);
        assertEquals(ud, f.get(rs));
    }

    /** Test that constructor handles invalid port with no exception */
    @Test
    public void testInvalidPortHandledGracefully() {
        RestaurantServer rs = new RestaurantServer(-1, rd, ud);
        assertNotNull(rs); // object still created
    }

    /** Test that invalid port results in null ServerSocket */
    @Test
    public void testInvalidPortResultsInNullServerSocket() throws Exception {
        RestaurantServer rs = new RestaurantServer(-5, rd, ud);

        Field f = RestaurantServer.class.getDeclaredField("serverSocket");
        f.setAccessible(true);
        assertNull(f.get(rs));
    }

    /** Test that passing null RestaurantDatabase is stored as null */
    @Test
    public void testNullRestaurantDatabase() throws Exception {
        RestaurantServer rs = new RestaurantServer(5004, null, ud);

        Field f = RestaurantServer.class.getDeclaredField("rd");
        f.setAccessible(true);
        assertNull(f.get(rs));
    }

    /** Test that passing null UserDatabase is stored as null */
    @Test
    public void testNullUserDatabase() throws Exception {
        RestaurantServer rs = new RestaurantServer(5005, rd, null);

        Field f = RestaurantServer.class.getDeclaredField("ud");
        f.setAccessible(true);
        assertNull(f.get(rs));
    }

    /** Test that constructor does not throw even if port is already in use */
    @Test
    public void testPortAlreadyInUseDoesNotThrow() throws Exception {
        ServerSocket blocker = new ServerSocket(5006);

        RestaurantServer rs = new RestaurantServer(5006, rd, ud);
        assertNotNull(rs);

        blocker.close();
    }

    /** Test that port already in use results in null serverSocket */
    @Test
    public void testPortInUseResultsInNullServerSocket() throws Exception {
        ServerSocket blocker = new ServerSocket(5007);
        RestaurantServer rs = new RestaurantServer(5007, rd, ud);

        Field f = RestaurantServer.class.getDeclaredField("serverSocket");
        f.setAccessible(true);

        assertNull(f.get(rs));

        blocker.close();
    }
}
