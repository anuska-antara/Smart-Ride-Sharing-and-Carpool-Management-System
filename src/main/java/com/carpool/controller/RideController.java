package com.carpool.controller;

import com.carpool.model.*;
import com.carpool.service.RideService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rides")
public class RideController {
    
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }
    
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
    public ResponseEntity<?> completeRide(@PathVariable Long rideId) {
        try {
            return ResponseEntity.ok(rideService.completeRide(rideId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // UC6
    @PostMapping("/booking/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(rideService.cancelBooking(bookingId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // View Driver History
    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<List<Ride>> getDriverHistory(@PathVariable Long driverId) {
        return ResponseEntity.ok(rideService.getDriverHistory(driverId));
    }

    // View Passenger History
    @GetMapping("/passenger/{passengerId}/history")
    public ResponseEntity<List<Booking>> getPassengerHistory(@PathVariable Long passengerId) {
        return ResponseEntity.ok(rideService.getPassengerHistory(passengerId));
    }

    // Rate and Review Driver
    @PostMapping("/booking/{bookingId}/rate")
    public ResponseEntity<?> rateDriver(@PathVariable Long bookingId, @RequestParam Integer rating, @RequestParam(required = false) String review) {
        try {
            return ResponseEntity.ok(rideService.rateDriver(bookingId, rating, review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Process Payment
    @PostMapping("/booking/{bookingId}/pay")
    public ResponseEntity<?> processPayment(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(rideService.processPayment(bookingId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
