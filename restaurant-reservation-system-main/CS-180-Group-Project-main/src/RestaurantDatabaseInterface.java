import java.util.ArrayList;

/**
 *This interface provides the methods and synchronization obejcts for
 * RestaurantDatabase
 *
 * @version Nov 4, 2025
 */

public interface  RestaurantDatabaseInterface {
    /**
     * Allows a user to make a reservation
     *
     * @param username the username of the person making a reservation
     * @param day the day the user wants to make a reservation for
     * @param tableNum the table number of the table the user wants to book
     * @param partySize the size of the users party
     * @param time the time the user wants to book the reservation
     * @param isSpecial whether the table the user is booking is special which means requiring credit card and payment
     * @param creditCard the user's credit card which is used if user is reserving special table.
     * @return A String telling the status of the attempt to reserve the table
     */

    String makeReservation(String username, String day, int tableNum, int partySize, int time, boolean isSpecial,
                           String creditCard);

    /**
     * Allows a user to cancel a reservation
     *
     * @param username the username of the person that made a reservation
     * @param day the day the user wants to cancel the reservation for
     * @return A String telling the status of the attempt to cancel the reservation
     */

    String cancelReservation(String username, String day);

    /**
     * Gets all available tables for a given day and time
     *
     * @param day the day the user wants to make a reservation for
     * @param time the time the user wants to book the reservation
     * @return An ArrayList of type String with strings for each available table
     */
    ArrayList<String> getAllAvailableTablesForDayAndTime(String day, int time);

    /**
     * Executes action specified by the admin to change times and seatings
     *
     * @param day the day the admin wants to look at
     * @param closingLater whether the admin wants to close later(true) or the admin wants to close early(false)
     * @return A string telling if action was successful or not.
     */
    String adminChange(String day, boolean closingLater);

    Object MONDAY_LOCK = new Object();
    Object TUESDAY_LOCK = new Object();
    Object WEDNESDAY_LOCK = new Object();
    Object THURSDAY_LOCK = new Object();
    Object FRIDAY_LOCK = new Object();
    Object SATURDAY_LOCK = new Object();
    Object SUNDAY_LOCK = new Object();
}
