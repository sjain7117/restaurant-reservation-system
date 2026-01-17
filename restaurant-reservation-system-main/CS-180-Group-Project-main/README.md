# Restaurant Reservation System

# Phase 3
To run the project first start the RestaurantServer by going to the RestaurantDriver class and clicking the green run button.
Next go to GUIClient class and run that class too by clicking the green run button.

In order to run multiple GUI clients at once to test the concurrency, go to the top of IntelliJ in the right hand area
where the green run button and debug button and are hit the arrow to show info about configurations. Click edit
configurations and select GUIClient. Under modify options select multiple instances and hit apply. Now when you hit the
green arrow to run GUIClient.java it will make a new instance for you to use.



## Class Functionalities:

#### AdminHandler.java
The AdminHandler class manages connections to the restaurant reservation server
for anybody that logs in as an Admin. It implements Runnable so that threading can be used
to support multiple clients. AdminHandler's run method takes in a given day and command to close late or close early
and uses the RestaurantDatabase to process this action. Finally, it writes back the results
of the action and ends.

#### Testing done on AdminHandler
This class contains JUnit tests that check whether the AdminHandler correctly processes administrative commands sent by an authenticated admin user. The tests verify that the handler reads the requested day and command properly, calls the adminChange method in the RestaurantDatabase with the correct parameters for closing later or closing earlier, and sends the appropriate success or failure response back to the client. The tests also ensure that non-“Close Late” commands are interpreted correctly, that all failure messages returned by the database result in a failure response to the client, that the socket is properly closed after handling a request, and that the handler gracefully manages unexpected input or internal exceptions without crashing.

#### AdminHandlerInterface.java
Empty interface for AdminHandler

#### GUIClient.java
This class is the graphical user interface that the user will be seeing to make their reservation. First, there are three options for account management. Create, login, or delete an account. The user will be then prompted to enter a username and password. If the user selected create then an account will be created with the information inputted, unless their already exists one. If the user selects delete they will have to input their information, and as long as it corresponds to an account the account will be deleted.
If the user selects login then the user will be logged in unless the information inputted does not correspond to an account's information. Next, the user will be prompted if they would like to create or delete a reservation. If they choose delete they will be prompted to enter the day they would like to delete their reservation for, as long as the user has a reservation for that day, the reservation will be deleted. If the user selects create the user will be prompted to enter a day and time within the next 7 days. A pick a table screen will pop-up and the user will select their desired table and then as long as all the information is correct the reservation will be made. The user would then be taken to a Thank you screen and the session would then end. Their is also a mangement feature when an admin can sign in and they will be shown a screen where they can decide between closing at 8 (early) or closing at 9 (late/normal) and the day to do so on. If closing at 8, the party room will not be bookable for that day. The admin will submit, and as long as everything went smoothly the closing time will be noted, and the admin session will end and reconnect as a normal user.

#### GUIClientInterface.java
Empty interface for GUIClient

# Phase 2


## Class Functionalities:

#### RestaurantDriver.java
This class first calls class InitializeRestaurantFiles static method initializeFiles
to initialize the RestaurantDatabase files. Then it instantiates instances
of RestaurantDatabase class and UserDatabase class. Finally, a new thread for
the server is created and started so the server can run.

#### RestaurantServer.java
This class implements the Runnable interface and the constructor takes
in a port number, RestaurantDatabase object, and UserDatabase object in
order to create a new ServerSocket and set access to the database objects. 
In the run method, there is a while loop that loops infinitely and constantly
checks for connections with clients. When a connection is found, then a 
new thread is created for ClientHandler to handle inputs from the client.

#### Client.java
This class has one main method to simulate the client who will want to
log in/make/or delete an account and make/cancel a reservation. There is also
a helper method getInput which takes in a string and gets input from the user.
The main method for Client.java does not do error handling since this will be
replaced by a GUI later in phase 3.

#### ClientHandler.java
The ClientHandler class manages individual client connections for a restaurant reservation server. It implements Runnable to run each client session in a separate thread, allowing the server to handle multiple users simultaneously. The class connects clients to two databases: UserDatabase for login and account operations, and RestaurantDatabase for reservation management. When a client connects, the handler opens input and output streams and enters a loop that reads commands and routes them to the appropriate methods. It supports six operations: login, account creation, account deletion, making reservations, canceling reservations, and retrieving available tables. The handler sends back simple "Success" or "Failed" responses for most operations, while table queries return a semicolon-separated list of available tables. Special handling is included for table 8 (marked as a special table), and the closeConnection method ensures all streams and sockets are properly closed when the session ends.

