import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@code UserDatabase}: constructor behavior, add, delete, login,
 * listing users, malformed-line handling, persistence, and basic concurrency.
 *
 * <p>These tests verify expected success paths and common error conditions while
 * avoiding behavior changes to the implementation under test.</p>
 *
 * @version 1.0
 */
public class UserDatabaseTest {

    private static final String TEST_FILE = "test_users.txt";
    private UserDatabase db;

    @BeforeEach
    public void setUp() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_FILE));
        Files.createFile(Paths.get(TEST_FILE));
        db = new UserDatabase(TEST_FILE);
    }

    @AfterEach
    public void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(TEST_FILE));
        } catch (IOException ignored) {
            // Best-effort cleanup; safe to ignore if file was already removed by the test.
        }
    }

    // Test constructor behavior
    @Test
    public void testConstructorCreatesFileIfMissing() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_FILE));
        Assertions.assertFalse(Files.exists(Paths.get(TEST_FILE)));
        UserDatabase temp = new UserDatabase(TEST_FILE);
        Assertions.assertTrue(Files.exists(Paths.get(TEST_FILE)));
    }

    // Test successful addUser
    @Test
    public void testAddUserSuccess() throws Exception {
        String result = db.addUser("alice", "pw1");
        Assertions.assertEquals("Success", result);
        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE));
        Assertions.assertTrue(lines.stream().anyMatch(l -> l.equals("alice,pw1")));
    }

    // Test addUser invalid input
    @Test
    public void testAddUserInvalidInput() {
        Assertions.assertEquals("Invalid input", db.addUser(null, "x"));
        Assertions.assertEquals("Invalid input", db.addUser("", "x"));
        Assertions.assertEquals("Invalid input", db.addUser("x", null));
        Assertions.assertEquals("Invalid input", db.addUser("x", ""));
    }

    // Test duplicate username in addUser
    @Test
    public void testAddUserDuplicateUsername() {
        Assertions.assertEquals("Success", db.addUser("bob", "pw2"));
        Assertions.assertEquals("This username is taken", db.addUser("bob", "pw3"));
    }

    // Test duplicate password in addUser
    @Test
    public void testAddUserDuplicatePassword() {
        Assertions.assertEquals("Success", db.addUser("carol", "secret"));
        Assertions.assertEquals("This password is taken", db.addUser("dave", "secret"));
    }

    // Test successful login
    @Test
    public void testLoginSuccess() {
        db.addUser("eve", "pw5");
        Assertions.assertTrue(db.login("eve", "pw5"));
    }

    // Test wrong password login
    @Test
    public void testLoginWrongPassword() {
        db.addUser("frank", "pw6");
        Assertions.assertFalse(db.login("frank", "badpw"));
    }

    // Test login with no such user
    @Test
    public void testLoginNoSuchUser() {
        Assertions.assertFalse(db.login("ghost", "nopw"));
    }

    // Test deleteUser no such username
    @Test
    public void testDeleteUserNoSuchUsername() {
        Assertions.assertEquals("No such username exists", db.deleteUser("nobody", "pw"));
    }

    // Test deleteUser wrong password
    @Test
    public void testDeleteUserWrongPassword() {
        db.addUser("henry", "pw7");
        Assertions.assertEquals("No such password exists", db.deleteUser("henry", "wrong"));
    }

    // Test successful deleteUser
    @Test
    public void testDeleteUserSuccess() throws Exception {
        db.addUser("ivy", "pw8");
        String result = db.deleteUser("ivy", "pw8");
        Assertions.assertEquals("Success", result);
        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE));
        Assertions.assertTrue(lines.stream().noneMatch(l -> l.equals("ivy,pw8")));
    }

    // Test getAllUsers empty
    @Test
    public void testGetAllUsersEmpty() {
        List<String> users = db.getAllUsers();
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
    }

    // Test getAllUsers after adding users
    @Test
    public void testGetAllUsersPopulated() {
        db.addUser("jack", "pw9");
        db.addUser("kate", "pw10");
        db.addUser("leo", "pw11");
        List<String> users = db.getAllUsers();
        Assertions.assertEquals(3, users.size());
        Assertions.assertTrue(users.containsAll(Arrays.asList("jack", "kate", "leo")));
    }

    // Test persistence across instances
    @Test
    public void testPersistenceMultipleUsers() throws Exception {
        db.addUser("mike", "m1");
        db.addUser("nina", "n1");
        UserDatabase db2 = new UserDatabase(TEST_FILE);
        Assertions.assertTrue(db2.login("mike", "m1"));
        Assertions.assertTrue(db2.login("nina", "n1"));
    }

    // Test ignoring malformed lines
    @Test
    public void testMalformedLinesAreIgnored() throws Exception {
        Files.write(
                Paths.get(TEST_FILE),
                Arrays.asList(
                        "badlinewithnocomma",
                        "too,many,commas,here",
                        "valid1,pwA",
                        ",missingusername",
                        "missingpw,",
                        "valid2,pwB"
                ),
                StandardCharsets.UTF_8
        );
        db = new UserDatabase(TEST_FILE);
        List<String> users = db.getAllUsers();
        Assertions.assertTrue(users.contains("valid1"));
        Assertions.assertTrue(users.contains("valid2"));
        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals("Success", db.addUser("newguy", "newpw"));
    }

    // Test concurrency safety for addUser
    @Test
    public void testConcurrentAdds() throws Exception {
        int threadCount = 5;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<String> usernames = Arrays.asList("u1", "u2", "u3", "u4", "u5");
        List<String> passwords = Arrays.asList("p1", "p2", "p3", "p4", "p5");
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                    results.add(db.addUser(usernames.get(idx), passwords.get(idx)));
                } catch (InterruptedException ignored) {
                    // Preserve interrupt status per best practices.
                    Thread.currentThread().interrupt();
                }
                done.countDown();
            }).start();
        }

        ready.await(1, TimeUnit.SECONDS);
        start.countDown();
        done.await(1, TimeUnit.SECONDS);

        Assertions.assertEquals(threadCount, results.size());
        Assertions.assertTrue(results.stream().allMatch("Success"::equals));
    }
}
