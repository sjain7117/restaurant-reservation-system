# Restaurant Reservation System

This project is a team-based restaurant reservation system developed as part of a university computer science course. The goal of the project was to design and implement a realistic reservation platform using a client–server architecture that supports multiple users, persistent data storage, and administrative controls.

The system uses a multi-threaded server to handle concurrent client connections, allowing several users to create accounts, log in, make reservations, and cancel reservations at the same time. All user and reservation data is stored persistently using file-based storage, with separate files maintained for each day of the week to ensure data consistency and thread safety.

Users interact with the system through a graphical user interface (GUI) that guides them through account management and reservation workflows. Users can view table availability for specific days and times, select tables, and manage their reservations. The system also includes a command-line client used during earlier phases for testing and validating network input/output behavior.

Administrative functionality is supported through a dedicated admin handler that allows authorized users to manage restaurant operations. Admins can adjust closing times for specific days, which dynamically affects table availability and reservation rules, including special handling for designated tables.

The project was developed incrementally across multiple phases, with increasing complexity in architecture, networking, and user interaction. Extensive JUnit testing and manual I/O testing were performed to verify correctness, concurrency handling, and edge-case behavior across the server, client, and database components.

This repository represents a group project, and all core functionality, system design decisions, and testing efforts were completed collaboratively as part of the course requirements.
