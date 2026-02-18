package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class CountryRequiredException extends DomainRuntimeException {
  public CountryRequiredException(String message) {
    super(message);
  }
}
