package rs.pds.booking.bookings.service;

import org.springframework.stereotype.Service;
import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.UserSummary;
import rs.pds.booking.bookings.repository.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserClient userClient;

    public BookingService(BookingRepository bookingRepository, UserClient userClient) {
        this.bookingRepository = bookingRepository;
        this.userClient = userClient;
    }

    public BookingDetails getDetails(Long bookingId){
        Booking b =  bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking po ID-ju nije nadjen (ID: "+bookingId+")"));

        UserSummary user = userClient.getById(b.getUserId());

        return new BookingDetails(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice(), user);
    }
}
