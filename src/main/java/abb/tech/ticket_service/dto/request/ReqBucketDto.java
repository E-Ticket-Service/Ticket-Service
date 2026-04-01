package abb.tech.ticket_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReqBucketDto(

        @NotNull(message = "userId boş ola bilməz")
        Long userId,

        @NotNull(message = "eventSessionId boş ola bilməz")
        Long eventSessionId,

        // seatId optional ola bilər (hasSeatMap=false olan hall-lar üçün)
        Long seatId,

        @NotNull(message = "count boş ola bilməz")
        @Min(value = 1, message = "count ən azı 1 olmalıdır")
        Integer count
) {
}
