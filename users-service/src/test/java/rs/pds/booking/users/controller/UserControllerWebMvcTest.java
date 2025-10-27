package rs.pds.booking.users.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.pds.booking.users.domain.User;
import rs.pds.booking.users.dto.UserRequest;
import rs.pds.booking.users.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerWebMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserRepository userRepository;

    @Test
    void create_valid_returns() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Ana");
        userRequest.setPassword("sifra123");
        userRequest.setEmail("ana@example.com");

        Mockito.when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);

        User user = new  User();
        user.setId(1L);
        user.setName("Ana");
        user.setPassword("sifra123");
        user.setEmail("ana@example.com");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userRequest))).andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/users/1")).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void create_duplicateEmail() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Ana");
        userRequest.setPassword("sifra123");
        userRequest.setEmail("ana@example.com");

        Mockito.when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userRequest))).andExpect(status().isConflict());
    }

    @Test
    void create_invalidEmail() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Ana");
        userRequest.setPassword("sifra123");
        userRequest.setEmail("palmaumro");

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userRequest))).andExpect(status().isBadRequest());
    }
}
