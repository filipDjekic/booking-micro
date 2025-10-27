package rs.pds.booking.users.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.UserSummary;
import rs.pds.booking.bookings.repository.BookingRepository;
import rs.pds.booking.bookings.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    BookingRepository repo = Mockito.mock(BookingRepository.class);
    UserClient userClient = Mockito.mock(UserClient.class);
    BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService(repo, userClient);
    }

    private Booking sample() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUserId(1L);
        b.setResourceName("Sala A");
        b.setStart(LocalDateTime.parse("2025-10-26T10:00:00"));
        b.setEnd(LocalDateTime.parse("2025-11-02T10:00:00"));
        b.setPrice(new BigDecimal("20.00"));
        return b;
    }

    @Test
    void getDetails_userUp_returnsBookingAndUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample()));
        when(userClient.getById(1L))
                .thenReturn(new UserSummary(1L, "Ana", "ana@example.com"));

        BookingDetails d = service.getDetails(1L);

        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getUser().getName()).isEqualTo("Ana");
        assertThat(d.getUser().getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void getDetails_userDown_usesFallback() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample()));
        when(userClient.getById(1L)).thenThrow(new RuntimeException("down"));

        BookingDetails d = service.getDetailsFallback(1L, new RuntimeException("down"));

        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getUser().getName()).isEqualTo("UNAVAILABLE");
        assertThat(d.getUser().getEmail()).isEqualTo("unavailable@local");
    }
}
