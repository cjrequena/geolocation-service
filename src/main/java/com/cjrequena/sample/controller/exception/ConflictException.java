package com.cjrequena.sample.controller.exception;

import org.springframework.http.HttpStatus;

/**
 * @author cjrequena
 */
public class ConflictException extends ControllerRuntimeException {

  public ConflictException() {
    super(HttpStatus.CONFLICT);
  }

  public ConflictException(String message) {
    super(HttpStatus.CONFLICT, message);
  }

  public ConflictException(String message, Throwable cause) {
    super(HttpStatus.CONFLICT, message, cause);
  }
}
