package com.siemens.internship.advice;

import com.siemens.internship.controller.ItemController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.CompletionException;

@RestControllerAdvice(assignableTypes = ItemController.class)
public class ItemControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExceptions(Exception ex) {
        //Added error handling for the /api/items/process route
        Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
        return ResponseEntity.internalServerError()
                .body("Error during processing: " + cause.getMessage());
    }

}
