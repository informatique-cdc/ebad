package fr.icdc.ebad.web.rest.errors;


import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTranslator.class);
    private static final String FIELD_ERRORS_KEY = "fieldErrors";
	private static final String MESSAGE_KEY = "message";
	private static final String VIOLATIONS_KEY = "violations";
	private static final String PATH_KEY = "path";

	/**
     * Post-process Problem payload to add the message key for front-end if needed
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null || entity.getBody() == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
            return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
        } else {
            builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            }
            return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
        }
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        LOGGER.warn("Method Argument Not Valid !", ex);

        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName(), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(EbadServiceException.class)
    public ResponseEntity<Problem> handleEbadServiceException(EbadServiceException ex, NativeWebRequest request) {
        LOGGER.warn("EbadServiceException !", ex);

        Problem problem = Problem.builder()
                .withStatus(Status.BAD_REQUEST)
                .withDetail(ex.getMessage())
                .with(MESSAGE_KEY, ErrorConstants.ERR_CLIENT_FAILURE)
                .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleException(Exception ex, NativeWebRequest request) {
        LOGGER.error("Exception !", ex);

        Problem problem = Problem.builder()
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with(MESSAGE_KEY, ErrorConstants.ERR_SERVER_FAILURE)
                .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        LOGGER.error("ConcurrencyFailureException !", ex);

        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build();
        return create(ex, problem, request);
    }
}
