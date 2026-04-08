package abb.tech.ticket_service.config;

import abb.tech.ticket_service.enums.SeatStatus;
import abb.tech.ticket_service.model.EventSessionSeat;
import abb.tech.ticket_service.service.EventSessionSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final EventSessionSeatService eventSessionSeatService;
    private final RedisProperties redisProperties;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      EventSessionSeatService eventSessionSeatService,
                                      RedisProperties redisProperties) {
        super(listenerContainer);
        this.eventSessionSeatService = eventSessionSeatService;
        this.redisProperties = redisProperties;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired: {}", expiredKey);

        String prefix = redisProperties.getReservationPrefix();
        if (expiredKey.startsWith(prefix)) {
            processReservationExpiration(expiredKey);
        }
    }

    private void processReservationExpiration(String expiredKey) {
        try {
            String[] parts = expiredKey.split(":");
            Long sessionId = Long.parseLong(parts[2]);
            Long seatId = Long.parseLong(parts[4]);

            EventSessionSeat sessionSeat = eventSessionSeatService.findByEventSessionIdAndSeatId(sessionId, seatId);
            if (sessionSeat.getSeatStatus() == SeatStatus.RESERVED) {
                sessionSeat.setSeatStatus(SeatStatus.AVAILABLE);
                eventSessionSeatService.create(sessionSeat);
                log.info("Seat {} in session {} set back to AVAILABLE due to timeout", seatId, sessionId);
            }
        } catch (Exception e) {
            log.error("Error processing expired redis key {}: {}", expiredKey, e.getMessage());
        }
    }
}
