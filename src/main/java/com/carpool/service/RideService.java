package com.carpool.service;

import com.carpool.model.*;
import com.carpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public RideService(RideRepository rideRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

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
        
        if ("COMPLETED".equals(ride.getStatus())) {
            throw new RuntimeException("Ride is already completed.");
        }
        
        ride.setStatus("COMPLETED");
        // Integration with external Gateway would go here
        System.out.println("Payment processed & driver credited.");
        return rideRepository.save(ride);
    }
    
    // UC6: Cancel Booking
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if ("CANCELED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already canceled.");
        }
        
        Ride ride = booking.getRide();
        if ("COMPLETED".equals(ride.getStatus())) {
            throw new RuntimeException("Cannot cancel booking for a completed ride.");
        }
        
        booking.setStatus("CANCELED");
        
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getRequestedSeats());
        rideRepository.save(ride);
        
        return bookingRepository.save(booking);
    }
    
    // UC7: View History (Driver)
    public List<Ride> getDriverHistory(Long driverId) {
        return rideRepository.findByDriverId(driverId);
    }

    // UC8: View History (Passenger)
    public List<Booking> getPassengerHistory(Long passengerId) {
        return bookingRepository.findByPassengerId(passengerId);
    }

    // UC9: Rate and Review (Passenger for Completed Rides)
    @Transactional
    public Booking rateDriver(Long bookingId, Integer rating, String review) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"COMPLETED".equals(booking.getRide().getStatus())) {
            throw new RuntimeException("You can only rate a completed ride.");
        }
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }
        
        booking.setRating(rating);
        booking.setReview(review);
        return bookingRepository.save(booking);
    }

    // Process Payment
    @Transactional
    public Booking processPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"COMPLETED".equals(booking.getRide().getStatus())) {
            throw new RuntimeException("You can only pay for a completed ride.");
        }
        booking.setPaid(true);
        return bookingRepository.save(booking);
    }
}
