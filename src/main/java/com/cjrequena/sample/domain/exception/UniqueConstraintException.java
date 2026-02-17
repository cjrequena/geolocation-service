package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class UniqueConstraintException extends DomainRuntimeException {
  public UniqueConstraintException(String message) {
    super(message);
  }

  public UniqueConstraintException(String message, Throwable ex) {
    super(message, ex);
  }

}
