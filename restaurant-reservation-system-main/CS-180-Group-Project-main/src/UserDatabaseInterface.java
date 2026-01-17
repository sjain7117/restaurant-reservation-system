import java.util.List;

/**
 * Methods and synchronization objects for a {@code UserDatabase}.
 *
 * <p>Defines operations to add, delete, and authenticate users, as well as a
 * method to list all existing usernames. Also exposes lock objects to coordinate
 * access in multi-threaded contexts.</p>
 *
 * @version 1.0
 */
public interface UserDatabaseInterface {

    /**
     * Adds a new user if the username and password are unique.
     *
     * @param username the username to register.
     * @param password the password for the user.
     * @return status message.
     */

    String addUser(String username, String password);

    /**
     * Deletes an existing user if username and password match.
     *
     * @param username the username to delete.
     * @param password the user's password.
     * @return status message.
     */

    String deleteUser(String username, String password);

    /**
     * Authenticates a user login.
     *
     * @param username the username.
     * @param password the password.
     * @return true if credentials are correct, false otherwise.
     */

    boolean login(String username, String password);

    /**
     * Returns a list of all registered usernames.
     *
     * @return list of usernames.
     */

    List<String> getAllUsers();

    Object USER_ADD_LOCK = new Object();
    Object USER_DELETE_LOCK = new Object();
    Object USER_LOGIN_LOCK = new Object();
    Object USER_LIST_LOCK = new Object();
}
