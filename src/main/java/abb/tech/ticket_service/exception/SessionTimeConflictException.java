package abb.tech.ticket_service.exception;

public class SessionTimeConflictException extends RuntimeException {

    public SessionTimeConflictException(String message) {
        super(message);
    }
}
