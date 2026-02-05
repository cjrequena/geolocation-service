package com.cjrequena.sample.domain.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Builder
@ToString
public class ExtensionVO implements Serializable {

  private final String traceId;

  // Constructor for deserialization
  @JsonCreator
  public ExtensionVO(@JsonProperty("trace_id") String traceId) {
    this.traceId = traceId;
  }

  // You can add more custom fields if needed
  // private String customField;

  // No setters to maintain immutability

}
