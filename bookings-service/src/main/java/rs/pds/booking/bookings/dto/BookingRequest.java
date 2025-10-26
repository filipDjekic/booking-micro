package rs.pds.booking.bookings.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingRequest {

    @NotNull
    private Long userId;

    @NotNull
    @Size(min = 2, max = 20)
    private String resourceName;

    @NotNull
    private LocalDateTime start;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    //getter-i
    public Long getUserId() {
        return userId;
    }
    public String getResourceName() {
        return resourceName;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public BigDecimal getPrice() {
        return price;
    }
    //setter-i
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
