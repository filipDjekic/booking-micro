package rs.pds.booking.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 60)
    @Column(nullable = false, length = 20)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    @Column(nullable = false, length = 20)
    private String password;

    //getter-i
    public Long getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    //setter-i
    public void  setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
