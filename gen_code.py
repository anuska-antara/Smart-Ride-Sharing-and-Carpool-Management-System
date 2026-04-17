import os

files = {
    'src/main/resources/application.properties': '''spring.application.name=carpool-backend
spring.datasource.url=jdbc:h2:mem:carpooldb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
''',
    
    'src/main/java/com/carpool/CarpoolApplication.java': '''package com.carpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarpoolApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarpoolApplication.class, args);
    }
}
''',

    'src/main/java/com/carpool/model/User.java': '''package com.carpool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String role; // DRIVER, PASSENGER
    private String email;
}
''',
    'src/main/java/com/carpool/model/Ride.java': '''package com.carpool.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User driver;
    
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    
    private int totalSeats;
    private int availableSeats;
    private double baseFare;
    
    private String status; // PUBLISHED, IN_PROGRESS, COMPLETED, CANCELLED
}
''',
    'src/main/java/com/carpool/model/Booking.java': '''package com.carpool.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Ride ride;
    
    @ManyToOne
    private User passenger;
    
    private int requestedSeats;
    private double finalFare;
    private String status; // CONFIRMED, CANCELED
    private LocalDateTime bookingTime;
}
''',
    'src/main/java/com/carpool/repository/RideRepository.java': '''package com.carpool.repository;
import com.carpool.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findBySourceAndDestinationAndStatusAndAvailableSeatsGreaterThanEqual(
        String source, String destination, String status, int seats);
}
''',
    'src/main/java/com/carpool/repository/UserRepository.java': '''package com.carpool.repository;
import com.carpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
''',
    'src/main/java/com/carpool/repository/BookingRepository.java': '''package com.carpool.repository;
import com.carpool.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
''',
    'src/main/java/com/carpool/service/RideService.java': '''package com.carpool.service;

import com.carpool.model.*;
import com.carpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    // UC1: Create & Publish Ride
    @Transactional
    public Ride createAndPublishRide(Long driverId, String source, String destination, LocalDateTime time, int seats, double fare) {
        User driver = userRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Driver not found"));
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setSource(source);
        ride.setDestination(destination);
        ride.setDepartureTime(time);
        ride.setTotalSeats(seats);
        ride.setAvailableSeats(seats);
        ride.setBaseFare(fare);
        ride.setStatus("PUBLISHED");
        return rideRepository.save(ride);
    }
    
    // UC2: Search & Match Ride
    public List<Ride> searchRides(String src, String dest, int requestedSeats) {
        return rideRepository.findBySourceAndDestinationAndStatusAndAvailableSeatsGreaterThanEqual(
                src, dest, "PUBLISHED", requestedSeats);
    }
    
    // UC3: Book Seat or Seat Allocation
    @Transactional
    public Booking bookSeat(Long rideId, Long passengerId, int requestedSeats) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));
        User passenger = userRepository.findById(passengerId).orElseThrow(() -> new RuntimeException("Passenger not found"));
        
        if (ride.getAvailableSeats() < requestedSeats) {
            throw new RuntimeException("Not enough seats available.");
        }
        
        // UC4 Fare Calculation included here
        double finalFare = calculateFare(ride.getBaseFare(), requestedSeats);
        
        ride.setAvailableSeats(ride.getAvailableSeats() - requestedSeats);
        rideRepository.save(ride);
        
        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassenger(passenger);
        booking.setRequestedSeats(requestedSeats);
        booking.setFinalFare(finalFare);
        booking.setStatus("CONFIRMED");
        booking.setBookingTime(LocalDateTime.now());
        
        return bookingRepository.save(booking);
    }
    
    // UC4: Fare Calculation (extracted module)
    private double calculateFare(double baseFare, int seats) {
        return baseFare * seats; // Can be upgraded to more complex strategy
    }
    
    // UC5: Complete Ride & Process Payment
    @Transactional
    public Ride completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));
        ride.setStatus("COMPLETED");
        // Integration with external Gateway would go here
        System.out.println("Payment processed & driver credited.");
        return rideRepository.save(ride);
    }
    
    // UC6: Cancel Booking
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELED");
        
        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getRequestedSeats());
        rideRepository.save(ride);
        
        return bookingRepository.save(booking);
    }
}
''',
    'src/main/java/com/carpool/controller/RideController.java': '''package com.carpool.controller;

import com.carpool.model.*;
import com.carpool.service.RideService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    
    private final RideService rideService;
    
    // UC1
    @PostMapping("/publish")
    public Ride publishRide(@RequestParam Long driverId, @RequestParam String src, @RequestParam String dest,
                            @RequestParam String timeStr, @RequestParam int seats, @RequestParam double fare) {
        LocalDateTime time = LocalDateTime.parse(timeStr);
        return rideService.createAndPublishRide(driverId, src, dest, time, seats, fare);
    }
    
    // UC2
    @GetMapping("/search")
    public List<Ride> searchRides(@RequestParam String src, @RequestParam String dest, @RequestParam int seats) {
        return rideService.searchRides(src, dest, seats);
    }
    
    // UC3 & UC4 & UC9 (Notification system mocked locally)
    @PostMapping("/{rideId}/book")
    public Booking bookRide(@PathVariable Long rideId, @RequestParam Long passengerId, @RequestParam int seats) {
        return rideService.bookSeat(rideId, passengerId, seats);
    }
    
    // UC5
    @PostMapping("/{rideId}/complete")
    public Ride completeRide(@PathVariable Long rideId) {
        return rideService.completeRide(rideId);
    }
    
    // UC6
    @PostMapping("/booking/{bookingId}/cancel")
    public Booking cancelBooking(@PathVariable Long bookingId) {
        return rideService.cancelBooking(bookingId);
    }
}
'''
}

for filepath, content in files.items():
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'w') as f:
        f.write(content)
