package abb.tech.ticket_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException exception) {
        ProblemDetail error=ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,exception.getMessage());
        error.setTitle("Resource Not Found");
        return error;
    }
    @ExceptionHandler(DuplicateEventException.class)
    public ProblemDetail handleDublicatEvent(DuplicateEventException exception) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        error.setTitle("Dublicat Name Error");
        return error;
    }
}
