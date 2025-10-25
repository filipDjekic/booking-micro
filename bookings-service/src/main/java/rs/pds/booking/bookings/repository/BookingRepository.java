package rs.pds.booking.bookings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pds.booking.bookings.domain.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
