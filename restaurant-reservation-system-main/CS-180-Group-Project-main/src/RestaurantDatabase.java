import java.io.*;
import java.util.ArrayList;

/**
 *This class is the RestaurantDatabase methods to make, cancel, and see
 * all possible reservations to make.
 *
 * @version Nov 4, 2025
 */

public class RestaurantDatabase implements RestaurantDatabaseInterface {
    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;

    //This constructor initializes all the days to their text files
    public RestaurantDatabase() {
        this.monday = "monday.txt";
        this.tuesday = "tuesday.txt";
        this.wednesday = "wednesday.txt";
        this.thursday = "thursday.txt";
        this.friday = "friday.txt";
        this.saturday = "saturday.txt";
        this.sunday = "sunday.txt";
    }

    /**
     * Allows a user to make a reservation
     *
     * @param username   the username of the person making a reservation
     * @param day        the day the user wants to make a reservation for
     * @param tableNum   the table number of the table the user wants to book
     * @param partySize  the size of the users party
     * @param time       the time the user wants to book the reservation
     * @param isSpecial  whether the table the user is booking is special which means requiring credit card and payment
     * @param creditCard the user's credit card which is used if user is reserving special table.
     * @return A String telling the status of the attempt to reserve the table
     */

    public String makeReservation(String username, String day, int tableNum, int partySize, int time,
                                  boolean isSpecial, String creditCard) {
        Object lock = getLock(day);

        if (lock == null) {
            return "Invalid Day";
        }

        if (!((time >= 11 && time <= 14) || (time >= 17 && time <= 21))) {
            return "Invalid Time";
        }

        boolean reservationMade = false;

        synchronized (lock) {
            String fileName = day + ".txt";
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader bfr = new BufferedReader(new FileReader(new File(fileName)))) {
                while (true) {
                    String line = bfr.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            } catch (Exception e) {
                return "Reservation Failed";
            }

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] lineSplit = line.split(",");
                String booked = lineSplit[0];
                if (booked.equals(username)) {
                    return "User Already Has Reservation For This Day";
                }
            }

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] lineSplit = line.split(",");
                int reserveTime = Integer.parseInt(lineSplit[5]);
                int reserveTableNum = Integer.parseInt(lineSplit[1]);
                int tableSize = Integer.parseInt(lineSplit[2]);
                String special = lineSplit[4];
                String isBooked = lineSplit[6];

                if (reserveTableNum == tableNum) {
                    if (reserveTime == time) {
                        if (isBooked.equals("Yes")) {
                            return "Table Already Booked";
                        }

                        if (partySize > tableSize) {
                            return "Party Too Big";
                        }

                        if (isSpecial) {
                            if (partySize > 8 || partySize <= 4) {
                                return "Party Can't Book Special";
                            }

                            if (special.equals("Yes")) {

                                if (creditCard.length() != 16) {
                                    return "Invalid Credit Card Number";
                                }

                                lineSplit[0] = username;
                                lineSplit[3] = "" + partySize;
                                lineSplit[6] = "Yes";
                                lineSplit[7] = creditCard;
                                lineSplit[8] = "100";

                                String updatedLine = String.join(",", lineSplit);
                                lines.set(i, updatedLine);
                                reservationMade = true;
                            }
                        } else {
                            if (special.equals("No")) {
                                lineSplit[0] = username;
                                lineSplit[3] = "" + partySize;
                                lineSplit[6] = "Yes";

                                String updatedLine = String.join(",", lineSplit);
                                lines.set(i, updatedLine);
                                reservationMade = true;
                            }
                        }
                    }
                }
            }

            if (reservationMade) {
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fileName)))) {
                    for (int i = 0; i < lines.size(); i++) {
                        pw.println(lines.get(i));
                    }
                } catch (Exception e) {
                    return "Reservation Failed";
                }

                return "Reservation Made";
            }
        }

        return "Reservation Failed";
    }

    /**
     * Allows a user to cancel a reservation
     *
     * @param username the username of the person that made a reservation
     * @param day      the day the user wants to cancel the reservation for
     * @return A String telling the status of the attempt to cancel the reservation
     */

    public String cancelReservation(String username, String day) {
        Object lock = getLock(day);

        if (lock == null) {
            return "Invalid Day";
        }

        boolean reservationCanceled = false;

        synchronized (lock) {
            String fileName = day + ".txt";
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader bfr = new BufferedReader(new FileReader(new File(fileName)))) {
                while (true) {
                    String line = bfr.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            } catch (Exception e) {
                return "Cancellation Failed";
            }

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] lineSplit = line.split(",");
                String booked = lineSplit[0];

                if (booked.equals(username)) {
                    lineSplit[0] = "N/A";
                    lineSplit[3] = "N/A";
                    lineSplit[6] = "No";
                    String special = lineSplit[4];
                    if (special.equals("Yes")) {
                        lineSplit[7] = "N/A";
                        lineSplit[8] = "0";
                    }
                    String updatedLine = String.join(",", lineSplit);
                    lines.set(i, updatedLine);
                    reservationCanceled = true;
                }
            }

            if (reservationCanceled) {
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fileName)))) {
                    for (int i = 0; i < lines.size(); i++) {
                        pw.println(lines.get(i));
                    }
                } catch (Exception e) {
                    return "Cancellation Failed";
                }

                return "Cancellation Made";
            }
        }

        return "Cancellation Failed";
    }

    /**
     * Gets all available tables for a given day and time
     *
     * @param day  the day the user wants to make a reservation for
     * @param time the time the user wants to book the reservation
     * @return An ArrayList of type String with strings for each available table
     */

    public ArrayList<String> getAllAvailableTablesForDayAndTime(String day, int time) {
        Object lock = getLock(day);

        if (lock == null) {
            return null;
        }

        synchronized (lock) {
            String fileName = day + ".txt";
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader bfr = new BufferedReader(new FileReader(new File(fileName)))) {
                while (true) {
                    String line = bfr.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] linePieces = line.split(",");
                    int lineTime = Integer.parseInt(linePieces[5]);
                    String isBooked = linePieces[6];
                    if (lineTime == time) {
                        if (isBooked.equals("No")) {
                            lines.add(line);
                        }
                    }
                }

                return lines;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Gets the synchronization lock for the given day
     *
     * @param day the day the user wants to make a reservation for
     * @return An Object which will be synchronized on
     */
    private Object getLock(String day) {
        switch (day) {
            case "monday":
                return MONDAY_LOCK;
            case "tuesday":
                return TUESDAY_LOCK;
            case "wednesday":
                return WEDNESDAY_LOCK;
            case "thursday":
                return THURSDAY_LOCK;
            case "friday":
                return FRIDAY_LOCK;
            case "saturday":
                return SATURDAY_LOCK;
            case "sunday":
                return SUNDAY_LOCK;
        }

        return null;
    }

    /**
     * Executes action specified by the admin to change times and seatings
     *
     * @param day          the day the admin wants to look at
     * @param closingLater whether the admin wants to close later(true) or the admin wants to close early(false)
     * @return A string telling if action was successful or not.
     */
    public String adminChange(String day, boolean closingLater) {
        Object lock = getLock(day);

        if (lock == null) {
            return "Invalid Day";
        }

        synchronized (lock) {
            String fileName = day + ".txt";
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader bfr = new BufferedReader(new FileReader(new File(fileName)))) {
                while (true) {
                    String line = bfr.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            } catch (Exception e) {
                return "Change Failed";
            }

            if (closingLater) {
                //add check if they are already there
                for (int j = 1; j < 4; j++) {
                    String table = String.format("N/A,%d,2,N/A,No,21,No,Not Needed,0", j);
                    lines.add(table);
                }
                for (int j = 4; j < 8; j++) {
                    String table = String.format("N/A,%d,4,N/A,No,21,No,Not Needed,0", j);
                    lines.add(table);
                }
                lines.add("N/A,8,8,N/A,Yes,21,No,N/A,0");

                for (int i = 0; i < lines.size(); i++) {
                    String[] pieces = lines.get(i).split(",");
                    if (pieces[1].equals("8")) {
                        pieces[0] = "N/A";
                        pieces[3] = "N/A";
                        pieces[6] = "No";
                        pieces[7] = "N/A";
                        pieces[8] = "0";
                        lines.set(i, String.join(",", pieces));
                    }
                }
            }

            if (!closingLater) {
                lines.removeIf(l -> l.split(",")[5].equals("21"));

                for (int i = 0; i < lines.size(); i++) {
                    String[] pieces = lines.get(i).split(",");
                    int tableSize = Integer.parseInt(pieces[1]);
                    if (tableSize == 8) {
                        pieces[0] = "N/A";
                        pieces[3] = "N/A";
                        pieces[6] = "No";
                        pieces[7] = "N/A";
                        pieces[8] = "0";
                        lines.set(i, String.join(",", pieces));
                    }
                }
            }

            try (PrintWriter pw = new PrintWriter(fileName)) {
                for (String line : lines) {
                    pw.println(line);
                }
            } catch (Exception e) {
                return "Change Failed";
            }

            return "Change Successful";
        }
    }
}
