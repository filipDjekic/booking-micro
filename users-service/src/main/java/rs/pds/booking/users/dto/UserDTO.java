package rs.pds.booking.users.dto;

public class UserDTO {
    private Long id;
    private String name;
    private String email;

    public UserDTO() {}

    public UserDTO(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    //getter-i
    public Long getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    //setter-i
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
