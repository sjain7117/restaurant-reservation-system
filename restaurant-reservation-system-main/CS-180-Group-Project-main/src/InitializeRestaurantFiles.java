import java.io.*;

/**
 *This class initializes the RestaurantDatabase files for easy testing if needed.
 *
 * @version Nov 4, 2025
 */

public class InitializeRestaurantFiles {

    private static final String[] DAYS = {"monday.txt", "tuesday.txt", "wednesday.txt",
        "thursday.txt", "friday.txt", "saturday.txt", "sunday.txt"};

    //This method initializes the RestaurantDatabase files
    public static void initializeFiles() {
        for (int i = 0; i < DAYS.length; i++) {
            File f = new File(DAYS[i]);
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(f))) {
                for (int a = 11; a <= 14; a++) {
                    for (int j = 1; j < 4; j++) {
                        //whoBooked,tableNum,tableSize,partySize,isSpecial,time,isBooked,creditCard,cost
                        String table = String.format("N/A,%d,2,N/A,No,%d,No,Not Needed,0", j, a);
                        pw.println(table);
                    }

                    for (int j = 4; j < 8; j++) {
                        String table = String.format("N/A,%d,4,N/A,No,%d,No,Not Needed,0", j, a);
                        pw.println(table);
                    }

                    String finalTable = String.format("N/A,%d,8,N/A,Yes,%d,No,N/A,0", 8, a);
                    pw.println(finalTable);
                }

                for (int a = 17; a <= 21; a++) {
                    for (int j = 1; j < 4; j++) {
                        //whoBooked,tableNum,tableSize,partySize,isSpecial,time,isBooked,creditCard,cost
                        String table = String.format("N/A,%d,2,N/A,No,%d,No,Not Needed,0", j, a);
                        pw.println(table);
                    }

                    for (int j = 4; j < 8; j++) {
                        String table = String.format("N/A,%d,4,N/A,No,%d,No,Not Needed,0", j, a);
                        pw.println(table);
                    }

                    String finalTable = String.format("N/A,%d,8,N/A,Yes,%d,No,N/A,0", 8, a);
                    pw.println(finalTable);
                }

            } catch (Exception e) {
                return;
            }
        }
    }
}
