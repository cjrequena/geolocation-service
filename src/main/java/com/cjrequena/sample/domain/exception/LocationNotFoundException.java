package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class LocationNotFoundException extends DomainRuntimeException {
  public LocationNotFoundException(String message) {
    super(message);
  }
}
