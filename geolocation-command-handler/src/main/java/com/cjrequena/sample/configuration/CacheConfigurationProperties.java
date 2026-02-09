package com.cjrequena.sample.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * <p>
 * <p>
 * <p>
 *
 * @author cjrequena
 */
@Data
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheConfigurationProperties {
  boolean cacheEnabled;
  boolean fullLoadEnabled;
  String type;

}
