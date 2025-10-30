package rs.pds.booking.bookings.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.BookingRequest;
import rs.pds.booking.bookings.dto.BookingResponse;
import rs.pds.booking.bookings.dto.UserSummary;
import rs.pds.booking.bookings.repository.BookingRepository;
import rs.pds.booking.bookings.service.BookingService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final UserClient usersClient;

    public BookingController(BookingRepository bookingRepository,
                             BookingService bookingService,
                             UserClient usersClient) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.usersClient = usersClient;
    }

    // === Pomoćna metoda: obavezna provera da li user postoji (404 ako ne postoji) ===
    private UserSummary ensureUserExists(Long userId) {
        try {
            return usersClient.getById(userId); // 200 -> postoji
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(NOT_FOUND, "User ne postoji");
        } catch (FeignException e) {
            // npr. 503/timeout sa users-service; može i drugi status po želji
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Users servis nije dostupan");
        }
    }

    // === POST /bookings  -> UPSERT (po userId + resourceName) uz proveru user-a ===
    @PostMapping
    public ResponseEntity<BookingResponse> createOrUpdate(@Valid @RequestBody BookingRequest input,
                                                          UriComponentsBuilder uriBuilder) {
        // 1) obavezna provera user-a
        ensureUserExists(input.getUserId());

        // 2) upsert logika
        LocalDateTime start = input.getStart();
        LocalDateTime end = start.plusDays(7);

        Optional<Booking> maybeExisting =
                bookingRepository.findByUserIdAndResourceName(input.getUserId(), input.getResourceName());

        if (maybeExisting.isPresent()) {
            Booking existing = maybeExisting.get();
            existing.setStart(start);
            existing.setEnd(end);
            existing.setPrice(input.getPrice());
            Booking updated = bookingRepository.save(existing);
            return ResponseEntity.ok(toResponse(updated));
        }

        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setUserId(input.getUserId());
        booking.setResourceName(input.getResourceName());
        booking.setPrice(input.getPrice());

        Booking saved = bookingRepository.save(booking);
        URI location = uriBuilder.path("/bookings/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    // === GET /bookings/{id} -> 200 ili 404; dodatno 404 ako user povezan s bookingom ne postoji ===
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable("id") Long id) {
        return bookingRepository.findById(id)
                .map(b -> {
                    ensureUserExists(b.getUserId()); // obavezna provera
                    return ResponseEntity.ok(toResponse(b));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // === GET /bookings/{id}/details -> 404 ako nema booking; zatim 404 ako nema user ===
    @GetMapping("/{id}/details")
    public ResponseEntity<BookingDetails> details(@PathVariable("id") Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        // provera user-a (eksplicitno pre agregacije)
        UserSummary user = ensureUserExists(booking.getUserId());

        // standardni detalji (servis može opet da zove usersClient, ali već imamo user ako želiš da ga proslediš)
        BookingDetails details = bookingService.getDetails(id);
        // Ako hoćeš da izbegneš drugi poziv, možeš napraviti varijantu koja prima već učitanog user-a.
        return ResponseEntity.ok(details);
    }

    // === GET /bookings -> 200; (ne proveravamo svakog user-a radi performansi liste)
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAll() {
        List<BookingResponse> out = bookingRepository.findAll()
                .stream()
                // Ako baš želiš striktno “svuda”, ovde bi za svaki red zvao ensureUserExists(b.getUserId()) – ali to je N poziva.
                .map(BookingController::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    // === PUT /bookings/{id} -> 200 ili 404; provera user-a iz inputa pre izmene ===
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody BookingRequest input) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        // provera user-a iz payload-a (ciljani user mora da postoji)
        ensureUserExists(input.getUserId());

        existing.setUserId(input.getUserId());
        existing.setResourceName(input.getResourceName());
        existing.setPrice(input.getPrice());
        existing.setStart(input.getStart());
        existing.setEnd(input.getStart().plusDays(7));

        Booking updated = bookingRepository.save(existing);
        return ResponseEntity.ok(toResponse(updated));
    }

    // === DELETE /bookings/{id} -> 204 ili 404; opciono validiraj i user-a povezanog sa bookingom ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        // Ako želiš “svuda” striktno: ne dozvoli brisanje ako user više ne postoji
        ensureUserExists(existing.getUserId());

        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // helper
    private static BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getUserId(),
                b.getResourceName(),
                b.getStart(),
                b.getEnd(),
                b.getPrice()
        );
    }
}
