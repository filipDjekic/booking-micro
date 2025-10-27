package rs.pds.booking.bookings.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.UserSummary;
import rs.pds.booking.bookings.repository.BookingRepository;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserClient userClient;

    public BookingService(BookingRepository bookingRepository, UserClient userClient) {
        this.bookingRepository = bookingRepository;
        this.userClient = userClient;
    }

    @CircuitBreaker(name="usersClient", fallbackMethod = "getDetailsFallback")
    @Retry(name="usersClient")
    public BookingDetails getDetails(Long bookingId){
        Booking b =  bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking po ID-ju nije nadjen (ID: "+bookingId+")"));

        UserSummary user = userClient.getById(b.getUserId());

        return new BookingDetails(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice(), user);
    }

    public BookingDetails getDetailsFallback(Long bookingId, Throwable ex){
        log.warn("Fallback trigerovan za bookingId: "+bookingId+",; cause:"+(ex!=null ? ex.toString() : "n/a"));

        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking " + bookingId + " nije pronadjen."));

        UserSummary user = new UserSummary(null, "NEMA", "NeMa");

        return new BookingDetails(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice(), user);
    }
}
