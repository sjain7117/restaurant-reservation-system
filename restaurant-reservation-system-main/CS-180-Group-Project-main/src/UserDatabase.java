import java.io.*;
import java.util.*;

/**
 * Handles secure user registration, deletion, and authentication.
 * Data is stored in a plain text file where each line follows the format:
 * username,password
 *
 * All file operations are synchronized for thread safety.
 *
 * Example usage:
 * UserDatabase db = new UserDatabase("users.txt");
 * db.addUser("john", "pass123");
 * db.login("john", "pass123");
 *
 * @version 1.0
 */
public class UserDatabase {

    private final String fileName;

    /**
     * Constructs a UserDatabase linked to the specified file.
     *
     * @param fileName the file where user credentials are stored.
     */
    public UserDatabase(String fileName) {
        this.fileName = fileName;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error initializing database file: " + e.getMessage());
        }
    }

    /**
     * Adds a new user if the username and password are unique.
     *
     * @param username the username to register.
     * @param password the password for the user.
     * @return status message.
     */
    public synchronized String addUser(String username, String password) {
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            return "Invalid input";
        }

        try {
            List<String[]> users = readUsers();
            for (String[] user : users) {
                if (user[0].equals(username)) {
                    return "This username is taken";
                }
                if (user[1].equals(password)) {
                    return "This password is taken";
                }
            }

            try (FileWriter fw = new FileWriter(fileName, true)) {
                fw.write(username + "," + password + System.lineSeparator());
            }
            return "Success";
        } catch (IOException e) {
            return "File write error";
        }
    }

    /**
     * Deletes an existing user if username and password match.
     *
     * @param username the username to delete.
     * @param password the user's password.
     * @return status message.
     */
    public synchronized String deleteUser(String username, String password) {
        try {
            List<String[]> users = readUsers();
            boolean usernameFound = false;
            boolean passwordMatch = false;

            Iterator<String[]> iterator = users.iterator();
            while (iterator.hasNext()) {
                String[] user = iterator.next();
                if (user[0].equals(username) && !user[1].equals("Admin")) {
                    usernameFound = true;
                    if (user[1].equals(password)) {
                        passwordMatch = true;
                        iterator.remove();
                        RestaurantDatabase rb = new RestaurantDatabase();
                        rb.cancelReservation(username, "monday");
                        rb.cancelReservation(username, "tuesday");
                        rb.cancelReservation(username, "wednesday");
                        rb.cancelReservation(username, "thursday");
                        rb.cancelReservation(username, "friday");
                        rb.cancelReservation(username, "saturday");
                        rb.cancelReservation(username, "sunday");
                        break;
                    }
                }
            }

            if (!usernameFound) {
                return "No such username exists";
            }
            if (!passwordMatch) {
                return "No such password exists";
            }

            try (FileWriter fw = new FileWriter(fileName, false)) {
                for (String[] user : users) {
                    fw.write(user[0] + "," + user[1] + System.lineSeparator());
                }
            }
            return "Success";
        } catch (IOException e) {
            return "File write error";
        }
    }

    /**
     * Authenticates a user login.
     *
     * @param username the username.
     * @param password the password.
     * @return true if credentials are correct, false otherwise.
     */
    public synchronized boolean login(String username, String password) {
        try {
            List<String[]> users = readUsers();
            for (String[] user : users) {
                if (user[0].equals(username) && user[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns a list of all registered usernames.
     *
     * @return list of usernames.
     */
    public synchronized List<String> getAllUsers() {
        List<String> usernames = new ArrayList<>();
        try {
            List<String[]> users = readUsers();
            for (String[] user : users) {
                usernames.add(user[0]);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return usernames;
    }

    /**
     * Helper method: reads all users from file.
     */
    private List<String[]> readUsers() throws IOException {
        List<String[]> users = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(fileName))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                        users.add(parts);
                    }
                }
            }
        }
        return users;
    }
}
