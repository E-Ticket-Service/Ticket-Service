package abb.tech.ticket_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "client.data.redis")
public class RedisProperties {
    private String reservationKey;
    private long reservationTtlMinutes;
    private String paymentIdempotencyKey;
    private long paymentIdempotencyTtlHours;

    public String getReservationPrefix() {
        if (reservationKey == null || !reservationKey.contains("%d")) {
            return reservationKey;
        }
        return reservationKey.substring(0, reservationKey.indexOf("%d"));
    }
}
