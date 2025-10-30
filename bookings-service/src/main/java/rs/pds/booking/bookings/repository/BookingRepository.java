package rs.pds.booking.bookings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pds.booking.bookings.domain.Booking;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByUserIdAndResourceName(Long userId, String resourceName);
}
