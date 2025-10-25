package rs.pds.booking.bookings.dto;

import rs.pds.booking.bookings.domain.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private Long userId;
    private String resourceName;
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal price;

    public BookingResponse() {}

    public BookingResponse(Long id,  Long userId, String resourceName, LocalDateTime start, LocalDateTime end, BigDecimal price) {
        this.id = id;
        this.userId = userId;
        this.resourceName = resourceName;
        this.start = start;
        this.end = end;
        this.price = price;
    }

    //getter-i i setter-i (mrzi me da pisem, intelli sense sam popunjava)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public LocalDateTime getEnd() {
        return end;
    }
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
