import java.io.*;
import java.net.*;
import java.util.*;

/**
 *This class is the Phase 2 client where the user types input through the terminal
 * to interact with the server and do actions like login, delete account, make reservation, cancel reservation.
 * This client has no error handling since it will be replaced with a GUI later.
 *
 * @version Nov 19, 2025
 */

public class Client implements ClientInterface {
    public static void main(String[] args) {
        System.out.println("Welcome to the CS Italian Restaurant!");
        try (Socket socket = new Socket("localhost", 4242);) {

            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream());

            while (true) {
                //User database stuff
                //------------------------------------------------------
                //Asking user if they have an account or not
                String haveAccount = getInput("Would you like to login(l), delete your account(d), or make an account" +
                        "(m)?");

                //Cases for what user said
                String username = getInput("What is your username: ");
                String password = getInput("What is your password: ");
                if (haveAccount.equalsIgnoreCase("l")) {
                    //If the user does have an account and wants to log in
                    //Telling the clientHandler that client wants to log in
                    //Sending username and password
                    pw.println("Logging in");
                    pw.println(username);
                    pw.println(password);
                    pw.flush();

                    //Reading in whether clientHandler said login = "Failed" or "Success"
                    String login = bfr.readLine();
                    if (login.equals("Failed")) {
                        System.out.println("Username and/or Password are invalid. Taking you back to main menu.");
                        continue;
                    } else if (login.equalsIgnoreCase("Success")) {
                        System.out.println("Login Successful! Taking you to reservation page.");
                    }

                } else if (haveAccount.equalsIgnoreCase("d")) {
                    //If the user does have an account and wants to delete it
                    //Telling the clientHandler that client wants to delete account
                    //Sending username and password
                    pw.println("Deleting account");
                    pw.println(username);
                    pw.println(password);
                    pw.flush();

                    //Reading in whether clientHandler said delete = "Failed" or "Success"
                    String delete = bfr.readLine();
                    if (delete.equals("Failed")) {
                        System.out.println("Username and/or Password are invalid. Taking you back to main menu.");
                        continue;
                    } else if (delete.equals("Success")) {
                        System.out.println("Deletion of account was successful. Have a pleasant day.");
                        break;
                    }

                } else if (haveAccount.equalsIgnoreCase("m")) {
                    //If the user doesn't have an account and wants to make one
                    //Telling the clientHandler that client wants to make account
                    //Sending username and password
                    pw.println("Making account");
                    pw.println(username);
                    pw.println(password);
                    pw.flush();

                    //Reading in whether clientHandler said make = "Failed" or "Success"
                    String make = bfr.readLine();
                    if (make.equals("Failed")) {
                        System.out.println("Username and/or Password are taken. Taking you back to main menu.");
                        continue;
                    } else if (make.equals("Success")) {
                        System.out.println("Creation of account was successful! Taking you to reservation page.");
                    }
                }

                //------------------------------------------------------
                //End of User database stuff


                //Restaurant database stuff
                //Seeing if user wants to make or cancel a reservation
                String reserveAction = getInput("Would you like to make(m) or cancel(c) a reservation?");

                //Getting a valid day and time
                String day = getInput("What day would you like to look at? (monday, tuesday, " +
                        "wednesday, thursday, friday, saturday, or sunday): ");

                //If user wants to cancel a reservation
                if (reserveAction.equals("c")) {
                    pw.println("Canceling Reservation");
                    pw.println(username);
                    pw.println(day);
                    pw.flush();

                    //Reading in whether clientHandler said cancel = "Failed" or "Success"
                    String cancel = bfr.readLine();
                    if (cancel.equals("Failed")) {
                        System.out.println("Could not find a reservation under your username for the given day.");
                        System.out.println("Returning you back to main page.");
                        continue;
                    } else if (cancel.equals("Success")) {
                        System.out.println("Reservation successfully canceled");
                        System.out.println("Have a good day");
                        break;
                    }
                }

                //If user wants to make a reservation
                //First we want to call getAllAvailableTablesForDayAndTime in RestaurantDatabase to show user options
                //I'm getting a valid time, and we already have valid day
                String time = getInput("What time would you like to look at reservations for? (11:00, 12:00, " +
                        "1:00, 2:00, 5:00, 6:00, 7:00, 8:00, 9:00");

                String[] pieces = time.split(":");
                int timeConverted = Integer.parseInt(pieces[0]);
                if (timeConverted < 11) {
                    timeConverted += 12;
                }
                pw.println("Getting All Available Tables");
                pw.println(day);
                pw.println(timeConverted);
                pw.flush();

                //We can send one giant string of the lines from the database file seperated by ;
                //Otherwise we have to do error handling for looping in network io

                //Kavin you will call getAllAvailableTablesForDayAndTime and add every index to a string with lines
                //separated by ;
                //                               ;<-separation
                //Ex. N/A,8,8,N/A,Yes,11,No,N/A,0;N/A,1,2,N/A,No,13,No,Not Needed,0
                //Make sure you look at the method and understand that null or an empty array list can be returned
                String tables = bfr.readLine();
                if (tables == null || tables.isEmpty()) {
                    System.out.println("Sorry no available tables for that day and time");
                    System.out.println("Returning to main menu");
                    continue;
                }
                String[] splitTables = tables.split(";");

                //Printing out all the available tables
                System.out.println("Available Tables: ");
                for (int i = 0; i < splitTables.length; i++) {
                    String line = splitTables[i];
                    String[] linePieces = line.split(",");
                    String table = String.format("Table %s, Max Party Size %s", linePieces[1], linePieces[2]);
                    if (linePieces[4].equals("Yes")) {
                        table += ", Is A Special Table That Requires Payment";
                    }

                    System.out.println(table);
                }

                //Doing reservation
                //Getting table number that user wants
                String tableNum = getInput("What table number would you like to reserve: ");
                int intTableNum = Integer.parseInt(tableNum);

                //Getting credit card is user wants special table
                String creditCard = "";
                if (intTableNum == 8) {
                    creditCard = getInput("What is your credit card number to pay for the special table: ");
                }

                //Getting user party size
                String partySize = getInput("What is your party size?");

                //Sending all information needed for reservation
                pw.println("Making Reservation");
                pw.println(username);
                pw.println(day);
                pw.println(tableNum);
                pw.println(partySize);
                pw.println(timeConverted);
                pw.println(creditCard);
                pw.flush();

                //Reading if reservation was successful or not. Just send the string the reservation method sends.
                String reserve = bfr.readLine();
                System.out.println("Result of Reservation: " + reserve);
                if (reserve.equals("Reservation Made")) {
                    System.out.println("Thank you for reserving with us, have a good day!");
                    break;
                }

                System.out.println("Taking you back to main menu.");
            }

            bfr.close();
            pw.close();

        } catch (Exception e) {
            System.out.println("Failure Encountered.");
        }

    }

    /**
     * Gets input from the user given the provided statement
     *
     * @param statement The statement to show the user
     * @return The user's input based off the statement provided.
     */
    private static String getInput(String statement) {
        Scanner input = new Scanner(System.in);
        System.out.println(statement);
        return input.nextLine();
    }
}
