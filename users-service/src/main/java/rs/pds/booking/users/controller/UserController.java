package rs.pds.booking.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rs.pds.booking.users.domain.User;
import rs.pds.booking.users.dto.UserDTO;
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
    public ResponseEntity<UserDTO> create(@Valid @RequestBody User input, UriComponentsBuilder uriBuilder){

        if(userRepository.existsByEmail(input.getEmail())){
            return ResponseEntity.status(409).build();
        }

        User saved = userRepository.save(input);

        URI location = uriBuilder.path("/users/{id}").buildAndExpand(saved.getId()).toUri();

        return ResponseEntity.created(location).body(toDto(saved));
    }

    // GET metoda za user-a path: /users/{id} -> status 200 OK + userdto ili baca 404
    public ResponseEntity<UserDTO> getById(@PathVariable Long id){
        return userRepository.findById(id).map(user -> ResponseEntity.ok(toDto(user))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // helperi
    private static UserDTO toDto(User u){
        return new UserDTO(u.getId(), u.getName(), u.getEmail());
    }
}
