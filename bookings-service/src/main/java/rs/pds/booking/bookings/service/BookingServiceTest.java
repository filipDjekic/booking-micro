package rs.pds.booking.bookings.service;

import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.UserSummary;
import rs.pds.booking.bookings.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingServiceTest {
    BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    UserClient userClient = Mockito.mock(UserClient.class);
    BookingService bookingService;

    @BeforeEach
    public void setup() {
        bookingService = new BookingService(bookingRepository, userClient);
    }

    private Booking sample(){
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(1L);
        booking.setResourceName("Sala B");
        booking.setStart(LocalDateTime.parse("2025-10-26T10:00:00"));
        booking.setEnd(LocalDateTime.parse("2025-11-02T10:00:00"));
        booking.setPrice(new BigDecimal("20.00"));
        return booking;
    }

    @Test
    void getDetails_userUp(){
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sample()));
        when(userClient.getById(1L)).thenReturn(new UserSummary(1L, "Ana", "ana@example.com"));

        BookingDetails bookingDetails = bookingService.getDetails(1L);

        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getUser().getName()).isEqualTo("Ana");
        assertThat(d.getUser().getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void getDetails_userDown(){
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sample()));
        when(userClient.getById(1L)).thenThrow(new RuntimeException("down"));

        BookingDetails bookingDetails = bookingService.getDetailsFallback(1L, new RuntimeException("down"));

        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getUser().getName()).isEqualTo("UNAVAILABLE");
        assertThat(d.getUser().getEmail()).isEqualTo("unavailable");
    }
}
