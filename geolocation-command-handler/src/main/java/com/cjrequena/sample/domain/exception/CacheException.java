package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class CacheException extends DomainRuntimeException {
  public CacheException(String message) {
    super(message);
  }

  public CacheException(String message, Throwable ex) {
    super(message, ex);
  }

}