#### Testing done on RestaurantServer
The RestaurantServerTest class is a JUnit test suite 
for the RestaurantServer constructor. The 
RestaurantServer class creates a ServerSocket and 
manages client connections using ClientHandler. 
These tests focus on verifying that the constructor 
correctly initializes the server, stores references 
to the RestaurantDatabase and UserDatabase, and 
handles invalid or edge-case input without crashing.
The suite includes cases for valid ports, invalid 
ports, and ports already in use, and ensures the 
constructor stores database objects correctly, even
when null references are passed. Reflection is used
to access private fields like serverSocket, rd, and
ud to assert the internal state without changing the
production code. These tests confirm that the server
object is properly constructed and ready for use, 
handling edge cases gracefully.

#### Testing done on Client
The ClientTest class is a JUnit test suite for the
private static getInput method in the Client class.
Client uses getInput to prompt the user for input via
the terminal. Since the method is private, reflection
is used to access it without modifying the source
code. The tests simulate user input by redirecting 
System.in and capture console output to verify the
prompt is printed correctly. Test cases ensure the
method returns the correct string for normal, empty,
and numeric input, and works for multiple consecutive
calls without exceptions like NoSuchElementException.
Testing this helper in isolation confirms that Client
can reliably read user input for login, account 
management, and reservations, providing confidence 
that the input logic will remain correct during future
refactoring or GUI integration.

#### Testing done on ClientHandler
This class contains JUnit tests that check whether the ClientHandler can be correctly constructed under different conditions, including when given a null socket, a valid socket, or no socket at all, and also verifies that login behavior functions correctly for both admin and non-admin users by ensuring that admin login triggers the proper handoff to an AdminHandler while normal users follow the standard connection flow.

#### I/O Manual Testing
Because the client application does not store any information locally, all operations depend on network I/O between the
client and the server. In order to test I/O functionalities follow the following steps. Start the RestaurantServer by 
going to the RestaurantDriver class and clicking the green run button. Next, go to the Client class and run that class 
too by clicking the green run button. You will be greeted by a welcome message, with options l, d, or m.

Type d in the terminal. You will be prompted to enter a username, enter "Quinn". You will be prompted to enter a password,
enter "pass". You will get an invalid message, and the main menu will be printed again.

Type l in the terminal. You will be prompted to enter a username, enter "Quinn". You will be prompted to enter a password, 
enter "pass". You will get an invalid message, and the main menu will be printed again. 

This time enter m in the terminal. You will be prompted to enter a username, enter "Quinn". You will be prompted to enter a password,
enter "pass". You will get a valid account creation message, and the reservation page will be printed. 

Type m in the terminal. Next, enter "monday" in the terminal. Next enter "11:00" in the terminal. A list of available tables will be printed
enter 1 in the terminal. Next, enter 2 in the terminal. You will get a reservation made message. 

Go to the Client class and click the green run button again. Enter l in the terminal, "Quinn", then "pass". You will 
get a login successful message. Enter c into the terminal then enter "sunday". You will get a reservation not found message.

Run the Client class again and enter l in the terminal, "Quinn", then "pass". You will get a login successful message. 
Enter c into the terminal. Enter "monday" then you will get a successfully canceled reservation message.

Run the Client class again and enter d in the terminal, "Quinn", then "pass". You will get a successful deletion 
account message.

In order to run multiple clients at once to test the concurrency, go to the top of IntelliJ in the right hand area
where the green run button and debug button and are hit the arrow to show info about configurations. Click edit 
configurations and select Client. Under modify options select multiple instances and hit apply. Now when you hit the 
green arrow to run Client.java it will make a new instance for you to use.

## Interface Functionalities:

#### RestaurantServerInterface.java
Empty interface for RestaurantServer
#### AdminHandlerInterface.java
Empty interface for AdminHandler
#### ClientHandlerInterface.java
Empty interface for ClientHandler
#### ClientInterface.java
Empty interface for Client
# Phase 1

