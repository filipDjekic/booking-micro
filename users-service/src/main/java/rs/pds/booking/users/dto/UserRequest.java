package rs.pds.booking.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @NotBlank
    @Size(min = 2, max = 20)
    private String name;

    @NotBlank
    @Email
    @Size(min = 2, max = 20)
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    // getter-i i setter-i
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
