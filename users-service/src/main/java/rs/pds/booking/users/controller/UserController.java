package rs.pds.booking.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.users.domain.User;
import rs.pds.booking.users.dto.UserRequest;
import rs.pds.booking.users.dto.UserResponse;
import rs.pds.booking.users.repository.UserRepository;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/users") // važi: /api/users/** preko gateway-a (StripPrefix=1) -> /users/**
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // POST /users -> 201 Created + Location: /users/{id}
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest input,
                                               UriComponentsBuilder uriBuilder) {

        if (userRepository.existsByEmail(input.getEmail())) {
            return ResponseEntity.status(CONFLICT).build();
        }

        User u = new User();
        u.setName(input.getName());
        u.setEmail(input.getEmail());
        u.setPassword(input.getPassword());

        User saved = userRepository.save(u);

        URI location = uriBuilder.path("/users/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    // GET /users/{id} -> 200 ili 404 (bez 500)
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /users -> 200 + lista
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        var list = userRepository.findAll()
                .stream()
                .map(UserController::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    // PUT /users/{id} -> 200 ili 404/409
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                               @Valid @RequestBody UserRequest input) {
        var existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User nije pronadjen"));

        if (!existing.getEmail().equalsIgnoreCase(input.getEmail())
                && userRepository.existsByEmailAndIdNot(input.getEmail(), id)) {
            throw new ResponseStatusException(CONFLICT, "Email se vec koristi.");
        }

        existing.setName(input.getName());
        existing.setEmail(input.getEmail());
        existing.setPassword(input.getPassword());

        var updated = userRepository.save(existing);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE /users/{id} -> 204 ili 404
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User nije pronadjen.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // helper: ne vraćamo password u response
    private static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }
}
