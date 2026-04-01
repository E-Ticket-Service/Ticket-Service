package abb.tech.ticket_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReqEventSessionDto(

        @NotNull(message = "hallId boş ola bilməz")
        Long hallId,

        @NotNull(message = "startTime boş ola bilməz")
        LocalDateTime startTime,

        @NotNull(message = "endTime boş ola bilməz")
        LocalDateTime endTime,

        @NotNull(message = "basePrice boş ola bilməz")
        @DecimalMin(value = "0.0", inclusive = false, message = "basePrice sıfırdan böyük olmalıdır")
        BigDecimal basePrice,

        @NotNull(message = "availableSeats boş ola bilməz")
        @Min(value = 1, message = "availableSeats ən azı 1 olmalıdır")
        Integer availableSeats
) {
}
