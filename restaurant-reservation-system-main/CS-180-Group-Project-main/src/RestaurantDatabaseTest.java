import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This class provides jUnit test cases for the RestaurantDatabase. The tests ensure
 * that the parameters entered when an attempt to make or cancel a reservation is made
 * are valid. The tests also ensure that when prompted to get all the available tables
 * for a day and time that the day and time are valid.
 *
 * @version Nov 4, 2025
 */

public class RestaurantDatabaseTest {

    private static final String[] DAYS = {"monday.txt", "tuesday.txt", "wednesday.txt", "thursday.txt", "friday.txt",
        "saturday.txt", "sunday.txt"};
    private RestaurantDatabase db;

    //Initialize restaurant files
    @BeforeEach
    public void setUp() {
        InitializeRestaurantFiles.initializeFiles();
        db = new RestaurantDatabase();
    }

    //Delete test files
    @AfterEach
    public void tearDown() {
        for (String day : DAYS) {
            try {
                Files.deleteIfExists(Paths.get(day));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InitializeRestaurantFiles.initializeFiles();
    }

    //Test if user already has a reservation for this day
    @Test
    public void testUserAlreadyHasReservationForThisDay() {
        String firstReservation = db.makeReservation("Steve", "monday", 2, 2, 11, false, "No");
        Assertions.assertEquals("Reservation Made", firstReservation);
        String secondReservation = db.makeReservation("Steve", "monday", 2, 2, 11, false, "No");
        Assertions.assertEquals("User Already Has Reservation For This Day", secondReservation);
    }

    //Test if making a reservation is a success
    @Test
    public void testMakeReservationSuccess() {
        String result = db.makeReservation("Bob", "monday", 1, 2, 11, false, "No");
        Assertions.assertEquals("Reservation Made", result);

        try {
            var lines = Files.readAllLines(Paths.get("monday.txt"));
            Assertions.assertTrue(lines.stream().anyMatch(line -> line.contains("Bob")));
        } catch (IOException e) {
            Assertions.fail("Could not read files after reservation");
        }
    }

    //Test if user tries to make a reservation for a valid table number
    @Test
    public void testMakeReservationInvalidTableNumber() {
        String result = db.makeReservation("Veronica", "monday", 100000, 2, 11, false, "No");
        Assertions.assertEquals("Reservation Failed", result);
    }

    //Test if user tries to make a reservation for a valid day
    @Test
    public void testMakeReservationFailsForDay() {
        String result = db.makeReservation("Bob", "tuesrar", 1, 2, 1, false, "No");
        Assertions.assertEquals("Invalid Day", result);
    }

    //Test if user tries to make a reservation for a valid time
    @Test
    public void testMakeReservationInvalidTime() {
        String result = db.makeReservation("Bob", "monday", 1, 2, 10, false, "No");
        Assertions.assertEquals("Invalid Time", result);
    }

    //Test if user tires to make a reservation for an already booked table
    @Test
    public void testMakeReservationTableAlreadyBooked() {
        db.makeReservation("Steve", "monday", 2, 2, 11, false, "No");
        String result = db.makeReservation("Bob", "monday", 2, 2, 11, false, "No");
        Assertions.assertEquals("Table Already Booked", result);
    }

    //Test if user tries to make a reservation for a special table with the correct party size
    @Test
    public void testMakeReservationSpecialTablePartyNotCorrectSize() {
        String result = db.makeReservation("Steve", "monday", 8, 3, 11, true, "568283957272");
        Assertions.assertEquals("Party Can't Book Special", result);
    }

    //Test if user's credit card number is invalid if booking a special table
    @Test
    public void testMakeReservationSpecialTablePartyIncorrectCardNumber() {
        String result = db.makeReservation("Steve", "monday", 8, 5, 11, true, "cardnumber");
        Assertions.assertEquals("Invalid Credit Card Number", result);
    }

    //Test if user tries to make a reservation with a party size too big
    @Test
    public void testMakeReservationPartyTooBig() {
        String result = db.makeReservation("Steve", "monday", 3, 5, 11, false, "No");
        Assertions.assertEquals("Party Too Big", result);
    }

    //Test if user has a reservation to cancel
    @Test
    public void testCancelReservationSuccess() {
        db.makeReservation("George", "monday", 2, 2, 11, false, "No");
        String result = db.cancelReservation("George", "monday");
        Assertions.assertEquals("Cancellation Made", result);

        try {
            var lines = Files.readAllLines(Paths.get("monday.txt"));
            Assertions.assertTrue(lines.stream().anyMatch(line -> line.startsWith("N/A")));
        } catch (IOException e) {
            Assertions.fail("Could not read files after reservation cancellation");
        }
    }

    //Test if user is trying to cancel a reservation for a valid day
    @Test
    public void testCancelReservationInvalidDay() {
        String result = db.cancelReservation("Veronica", "saturdy");
        Assertions.assertEquals("Invalid Day", result);
    }

    //Test if user entered a valid username to cancel their reservation
    @Test
    public void testCancelReservationUserNotFound() {
        String result = db.cancelReservation("noUser", "monday");
        Assertions.assertEquals("Cancellation Failed", result);

    }

    //Test if all available tables for day and time can be found
    @Test
    public void testGetAllAvailableTablesForDayAndTime() {
        ArrayList<String> availableTables = db.getAllAvailableTablesForDayAndTime("monday", 11);
        Assertions.assertNotNull(availableTables);
        //8 total tables available for time 11
        Assertions.assertEquals(8, availableTables.size());
    }

    //Test if all available tables for an invalid day
    @Test
    public void testGetAllAvailableTablesForInvalidDay() {
        ArrayList<String> availableTables = db.getAllAvailableTablesForDayAndTime("wenksday", 1);
        Assertions.assertNull(availableTables);
    }

    //Helper to read the maximum time value present in a day's file.
    private int getMaxTimeForDay(String fileName) {
        try {
            var lines = Files.readAllLines(Paths.get(fileName));
            int max = 0;
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length > 5) {
                    int t = Integer.parseInt(parts[5]);
                    if (t > max) {
                        max = t;
                    }
                }
            }
            return max;
        } catch (IOException e) {
            return 0;
        }
    }

