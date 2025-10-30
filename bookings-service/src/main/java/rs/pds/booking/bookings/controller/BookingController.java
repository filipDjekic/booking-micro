package rs.pds.booking.bookings.controller;

import feign.FeignException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; // anotacija
import io.github.resilience4j.retry.annotation.Retry;                  // anotacija
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.bookings.client.UserClient;
import rs.pds.booking.bookings.domain.Booking;
import rs.pds.booking.bookings.dto.BookingDetails;
import rs.pds.booking.bookings.dto.BookingRequest;
import rs.pds.booking.bookings.dto.BookingResponse;
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

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final UserClient usersClient;

    // registries za event logove (autokonfiguriše ih resilience4j-spring-boot starter)
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry cbRegistry;

    public BookingController(BookingRepository bookingRepository,
                             BookingService bookingService,
                             UserClient usersClient,
                             RetryRegistry retryRegistry,
                             CircuitBreakerRegistry cbRegistry) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.usersClient = usersClient;
        this.retryRegistry = retryRegistry;
        this.cbRegistry = cbRegistry;
    }

    // Registruj jasne logove za SVE Retry/CB evente odmah po startu
    @PostConstruct
    public void wireResilienceLogging() {
        // koristimo FQN za tipove da izbegnemo konflikt sa anotacijama
        io.github.resilience4j.retry.Retry retry = retryRegistry.retry("usersClient");
        retry.getEventPublisher()
                .onRetry(e -> log.warn("[RETRY][usersClient] attempt={} lastThrowable={}",
                        e.getNumberOfRetryAttempts(),
                        e.getLastThrowable() == null ? "n/a" : e.getLastThrowable().toString()))
                .onError(e -> log.error("[RETRY][usersClient][ERROR] {}", e))
                .onSuccess(e -> log.info("[RETRY][usersClient][SUCCESS] {}", e));

        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = cbRegistry.circuitBreaker("usersClient");
        cb.getEventPublisher()
                .onStateTransition(e -> log.warn("[CB][usersClient] {} -> {} (eventType={})",
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState(), e.getEventType()))
                .onError(e -> log.error("[CB][usersClient][ERROR] duration={}ms throwable={}",
                        e.getElapsedDuration() == null ? "n/a" : e.getElapsedDuration().toMillis(),
                        e.getThrowable() == null ? "n/a" : e.getThrowable().toString()))
                .onCallNotPermitted(e -> log.warn("[CB][usersClient] CALL_NOT_PERMITTED (OPEN)"));

        log.info("[Resilience] listeners wired in BookingController for 'usersClient'");
    }

    // ---- Provera postojanja user-a (sa Retry/CB) ----
    @Retry(name = "usersClient")
    @CircuitBreaker(name = "usersClient", fallbackMethod = "ensureUserExistsFallback")
    private void ensureUserExists(Long userId) {
        log.info("[usersGuard] checking userId={} ...", userId);
        try {
            usersClient.getById(userId); // 404/greške bacaju FeignException
            log.info("[usersGuard] userId={} EXISTS", userId);
        } catch (FeignException.NotFound e) {
            log.warn("[usersGuard] userId={} NOT FOUND (404 from users-service)", userId);
            throw new ResponseStatusException(NOT_FOUND, "User ne postoji");
        } catch (FeignException e) {
            log.error("[usersGuard] users-service error for userId={}, status={}", userId, e.status(), e);
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Users servis nije dostupan");
        }
    }

    // Fallback mora da ima isti potpis + Throwable
    @SuppressWarnings("unused")
    private void ensureUserExistsFallback(Long userId, Throwable t) {
        log.error("[usersGuard][FALLBACK] userId={} cause={}", userId, t.toString(), t);
        if (t instanceof ResponseStatusException rse && rse.getStatusCode().value() == 404) {
            throw rse; // zadrži 404 ako user realno ne postoji
        }
        throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Users servis nije dostupan (fallback)");
    }

    // POST /bookings  -> UPSERT (po userId + resourceName)
    @PostMapping
    public ResponseEntity<BookingResponse> createOrUpdate(@Valid @RequestBody BookingRequest input,
                                                          UriComponentsBuilder uriBuilder) {
        ensureUserExists(input.getUserId());

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

    // GET /bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable("id") Long id) {
        return bookingRepository.findById(id)
                .map(b -> {
                    ensureUserExists(b.getUserId());
                    return ResponseEntity.ok(toResponse(b));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /bookings/{id}/details
    @GetMapping("/{id}/details")
    public ResponseEntity<BookingDetails> details(@PathVariable("id") Long id) {
        var booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        ensureUserExists(booking.getUserId());
        var details = bookingService.getDetails(id);
        return ResponseEntity.ok(details);
    }

    // GET /bookings
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAll() {
        var out = bookingRepository.findAll()
                .stream()
                .map(BookingController::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    // PUT /bookings/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody BookingRequest input) {
        var existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

        ensureUserExists(input.getUserId());

        existing.setUserId(input.getUserId());
        existing.setResourceName(input.getResourceName());
        existing.setPrice(input.getPrice());
        existing.setStart(input.getStart());
        existing.setEnd(input.getStart().plusDays(7));

        var updated = bookingRepository.save(existing);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE /bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        var existing = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking ne postoji"));

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