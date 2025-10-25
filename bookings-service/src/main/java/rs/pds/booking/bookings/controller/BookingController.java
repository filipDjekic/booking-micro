package rs.pds.booking.bookings.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingRequest;
import rs.pds.booking.bookings.dto.BookingResponse;
import rs.pds.booking.bookings.repository.BookingRepository;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    //Post metoda, ist kao i za user-a, vraca druge stvari samo
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request, UriComponentsBuilder uriBuilder) {
        LocalDateTime start = request.getStart();
        LocalDateTime end = start.plusDays(7);

        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setUserId(request.getUserId());
        booking.setResourceName(request.getResourceName());
        booking.setPrice(request.getPrice());

        Booking saved = bookingRepository.save(booking);

        URI location =  uriBuilder.path("/bookings/{id}").buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    //Get metoda
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable long id) {
        return bookingRepository.findById(id).map(b -> ResponseEntity.ok(toResponse(b))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //helperi
    private static BookingResponse toResponse(Booking b){
        return new BookingResponse(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice());
    }
}
