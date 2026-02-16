package com.cjrequena.sample.domain.exception;

/**
 *
 * <p></p>
 * <p></p>
 * @author cjrequena
 */
public class RegionNotFoundException extends DomainRuntimeException {
  public RegionNotFoundException(String message) {
    super(message);
  }
}
