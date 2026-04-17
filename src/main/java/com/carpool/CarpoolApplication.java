package com.carpool;

import com.carpool.model.User;
import com.carpool.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CarpoolApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarpoolApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository) {
        return args -> {
            User driver = new User();
            driver.setName("John Driver");
            driver.setRole("DRIVER");
            driver.setEmail("john@example.com");
            userRepository.save(driver);

            User passenger = new User();
            passenger.setName("Alice Passenger");
            passenger.setRole("PASSENGER");
            passenger.setEmail("alice@example.com");
            userRepository.save(passenger);

            System.out.println("Mock Users Created! John (Driver ID: 1), Alice (Passenger ID: 2)");
        };
    }
}
