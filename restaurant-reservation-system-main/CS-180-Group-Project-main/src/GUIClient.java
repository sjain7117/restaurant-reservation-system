import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class is the GUI version of the client class for phase 3. Users input on the GUI which interacts with the
 * server and do actions like create, login, and delete accounts, make reservations or cancel reservations. When a
 * user makes a reservation they are able to pick their table and are only able to make one reservation per day.
 * There is also a management feature where an Admin can sign in and change the hours of operation and seating
 * arrangements.
 *
 * @version December 3, 2025
 *
 */
public class GUIClient extends Frame implements GUIClientInterface {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private CardLayout cardLayout;
    private Panel accountManagement;
    private Panel reservationMenu;
    private Panel createReservationPanel;
    private Panel cancelReservationPanel;
    private Panel tableSelectionPanel;
    private Panel adminPanel;
    private Panel thankYouPanel;

    private TextField userField;
    private TextField passField;
    private String pendingAction = "";
    private String user;
    private String pass;
    private String day;
    private String time;
    private int partySize;
    private int tableNum;
    private String creditCard = "";
    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    /**
     * Connects the creation GUI logic of starting user Socket and displaying the GUI.
     */
    public GUIClient() {
        connectToServer();
        setUpGUI();
    }

    /**
     * Connects the user to the server through a Socket, BufferedReader, and PrintWriter.
     */
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 4242);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Couldn't connect to server");
        }

    }

    /**
     * Sets up the main structure of the GUI implementation.
     */
    private void setUpGUI() {
        setTitle("Italian CS Reservation Management System");
        setSize(400, 400);
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        accountManagement = new Panel(cardLayout);

        accountManagement.add("menu", createMenu());
        accountManagement.add("userPass", userPassScreen());

        add(accountManagement, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                dispose();
            }
        });

        setVisible(true);

    }

    /**
     * Creates the menu screen where a user can choose to create, login, or delete their account.
     */
    private Panel createMenu() {
        Panel menu = new Panel(new GridLayout(3, 1, 10, 10));

        Button login = new Button("Login");
        Button create = new Button("Create Account");
        Button delete = new Button("Delete Account");

        login.addActionListener(l -> showUserPass("LOGIN"));

        create.addActionListener(c -> showUserPass("CREATE"));

        delete.addActionListener(d -> showUserPass("DELETE"));

        menu.add(create);
        menu.add(login);
        menu.add(delete);

        return menu;
    }

    /**
     * Displays the screen where the User enters their username and password.
     * This screen is used by the login, create, and delete account logic.
     */
    private void showUserPass(String action) {
        pendingAction = action;
        userField.setText("");
        passField.setText("");
        cardLayout.show(accountManagement, "userPass");
    }

    /**
     * Creates the screen where the User enters their username and password
     */
    private Panel userPassScreen() {
        Panel userPass = new Panel(new GridLayout(3, 2, 10, 10));

        Label userName = new Label("    Username:");
        Label passLabel = new Label("    Password:");

        userField = new TextField();
        passField = new TextField();
        passField.setEchoChar('*');

        Button cancel = new Button("Cancel");
        cancel.addActionListener(e -> cardLayout.show(accountManagement, "menu"));

        Button submit = new Button("Submit");
        submit.addActionListener(e -> handleUserPassSubmit());

        userPass.add(userName);
        userPass.add(userField);
        userPass.add(passLabel);
        userPass.add(passField);

        userPass.add(cancel);
        userPass.add(submit);

        return userPass;
    }

    /**
     * Gets user input and handles the split of logic between the create, login, and delete options.
     */
    private void handleUserPassSubmit() {
        user = userField.getText().trim();
        pass = passField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showMessage("   Please fill all the fields");
            return;
        }

        switch (pendingAction) {
            case "LOGIN":
                loginAccount();
                break;
            case "CREATE":
                createAccount();
                break;
            case "DELETE":
                deleteAccount();
                return;
        }

    }

    /**
     * Writes the deleting account command to the server along with the username
     * and password. The server responds with Success or Failure and a screen is
     * shown based on the outcome.
     */
    private void deleteAccount() {
        out.println("Deleting account");
        out.println(user);
        out.println(pass);
        out.flush();

        try {
            String response = in.readLine();
            if (response.equals("Success")) {
                showThankYouPanel(" Successfully deleted account. Have a nice day!");
            } else {
                showThankYouPanel("Invalid Account");
            }

        } catch (IOException e) {
            showThankYouPanel(" There was an error deleting your account. Taking you back to the main menu");
        }

    }

    /**
     * Writes the login command to the server along with the username and password.
     * The server responds with Admin Handoff, if an admin has logged in, Success or Failure,
     * and a screen is displayed based on the outcome.
     */
    private void loginAccount() {
        out.println("Logging in");
        out.println(user);
        out.println(pass);
        out.flush();

        try {
            String response = in.readLine();
            if (response.equals("Admin HandOff")) {
                showAdminScreen();
            } else if (response.equals("Success")) {
                showMessage("   Successfully logged in. Taking you to the reservation page.");
                showReservationMenu();
            } else {
                showThankYouPanel("Username and/or Password are invalid. Taking you back to main menu.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Writes the create account command to the server. The server responds Success
     * or Failure and a screen is shown based on the outcome.
     */
    private void createAccount() {
        out.println("Making account");
        out.println(user);
        out.println(pass);
        out.flush();

        try {
            String response = in.readLine();
            if (response.equals("Success")) {
                showMessage("   Successfully created account. " +
                        "\n Taking you to the reservation page.");

                showReservationMenu();

            } else {
                showThankYouPanel("Account could not be created.");
            }
        } catch (IOException er) {
            er.printStackTrace();
        }

    }

    /**
     * Creates the reservation menu where the user can choose to either create or
     * delete a reservation.
     */
    private void initializeReservationMenu() {
        if (reservationMenu != null) return;

        reservationMenu = new Panel(new BorderLayout(10, 10));

        String userCap = user.substring(0, 1).toUpperCase() + user.substring(1);
        String message = "  Welcome, " + userCap + " please choose if you would like to create " +
            "or delete a reservation.";

        TextArea welcome = new TextArea(message, 5, 40, TextArea.SCROLLBARS_NONE );
        welcome.setEditable(false);
        welcome.setBackground(getBackground());
        reservationMenu.add(welcome, BorderLayout.NORTH);

        Panel buttonPanel = new Panel(new FlowLayout());
        Button create = new Button("Create a Reservation");
        Button cancel = new Button("Cancel a Reservation");

        create.addActionListener(e -> showCreateReservationScreen());
        cancel.addActionListener(e -> showCancelReservationScreen());

        buttonPanel.add(create);
        buttonPanel.add(cancel);

        reservationMenu.add(buttonPanel, BorderLayout.CENTER);

        accountManagement.add(reservationMenu, "Reservation Menu");

    }

    /**
     * Displays the reservation menu where the user can choose to create or delete a reservation.
     */
    private void showReservationMenu() {
        initializeReservationMenu();
        cardLayout.show(accountManagement, "Reservation Menu");
    }

    /**
     * Creates the create reservation screen that accepts user input of party size, day, time
     * then a table choice screen is displayed. Party size cannot be less than 1 or more than
     * 8 for this restaurant.
     */
    private void initializeCreateReservationScreen() {
        if (createReservationPanel != null) return;

        createReservationPanel = new Panel(new BorderLayout(10, 10));

        Label header = new Label("Create a Reservation!");
        String message = " We accept party sizes up to 4 for our regular tables, " +
            "and party sizes from 4 to 8 for our event room. Renting the event room costs $80 for one hour.";
        TextArea tableSize = new TextArea(message, 5, 40, TextArea.SCROLLBARS_NONE );
        tableSize.setEditable(false);
        tableSize.setBackground(getBackground());

        Panel northPanel = new Panel(new GridLayout(2, 1));
        northPanel.add(header);
        northPanel.add(tableSize);
        createReservationPanel.add(northPanel, BorderLayout.NORTH);

        Panel formPanel = new Panel(new GridLayout(4, 2, 5, 5));

        Label dayLabel = new Label("Day:");
        Choice dayChoice = new Choice();
        for (String d: days) {
            dayChoice.add(d);
        }

        Label timeLabel = new Label("Time:");
        Choice timeChoice = new Choice();

        Label partyLabel = new Label("Party Size:");
        TextField partyField = new TextField();

        formPanel.add(dayLabel);
        formPanel.add(dayChoice);
        formPanel.add(timeLabel);
        formPanel.add(timeChoice);
        formPanel.add(partyLabel);
        formPanel.add(partyField);

        createReservationPanel.add(formPanel, BorderLayout.CENTER);

        dayChoice.addItemListener(e -> {
            String selectedDay = dayChoice.getSelectedItem();
            timeChoice.removeAll();
            timeChoice.add("11:00 AM");
            timeChoice.add("12:00 PM");
            timeChoice.add("1:00 PM (13)");
            timeChoice.add("2:00 PM (14)");
            timeChoice.add("4:00 PM (16)");
            timeChoice.add("5:00 PM (17)");
            timeChoice.add("6:00 PM (18)");
            timeChoice.add("7:00 PM (19)");
            timeChoice.add("8:00 PM (20)");
            if (isHourNineAva(selectedDay)) {
                timeChoice.add("9:00 PM (21)");
            }

        });

        Panel buttonPanel = new Panel(new FlowLayout());
        Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(e -> cardLayout.show(accountManagement, "Reservation Menu"));

        Button createButton = new Button("Pick the Table");
        createButton.addActionListener(e -> {
            time = timeChoice.getSelectedItem();
            String party = partyField.getText().trim();
            String dayUpper = dayChoice.getSelectedItem();

            if (party.isEmpty() || dayUpper == null || time == null) {
                showMessage("   Please enter valid entries.");
                return;
            }

            if (!party.matches("\\d+")) {
                showMessage("   Please enter only letters for party size.");
                partyField.setText("");
                return;
            }

            int size = Integer.parseInt(party);

            if (size > 8 || size < 1) {
                showMessage("   Please enter a valid party size.");
                partyField.setText("");
                return;
            }

            day = dayUpper.substring(0, 1).toLowerCase() + dayUpper.substring(1);

            if (time.contains("(")) {
                int start = time.indexOf("(");
                int end = time.indexOf(")");
                time = time.substring(start + 1, end);
            } else {
                String[] parts = time.split(":");
                time = parts[0];
            }

            partySize = size;
            showTableSelection();

        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        createReservationPanel.add(buttonPanel, BorderLayout.SOUTH);
        accountManagement.add(createReservationPanel, "Create Reservation");
    }

    /**
     * Displays all the valid tables for requested day and time by writing the command out
     * to the server along with the day and time. The server responds a string that contains
     * the table numbers available for the user if there are none a screen pops up saying that.
     * If the party room is already or unable to be booked a screen is displayed for that.
     */
    private void showTableSelection() {
        out.println("Getting All Available Tables");
        out.println(day);
        out.println(time);
        out.flush();
        ArrayList<String> tablesSearch = new ArrayList<>();

        try {
            String response = in.readLine();
            String[] parts = response.split(";");

            for (int i = 0; i < parts.length; i++) {
                String[] extracted = parts[i].split(",");
                tablesSearch.add(extracted[1]);
            }

            String[] tableNumbers = tablesSearch.toArray(new String[tablesSearch.size()]);

            List<Integer> usableTables = filterTables(tableNumbers);

            if (partySize > 4 && !isHourNineAva(day)) {
                showMessage("   The party room is not available for this day. " +
                    "Please try booking a different day.");
                return;
            } else if (partySize > 4 && usableTables.isEmpty()) {
                showMessage("   The party room is not available for this day. " +
                    "Please try booking a different day.");
                return;
            } else if (usableTables.isEmpty()) {
                showMessage("   No available tables for this time and party size.");
                return;
            }

            displayTableSelection(usableTables);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *Gets the table numbers that are acceptable for the party size
     *@param tableNumbers an array of table numbers that are not booked
     */
    private List<Integer> filterTables(String[] tableNumbers) {
        List<Integer> result = new ArrayList<>();

        for (String t : tableNumbers) {
            if (t.isEmpty()) continue;
            tableNum = Integer.parseInt(t);

            if (partySize <= 2 && tableNum >= 1 && tableNum <= 3) {
                result.add(tableNum);
            } else if (partySize <= 4 && partySize > 2 && tableNum >= 4 && tableNum <= 7) {
                result.add(tableNum);
            } else if (partySize > 4 && tableNum == 8 && isHourNineAva(day)) {
                result.add(tableNum);
            }
        }

        return result;
    }

    /**
     * If the user is booking the party room then the user is prompted to enter their
     * credit card number for payment. Displays the tables so the user can click on one
     * to decide where they will sit.
     * @param tables a list of tables to be displayed for choosing
     */
    private void displayTableSelection(List<Integer> tables) {
        Panel tablePanel = new Panel(new GridLayout(2, 4, 10, 10));

        for (int table : tables) {
            tableNum = table;
            Button tableButton = new Button("Table " + tableNum);
            tableButton.setBackground(Color.LIGHT_GRAY);

            tableButton.addActionListener(e -> {
                if (partySize > 4) {
                    showCreditCardScreen();
                } else {
                    creditCard = "0";
                    completeReservation();
                }

            });

            tablePanel.add(tableButton);

            tableSelectionPanel = new Panel(new BorderLayout());
            tableSelectionPanel.add(new Label("        Select a Table:"), BorderLayout.NORTH);
            tableSelectionPanel.add(tablePanel, BorderLayout.CENTER);

            accountManagement.add(tableSelectionPanel, "Table Selection");
            cardLayout.show(accountManagement, "Table Selection");
        }
    }

    /**
     * Displays the credit card information input screen if the user is booking the party room.
     */
    private void showCreditCardScreen() {
        creditCard = "";
        initializeCreditCardScreen();
        cardLayout.show(accountManagement, "Credit Card Number");
    }

    /*
    Creates the credit card information input screen if the user is booking the party room.
     */
    private void initializeCreditCardScreen() {
        Panel creditCardPanel = new Panel(new GridLayout(3, 1));

        Panel topPanel = new Panel(new GridLayout(2, 1));
        Label chargePrice = new Label("Party Room Reservation Price: $80");
        Label creditCardLabel = new Label("Please enter your credit card number:");
        topPanel.add(chargePrice);
        topPanel.add(creditCardLabel);

        Panel inputPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        TextField creditCardNumber = new TextField(18);

        creditCardNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (creditCardNumber.getText().length() >= 16) {
                    e.consume();
                }
            }
        });

        inputPanel.add(creditCardNumber);


        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        Button cancelButton = new Button("Close");
        cancelButton.addActionListener(e -> {
            showMessage("   Are you sure you want to stop making the reservation?");
            cardLayout.show(accountManagement, "Reservation Menu");
        });

        Button submit = new Button("Submit");
        submit.addActionListener(e -> {
            String creditCardRaw = creditCardNumber.getText();
            String digits = creditCardRaw.replaceAll("([^0-9])", "");

            if (digits.length() != 16) {
                showMessage("Invalid Credit Card Number");
                return;
            }

            creditCard = digits;
            completeReservation();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submit);

        creditCardPanel.add(topPanel);
        creditCardPanel.add(inputPanel);
        creditCardPanel.add(buttonPanel);

        accountManagement.add(creditCardPanel, "Credit Card Number");
    }

    /**
     * Completes the reservation by writing the command to the server along with the
     * username, day, table number, party size, time, and credit card number if gathered
     * from the user if not a 0 in its place. The server responds Reservation made,
     * reservation failed, or user already has a reservation for this day
     */
    private void completeReservation() {
        try {
            out.println("Making Reservation");
            out.println(user);
            out.println(day);
            out.println(tableNum);
            out.println(partySize);
            out.println(time);
            if (partySize < 5) {
                creditCard = "0";
            }
            out.println(creditCard);
            out.flush();

            String response = in.readLine();

            if (response.equals("Reservation Made")) {
                showThankYouPanel(" Thank you for your Reservation! See you soon.");
            } else if (response.equals("User Already Has Reservation For This Day")) {
                showThankYouPanel(" You already have a reservation");
            } else {
                showThankYouPanel(" Reservation Failed. Taking you back to the Login screen.");
            }

            cardLayout.show(accountManagement, "Thank you");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows an exit screen to the user or admin before disconnecting or being shown a previous card again.
     * @param message the message to be displayed on the screen
     */
    private void showThankYouPanel(String message) {
        if (thankYouPanel != null) {
            accountManagement.remove(thankYouPanel);
        }
        initializeThankYouPanel(message);
    }

    private void initializeThankYouPanel(String message) {
        thankYouPanel = new Panel(new BorderLayout());
        String dayUpper = "";

        if (day != null) {
            dayUpper = day.substring(0, 1).toUpperCase() + day.substring(1);
        }

        if (message.equals("Close Late")) {
            message = " Closing time is 9:00 for " + dayUpper + ". " +
                "Ending the admin session, and taking you back to the Login screen.";
            endAdminSession();
        } else if (message.equals("Error in changing the closing time. Please try again.")) {
            cardLayout.show(accountManagement, "Admin Screen");
        } else if (message.equals("Close Early")) {
            message = " Closing time changed to 8:00 for " + dayUpper + ". " +
                "Taking you back to the Login screen.";
            endAdminSession();
        } else if (message.equals(" Reservation Failed. Taking you back to the Login screen")) {
            cardLayout.show(accountManagement, "Login Screen");
        } else if (message.equals("Invalid Account")) {
            message = " Username and/or Password are invalid. Taking you back to main menu.";
            cardLayout.show(accountManagement, "Login Screen");
        } else if (message.equals("Account could not be created.")) {
            message = " Account could not be created. Username and/or password may already be taken. " +
                "Taking you back to the Login screen.";
            cardLayout.show(accountManagement, "Login Screen");
        } else if (message.equals(" There was an error deleting your account. Taking you back to the main menu")) {
            cardLayout.show(accountManagement, "Login Screen");
        } else if (message.equals("Cancel Error")) {
            message = " Your reservation cannot be canceled. There may not be a" +
                    "     reservation for you on " + day + ".";
            cardLayout.show(accountManagement, "Login Screen");
        } else if (message.equals("You already have a reservation")) {
            message = "You already have a reservation for this day. " +
                    "Taking you back to the Login screen.";
            cardLayout.show(accountManagement, "Login Screen");
        }

        TextArea textArea = new TextArea(message, 5, 40, TextArea.SCROLLBARS_NONE);
        textArea.setEditable(false);
        textArea.setBackground(getBackground());
        textArea.setFocusable(false);

        Button ok = new Button("Ok");
        ok.addActionListener(e -> endUserSession());

        thankYouPanel.add(textArea, BorderLayout.CENTER);
        thankYouPanel.add(ok, BorderLayout.SOUTH);

        accountManagement.add(thankYouPanel, "Thank you");
        cardLayout.show(accountManagement, "Thank you");
    }

    /*
    Creates the cancel reservation screen.
    */
    private void initializeCancelReservationScreen() {
        if (cancelReservationPanel != null) return;
        cancelReservationPanel = new Panel(new BorderLayout(10, 10));
        Label header = new Label("Cancel a Reservation");

        Choice dayChoice = new Choice();
        for (String d : days) {
            dayChoice.add(d);
        }

        Panel buttonPanel = new Panel(new FlowLayout());

        Button submit = new Button("Submit");
        submit.addActionListener(e -> {
            out.println("Canceling Reservation");
            out.println(user);

            String capDay = dayChoice.getSelectedItem();
            day = capDay.substring(0, 1).toLowerCase() + capDay.substring(1);
            out.println(day);

            try {
                String response = in.readLine();
                if (response.equals("Success")) {
                    showThankYouPanel(" Reservation canceled. Have a nice day!");
                } else {
                    showThankYouPanel("Cancel Error");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(r -> cardLayout.show(accountManagement, "Reservation Menu"));

        cancelReservationPanel.add(header, BorderLayout.NORTH);
        cancelReservationPanel.add(dayChoice, BorderLayout.CENTER);
        buttonPanel.add(cancelButton);
        buttonPanel.add(submit);
        cancelReservationPanel.add(buttonPanel, BorderLayout.SOUTH);

        accountManagement.add(cancelReservationPanel, "Cancel Reservation");
    }

    /*
    Shows messages on a pop-up screen with an ok button
    */
    private void showMessage(String message) {
        Dialog d = new Dialog(this, "Message", true);
        d.setSize(400, 400);
        d.setLayout(new BorderLayout());
        TextArea text = new TextArea(message, 6, 30, TextArea.SCROLLBARS_NONE);
        text.setEditable(false);
        text.setBackground(getBackground());
        Button ok = new Button("OK");
        ok.addActionListener(e -> d.dispose());
        d.add(text, BorderLayout.CENTER);
        d.add(ok, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    /**
     * Displays the cancel reservation screen.
     */
    private void showCancelReservationScreen() {
        initializeCancelReservationScreen();
        cardLayout.show(accountManagement, "Cancel Reservation");
    }

    /**
     * Displays the create reservation screen.
     */
    private void showCreateReservationScreen() {
        initializeCreateReservationScreen();
        cardLayout.show(accountManagement, "Create Reservation");
    }

    /*
    Sends the day and 21hour(9:00) to the server to see if 9:00 is available for booking or not.
     */
    private boolean isHourNineAva(String selectedDay) {
        String daySend = selectedDay.substring(0, 1).toLowerCase() + selectedDay.substring(1);
        try {
            out.println("Getting All Available Tables");
            out.println(daySend);
            out.println(21);
            out.flush();

            String response = in.readLine();

            return response != null && !response.trim().isEmpty();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Creates the admin control screen with the choices to close early (8) or close late/normal (9).
     */
    private void initializeAdminScreen() {
        if (adminPanel != null) return;

        adminPanel = new Panel(new GridLayout(4, 1, 10, 10));

        Label header = new Label("Admin Controls");

        Choice dayChoice = new Choice();
        for (String d : days) {
            dayChoice.add(d);
        }

        Choice actionChoice = new Choice();
        actionChoice.add("Close Early (8:00)");
        actionChoice.add("Close at normal time (9:00)");

        Button submit = new Button("Submit");
        submit.addActionListener(e -> {
            day = dayChoice.getSelectedItem();
            day = day.substring(0, 1).toLowerCase() + day.substring(1);
            String command = actionChoice.getSelectedItem();

            if (command.equals("Close Early (8:00)")) {
                sendAdminCommand("Close Early");

            } else {
                sendAdminCommand("Close Late");
            }
        });

        adminPanel.add(header);
        adminPanel.add(dayChoice);
        adminPanel.add(actionChoice);
        adminPanel.add(submit);

        accountManagement.add(adminPanel, "Admin Screen");

    }

    /**
     * Displays the admin control screen.
     */
    private void showAdminScreen() {
        initializeAdminScreen();
        cardLayout.show(accountManagement, "Admin Screen");
    }

    /**
     * Sends the command to the server, and the server responds with Success or Failed.
     * A screen is shown to reflect what the outcome of the change was.
     * @param command what the admin is choosing, to close early or late
     */
    private void sendAdminCommand(String command) {
        try {
            out.println(day);
            out.println(command);
            out.flush();

            String response = in.readLine();

            if (response.equals("Success")) {
                if (command.equals("Close Early")) {
                    showThankYouPanel("Close Early");
                } else {
                    showThankYouPanel("Close Late");
                }
            } else  {
                showThankYouPanel("Error in changing the closing time. Please try again.");
            }

        } catch (Exception ex) {
            showMessage("Admin session ended.");
            endAdminSession();
        }

    }

    /**
     * Stops the admin session by closing the socket and re-connects as a normal user.
     */
    private void endAdminSession() {
        try {
            socket.close();
            connectToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cardLayout.show(accountManagement, "menu");
    }

    /**
     * Stops the user session by closing the socket and reconnects to the server.
     */
    private void endUserSession() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            connectToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cardLayout.show(accountManagement, "menu");
    }

    /**
     * Creates a new instance of GUIClient which sets up the graphical user interface and connects to the server.
     */
    public static void main(String[] args) {
        new GUIClient();
    }

}
