import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@code Client}: specifically tests the private {@code getInput} method.
 *
 * Ensures that user input is correctly read and returned by the method,
 * and that the prompt is displayed to the console.
 *
 * @version 1.1
 */
public class ClientTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    public void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /** Tests that getInput returns the string the user types. */
    @Test
    public void testGetInputReturnsUserInput() throws Exception {
        testIn = new ByteArrayInputStream("Hello World\n".getBytes());
        System.setIn(testIn);

        Method getInputMethod = Client.class.getDeclaredMethod("getInput", String.class);
        getInputMethod.setAccessible(true);

        String result = (String) getInputMethod.invoke(null, "Type something: ");
        assertEquals("Hello World", result);
    }

    /** Tests that getInput correctly prints the prompt to the console. */
    @Test
    public void testGetInputDisplaysPrompt() throws Exception {
        testIn = new ByteArrayInputStream("Test Input\n".getBytes());
        System.setIn(testIn);

        Method getInputMethod = Client.class.getDeclaredMethod("getInput", String.class);
        getInputMethod.setAccessible(true);

        getInputMethod.invoke(null, "Enter value: ");
        String consoleOutput = testOut.toString();
        assertTrue(consoleOutput.contains("Enter value:"));
    }

    /** Tests that getInput returns an empty string if the user presses enter without typing anything. */
    @Test
    public void testGetInputEmptyString() throws Exception {
        testIn = new ByteArrayInputStream("\n".getBytes());
        System.setIn(testIn);

        Method getInputMethod = Client.class.getDeclaredMethod("getInput", String.class);
        getInputMethod.setAccessible(true);

        String result = (String) getInputMethod.invoke(null, "Enter something: ");
        assertEquals("", result);
    }

    /** Tests that getInput handles numeric input correctly. */
    @Test
    public void testGetInputNumeric() throws Exception {
        testIn = new ByteArrayInputStream("12345\n".getBytes());
        System.setIn(testIn);

        Method getInputMethod = Client.class.getDeclaredMethod("getInput", String.class);
        getInputMethod.setAccessible(true);

        String result = (String) getInputMethod.invoke(null, "Enter number: ");
        assertEquals("12345", result);
    }

    /** Tests that getInput works correctly with multiple consecutive calls using separate input streams. */
    @Test
    public void testGetInputMultipleConsecutive() throws Exception {
        Method getInputMethod = Client.class.getDeclaredMethod("getInput", String.class);
        getInputMethod.setAccessible(true);

        // First input
        testIn = new ByteArrayInputStream("First\n".getBytes());
        System.setIn(testIn);
        String r1 = (String) getInputMethod.invoke(null, "Prompt 1: ");

        // Second input
        testIn = new ByteArrayInputStream("Second\n".getBytes());
        System.setIn(testIn);
        String r2 = (String) getInputMethod.invoke(null, "Prompt 2: ");

        // Third input
        testIn = new ByteArrayInputStream("Third\n".getBytes());
        System.setIn(testIn);
        String r3 = (String) getInputMethod.invoke(null, "Prompt 3: ");

        assertEquals("First", r1);
        assertEquals("Second", r2);
        assertEquals("Third", r3);
    }
}
