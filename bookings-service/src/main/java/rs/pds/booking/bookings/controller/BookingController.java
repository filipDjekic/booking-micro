package rs.pds.booking.bookings.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.BookingRequest;
import rs.pds.booking.bookings.dto.BookingResponse;
import rs.pds.booking.bookings.repository.BookingRepository;
import rs.pds.booking.bookings.service.BookingService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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

    // GET all
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAll() {
        List<BookingResponse> out = bookingRepository.findAll().stream().map(BookingController::toResponse).toList();
        return ResponseEntity.ok(out);
    }

    // PUT (update)
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable("id") Long id, @Valid @RequestBody BookingRequest input) {
        Booking postojeci = bookingRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        postojeci.setUserId(input.getUserId());
        postojeci.setResourceName(input.getResourceName());
        postojeci.setPrice(input.getPrice());
        postojeci.setStart(input.getStart());
        postojeci.setEnd(input.getStart().plusDays(7));

        Booking updated = bookingRepository.save(postojeci);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if(!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Booking ne postoji");
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    //helperi
    private static BookingResponse toResponse(Booking b){
        return new BookingResponse(b.getId(), b.getUserId(), b.getResourceName(), b.getStart(), b.getEnd(), b.getPrice());
    }
}
