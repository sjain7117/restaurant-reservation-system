import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

/**
 * This class provides jUnit test cases for the AdminHandler. The tests ensure
 * that admin commands are correctly translated into calls to RestaurantDatabase
 * and that responses are written back to the client appropriately.
 *
 * No real network I/O or file operations are performed; RestaurantDatabase is
 * stubbed and the socket is an unconnected local instance.
 *
 * @version Dec 2, 2025
 */
public class AdminHandlerTest {

    /**
     * Simple stub of RestaurantDatabase that records the last adminChange call
     * and returns a configurable result string.
     */
    private static class TestRestaurantDatabase extends RestaurantDatabase {
        String lastDay;
        Boolean lastClosingLater;
        String resultToReturn = "Change Successful";

        @Override
        public String adminChange(String day, boolean closingLater) {
            this.lastDay = day;
            this.lastClosingLater = closingLater;
            return resultToReturn;
        }
    }

    // Tests that "Close Late" command calls adminChange with closingLater = true and sends "Success" on success.
    @Test
    public void testCloseLateSuccess() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();
        db.resultToReturn = "Change Successful";

        String input = "monday\nClose Late\n";
        BufferedReader in = new BufferedReader(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        handler.run();

        Assertions.assertEquals("monday", db.lastDay);
        Assertions.assertEquals(Boolean.TRUE, db.lastClosingLater);
        Assertions.assertEquals("Success\n", baos.toString());
        Assertions.assertTrue(socket.isClosed());
    }

    // Tests that "Close Early" command uses closingLater = false and sends "Success" when change is successful.
    @Test
    public void testCloseEarlySuccess() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();
        db.resultToReturn = "Change Successful";

        String input = "tuesday\nClose Early\n";
        BufferedReader in = new BufferedReader(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        handler.run();

        Assertions.assertEquals("tuesday", db.lastDay);
        Assertions.assertEquals(Boolean.FALSE, db.lastClosingLater);
        Assertions.assertEquals("Success\n", baos.toString());
        Assertions.assertTrue(socket.isClosed());
    }

    // Tests that a non "Close Late" command still uses closingLater = false when calling adminChange.
    @Test
    public void testNonCloseLateCommandUsesClosingLaterFalse() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();
        db.resultToReturn = "Change Successful";

        String input = "wednesday\nSomeOtherCommand\n";
        BufferedReader in = new BufferedReader(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        handler.run();

        Assertions.assertEquals("wednesday", db.lastDay);
        Assertions.assertEquals(Boolean.FALSE, db.lastClosingLater);
        Assertions.assertEquals("Success\n", baos.toString());
        Assertions.assertTrue(socket.isClosed());
    }

    // Tests that when adminChange returns "Change Failed", AdminHandler sends "Failure" back to the client.
    @Test
    public void testAdminChangeFailureOutputsFailure() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();
        db.resultToReturn = "Change Failed";

        String input = "thursday\nClose Late\n";
        BufferedReader in = new BufferedReader(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        handler.run();

        Assertions.assertEquals("thursday", db.lastDay);
        Assertions.assertEquals(Boolean.TRUE, db.lastClosingLater);
        Assertions.assertEquals("Failure\n", baos.toString());
        Assertions.assertTrue(socket.isClosed());
    }

    // Tests that when adminChange returns a non-success string like "Invalid Day", AdminHandler still sends "Failure".
    @Test
    public void testAdminChangeInvalidDayOutputsFailure() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();
        db.resultToReturn = "Invalid Day";

        String input = "notaday\nClose Early\n";
        BufferedReader in = new BufferedReader(new StringReader(input));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        handler.run();

        Assertions.assertEquals("notaday", db.lastDay);
        Assertions.assertEquals(Boolean.FALSE, db.lastClosingLater);
        Assertions.assertEquals("Failure\n", baos.toString());
        Assertions.assertTrue(socket.isClosed());
    }

    // Tests that AdminHandler handles exceptions inside run without throwing them to the caller.
    @Test
    public void testRunHandlesExceptionGracefully() throws Exception {
        TestRestaurantDatabase db = new TestRestaurantDatabase();

        BufferedReader in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos, true);

        Socket socket = new Socket();
        AdminHandler handler = new AdminHandler(socket, null, db, in, out);

        Assertions.assertDoesNotThrow(handler::run);
    }
}
