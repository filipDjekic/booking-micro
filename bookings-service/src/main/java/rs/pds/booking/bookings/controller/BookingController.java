package rs.pds.booking.bookings.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.BookingRequest;
import rs.pds.booking.bookings.dto.BookingResponse;
import rs.pds.booking.bookings.repository.BookingRepository;
import rs.pds.booking.bookings.service.BookingService;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public BookingController(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    //Post metoda, ist kao i za user-a, vraca druge stvari samo
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest input, UriComponentsBuilder uriBuilder) {
        LocalDateTime start = input.getStart();
        LocalDateTime end = start.plusDays(7);

        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setUserId(input.getUserId());
        booking.setResourceName(input.getResourceName());
        booking.setPrice(input.getPrice());

        Booking saved = bookingRepository.save(booking);

        URI location =  uriBuilder.path("/bookings/{id}").buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    //Get metoda
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable("id") long id) {
        return bookingRepository.findById(id).map(b -> ResponseEntity.ok(toResponse(b))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //GET Detalji
    @GetMapping("/{id}/details")
    public ResponseEntity<BookingDetails> details(@PathVariable("id") Long id) {
        try {
            BookingDetails details = bookingService.getDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException notFound) {
            return ResponseEntity.notFound().build();
        }
    }

    //helperi
    private static BookingResponse toResponse(Booking b){
        return new BookingResponse(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice());
    }
}
