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
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // POST /users -> 201 Created + Location + DTO (bez lozinke)
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest input,
                                               UriComponentsBuilder uriBuilder) {

        if (userRepository.existsByEmail(input.getEmail())) {
            // 409 ako email već postoji
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

    // GET /users/{id} -> 200 ili 404
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /users -> lista svih
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        var list = userRepository.findAll()
                .stream()
                .map(UserController::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    // PUT /users/{id} -> update
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UserRequest input) {
        var postojeci = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User nije pronadjen"));

        if (!postojeci.getEmail().equalsIgnoreCase(input.getEmail())
                && userRepository.existsByEmailAndIdNot(input.getEmail(), id)) {
            throw new ResponseStatusException(CONFLICT, "Email se vec koristi.");
        }

        postojeci.setName(input.getName());
        postojeci.setEmail(input.getEmail());
        postojeci.setPassword(input.getPassword());

        var updated = userRepository.save(postojeci);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE /users/{id} -> 204 ili 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User nije pronadjen.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // helper
    private static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }
}
