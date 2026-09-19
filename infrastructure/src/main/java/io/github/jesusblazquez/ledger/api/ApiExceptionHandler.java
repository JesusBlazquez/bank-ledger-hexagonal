package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.exception.IdempotencyConflictException;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotEmptyException;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotOperativeException;
import io.github.jesusblazquez.ledger.domain.exception.CurrencyMismatchException;
import io.github.jesusblazquez.ledger.domain.exception.DailyLimitExceededException;
import io.github.jesusblazquez.ledger.domain.exception.DomainException;
import io.github.jesusblazquez.ledger.domain.exception.InsufficientBalanceException;
import io.github.jesusblazquez.ledger.domain.exception.InvalidAmountException;
import io.github.jesusblazquez.ledger.domain.exception.InvalidIbanException;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns business failures into HTTP answers, using Problem Details (RFC 9457).
 *
 * <p>A machine-readable {@code type} and a clear {@code detail} beat a bare 500 with a stack trace:
 * the caller can react to "insufficient balance" differently from "account not found". Note that no
 * message here is invented — the domain already explains itself.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    private static final String PROBLEM_BASE = "https://github.com/JesusBlazquez/bank-ledger-hexagonal/problems/";

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail accountNotFound(AccountNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "account-not-found", "Account not found", exception.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    ProblemDetail insufficientBalance(InsufficientBalanceException exception) {
        return problem(HttpStatus.CONFLICT, "insufficient-balance", "Insufficient balance", exception.getMessage());
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    ProblemDetail dailyLimit(DailyLimitExceededException exception) {
        return problem(
                HttpStatus.CONFLICT, "daily-limit-exceeded", "Daily transfer limit exceeded", exception.getMessage());
    }

    @ExceptionHandler({AccountNotOperativeException.class, AccountNotEmptyException.class})
    ProblemDetail accountState(DomainException exception) {
        return problem(
                HttpStatus.CONFLICT, "account-state", "Account state does not allow this", exception.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail idempotencyConflict(IdempotencyConflictException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "idempotency-conflict",
                "Idempotency key reused for a different request",
                exception.getMessage());
    }

    @ExceptionHandler({InvalidAmountException.class, CurrencyMismatchException.class, InvalidIbanException.class})
    ProblemDetail invalidInput(DomainException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "invalid-request", "Invalid request", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validationFailed(MethodArgumentNotValidException exception) {
        Map<String, String> fields = exception.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage(),
                        (first, second) -> first));

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", "The request body is not valid");
        problem.setProperty("fields", fields);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}