    //Test if adminChange returns Invalid Day when given an incorrect day string.
    @Test
    public void testAdminChangeInvalidDay() {
        String result = db.adminChange("notaday", true);
        Assertions.assertEquals("Invalid Day", result);
    }

    //Test adminChange behavior when closing later, depending on the current maximum time.
    @Test
    public void testAdminChangeCloseLaterBehavior() {
        int initialMax = getMaxTimeForDay("monday.txt");
        String result = db.adminChange("monday", true);
        Assertions.assertEquals("Change Successful", result);
        int newMax = getMaxTimeForDay("monday.txt");
        Assertions.assertEquals(21, newMax);

        try {
            var lines = Files.readAllLines(Paths.get("monday.txt"));
            long count21 = lines.stream().filter(line -> {
                String[] parts = line.split(",");
                return parts.length > 5 && parts[5].equals("21");
            }
               ).count();

            Assertions.assertEquals(16, count21);
        } catch (IOException e) {
            Assertions.fail("Could not read monday.txt after adminChange close later");
        }
    }

    //Test that adminChange closing earlier clears special table reservations when the change succeeds.
    @Test
    public void testAdminChangeCloseEarlierClearsSpecialTableReservations() {
        String validCard = "0000000012345678";

        int[] timesToTry = {11, 12, 13, 14, 17, 18, 19, 20, 21};
        String reservationResult = "Reservation Failed";
        int usedTime = -1;

        for (int t : timesToTry) {
            reservationResult = db.makeReservation("SpecialUser", "monday", 8, 5, t, true, validCard);
            if (reservationResult.equals("Reservation Made")) {
                usedTime = t;
                break;
            }
        }

        Assertions.assertEquals("Reservation Made", reservationResult);
        Assertions.assertNotEquals(-1, usedTime);

        String result = db.adminChange("monday", false);

        if (!result.equals("Change Successful")) {
            Assertions.assertEquals("Change Failed", result);
            return;
        }

        try {
            var lines = Files.readAllLines(Paths.get("monday.txt"));
            boolean foundSpecialLine = false;
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 9 && parts[2].equals("8")) {
                    foundSpecialLine = true;
                    Assertions.assertEquals("N/A", parts[0]);
                    Assertions.assertEquals("N/A", parts[3]);
                    Assertions.assertEquals("Yes", parts[6]);
                    Assertions.assertEquals("N/A", parts[7]);
                    Assertions.assertEquals("0", parts[8]);
                }
            }
            Assertions.assertTrue(foundSpecialLine);
        } catch (IOException e) {
            Assertions.fail("Could not read monday.txt after adminChange close earlier");
        }
    }
}
