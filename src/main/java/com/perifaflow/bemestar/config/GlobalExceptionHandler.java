package com.perifaflow.bemestar.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        var locale = LocaleContextHolder.getLocale();
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("error.validation", null, locale));

        List<Map<String,String>> details = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()){
            String msg = fe.getDefaultMessage();
            // se a mensagem vier como chave (ex: validation.turno), resolvemos no bundle
            if (msg != null && msg.matches("[a-zA-Z0-9_.-]+")) {
                msg = messageSource.getMessage(msg, null, msg, locale);
            }
            details.add(Map.of("field", fe.getField(), "message", msg));
        }
        pd.setProperty("details", details);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadJson(HttpMessageNotReadableException ex){
        var locale = LocaleContextHolder.getLocale();
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("error.validation", null, locale));
        pd.setDetail(messageSource.getMessage("error.invalid_body", null, locale));
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("INVALID_ARGUMENT");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGeneric(Exception ex){
        var locale = LocaleContextHolder.getLocale();
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("INTERNAL_ERROR");
        pd.setDetail(messageSource.getMessage("error.internal", null, locale));
        return pd;
    }
}
