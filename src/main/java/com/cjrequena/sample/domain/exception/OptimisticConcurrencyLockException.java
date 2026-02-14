package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class OptimisticConcurrencyLockException extends DomainRuntimeException {
  public OptimisticConcurrencyLockException(String message) {
    super(message);
  }

  public OptimisticConcurrencyLockException(String message, Throwable ex) {
    super(message, ex);
  }
}
