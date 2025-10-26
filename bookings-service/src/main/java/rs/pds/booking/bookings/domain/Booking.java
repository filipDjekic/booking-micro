package rs.pds.booking.bookings.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Positive
    @Column(nullable = false)
    private Long userId;

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String resourceName;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    //getter-i
    public Long getId() {
        return id;
    }
    public Long getUserId() {
        return userId;
    }
    public String getResourceName() {
        return resourceName;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public BigDecimal getPrice() {
        return price;
    }
    //setter-i
    public void setId(Long id) {
        this.id = id;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
