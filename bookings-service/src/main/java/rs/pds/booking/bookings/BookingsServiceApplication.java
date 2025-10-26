package rs.pds.booking.bookings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "rs.pds.booking.bookings.client")
public class BookingsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingsServiceApplication.class, args);
    }
}
