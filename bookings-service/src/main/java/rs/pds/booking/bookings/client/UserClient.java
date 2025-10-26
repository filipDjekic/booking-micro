package rs.pds.booking.bookings.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rs.pds.booking.bookings.dto.UserSummary;

@FeignClient(name = "USERS-SERVICE")
public interface UserClient {

    @GetMapping("/users/{id}")
    public UserSummary getById(@PathVariable("id") Long id);
}
