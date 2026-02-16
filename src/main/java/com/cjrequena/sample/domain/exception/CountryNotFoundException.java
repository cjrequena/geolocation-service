package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class CountryNotFoundException extends DomainRuntimeException {
  public CountryNotFoundException(String message) {
    super(message);
  }
}
