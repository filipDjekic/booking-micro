package rs.pds.booking.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.users.domain.User;
import rs.pds.booking.users.dto.UserRequest;
import rs.pds.booking.users.dto.UserResponse;
import rs.pds.booking.users.repository.UserRepository;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // POST metoda za user-a (201 vraca sa created, lokacijom i userdto (bez lozinke NA RA VNO)
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest input, UriComponentsBuilder uriBuilder){

        if (userRepository.existsByEmail(input.getEmail())) {
            return ResponseEntity.status(409).build(); // Conflict
        }

        User u = new User();
        u.setName(input.getName());
        u.setEmail(input.getEmail());
        u.setPassword(input.getPassword());

        User saved = userRepository.save(u);

        URI location = uriBuilder.path("/users/{id}").buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    // GET metoda za user-a path: /users/{id} -> status 200 OK + userdto ili baca 404
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable("id") Long id) {
        return userRepository.findById(id).map(user -> ResponseEntity.ok(toResponse(user))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // helperi
    private static UserResponse toResponse(User u){
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }
}
