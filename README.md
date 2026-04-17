# Smart Ride-Sharing System (OOAD Mini Project)

A carpool and ride-sharing application allowing drivers to publish rides and passengers to search, book, pay, and rate their trips.

## Features

- **Driver Portal**: 
  - Register as a driver.
  - Publish rides (source, destination, available seats, base fare).
  - Manage rides through statuses (e.g., ONGOING, COMPLETED).

- **Passenger Portal**:
  - Search available rides based on criteria.
  - Book seats.
  - **Payment Gateway Integration**: Simulated local Razorpay checkout terminal (Test Mode) handling secure ride payments on the frontend.
  - Leave ratings and reviews for completed rides.

## Tech Stack
- **Backend:** Java, Spring Boot 3.2.3, Maven
- **Database:** H2 Database (In-Memory)
- **Frontend:** Vanilla HTML, CSS, JavaScript

## Prerequisites
- Java (Recommended: JDK 21/25)
- Maven installed and added to PATH (or use the Maven Wrapper)

## How to Run

1. Open your terminal in the root directory of this project.
2. Build and start the Spring Boot application using Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Once the server starts (showing `Tomcat started on port(s): 8080`), open your web browser and navigate to:
   ```text
   http://localhost:8080
   ```
4. You will see the home/dashboard screen where you can navigate to the inner driver/passenger areas.

## Note on Database
This app is currently configured to use an H2 in-memory database. All data (users, rides, bookings) will be reset when the server restarts.
