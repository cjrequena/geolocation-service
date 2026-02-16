package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class CityNotFoundException extends DomainRuntimeException {
  public CityNotFoundException(String message) {
    super(message);
  }
}
