package com.carpool.repository;
import com.carpool.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findBySourceAndDestinationAndStatusAndAvailableSeatsGreaterThanEqual(
        String source, String destination, String status, int seats);
        
    List<Ride> findByDriverId(Long driverId);
}