Instructions on running and compiling: Assuming the program will be run in IntelliJ, 
make sure all the files from Vocareum are present. IntelliJ will automatically compile all the java files so you first can 
run the main method in RestaurantDriver class by pressing the green run button to initialize the restaurant database files. 
Go to RestaurantDatabaseTest.java and press the green run button to run the test cases and verify everything works. Then go to 
UserDatabaseTest.java and press the green run button to run the test cases and verify everything works. Then you are done.


## Class Functionalities:

##### InitializeRestaurantFiles.java:
This class initializes the restaurant database files with a static method 
initializeFiles(). No testing was done on this since we looked at the files, 
and they had all the information we wanted. The files made are monday.txt,
tuesday.txt, wednesday.txt, thursday.txt, friday.txt, saturday.txt, and sunday.txt
along with given information for tables and timings.

##### RestaurantDatabase:
This class runs the reservation system to allow users to make and cancel
reservations. The method makeReservation allows users to make a reservation
with given parameters of the username, day for reservation, table number, 
party size of user, the time for reservation, whether the user is booking
a special table or not, and a string for credit card number if booking a special table, 
otherwise it's just empty. The method cancelReservation allows the user to
cancel their reservation given parameters of the username and the day of the 
reservation. The method getAllAvailableTablesForDayAndTime gets all
available tables on the given day and time which can be reserved. The
method getLock is a private method to get the synchronization lock for the 
given day to allow users to update different days, but not the same. Finally, 
the method adminChange is for any user that logs in as an admin in order to
execute a given action of closing later or closing early given by the boolean 
parameter closingLater on a given day with parameter day.

##### Testing done on RestaurantDatabase:
This class contains JUnit tests that check whether the RestaurantDatabase class works correctly when users make or cancel a reservation. The tests verify that the parameters—username, reservation day, table number, party size, reservation time, whether the table is special, and the credit card number for special tables—are all validated properly when attempting to create or cancel a reservation. The tests also ensure that the day and time parameters are valid when retrieving all available tables for a given day and time. In addition, tests validate the behavior of administrative actions by checking that the adminChange method correctly processes both closing later and closing earlier operations, handles invalid days and failure responses appropriately, and updates or removes relevant table entries as expected.
##### UserDatabase:
The UserDatabase class is responsible for managing secure user authentication, including registration and deletion, with all user credentials stored in a plain text file in the format username, password. To ensure thread safety during concurrent file access, all critical file operations are explicitly declared as synchronized. Key public methods allow users to be managed: addUser(username, password) registers a new user only if the username and password haven't been previously claimed; deleteUser(username, password) removes a user after authenticating the credentials; and login(username, password) verifies a user's identity by checking the stored credentials. Additionally, getAllUsers()provides a list of all registered usernames, while the private readUsers() helper method handles the underlying file reading and parsing of the credential data.

##### Testing done on UserDatabase:
This class contains JUnit tests that check whether the UserDatabase class works 
correctly when adding, removing, and verifying users stored in a text file. The 
tests make sure the file is created if it does not already exist and that new users 
can only be added when both the username and password are provided, not empty, and 
not already used. The tests also check that logging in only works when the username
and password match, and that deleting a user only succeeds when both pieces of 
information are correct. It also verifies that the method for getting all usernames 
returns the correct list, both when the file is empty and when users have been added. 
The tests confirm that user data is saved permanently by checking that another
UserDatabase object can still read the information later. The class also tests that
any lines in the file that are not in the correct username,password format are 
ignored. Finally, the class includes a test to make sure that when several threads
try to add users at the same time, the program still works safely and does not create
errors or duplicates.

## Interface Functionalities:

##### RestaurantDatabaseInterface:
The RestaurantDatabaseInterface defines the main methods for making and canceling
restaurant reservations and checking available tables. Each reservation uses details
like username, day, table number, party size, and time. The interface also includes
a lock for each day of the week to ensure safe access when multiple users try to
update reservations at the same time.

##### UserDatabaseInterface:
The UserDatabaseInterface defines the main operations used to manage user accounts
in the system. It includes methods to add a new user, delete an existing user, log
in with a username and password, and retrieve a list of all registered users. The
interface also provides separate lock objects to ensure thread safety, allowing
multiple users to interact with the database without interfering with each other
during add, delete, login, or list operations.


