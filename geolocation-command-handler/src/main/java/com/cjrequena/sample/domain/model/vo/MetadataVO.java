package com.cjrequena.sample.domain.model.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Enhanced Metadata value object for storing flexible JSON metadata.
 * Provides type-safe access to common metadata patterns while maintaining JSON flexibility.
 */
@Getter
@EqualsAndHashCode
public class MetadataVO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final JsonNode value;

  private MetadataVO(JsonNode value) {
    this.value = value != null ? value : OBJECT_MAPPER.createObjectNode();
  }

  /**
   * Create metadata from JsonNode.
   */
  public static MetadataVO of(JsonNode value) {
    return new MetadataVO(value);
  }

  /**
   * Create empty metadata.
   */
  public static MetadataVO empty() {
    return new MetadataVO(OBJECT_MAPPER.createObjectNode());
  }

  /**
   * Create metadata from Map.
   */
  public static MetadataVO fromMap(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return empty();
    }
    ObjectNode node = OBJECT_MAPPER.valueToTree(map);
    return new MetadataVO(node);
  }

  /**
   * Create metadata from JSON string.
   */
  public static MetadataVO fromJson(String json) {
    if (json == null || json.trim().isEmpty()) {
      return empty();
    }
    try {
      JsonNode node = OBJECT_MAPPER.readTree(json);
      return new MetadataVO(node);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid JSON: " + json, e);
    }
  }

  /**
   * Check if metadata is empty.
   */
  public boolean isEmpty() {
    return value.isEmpty();
  }

  /**
   * Check if metadata has a specific key.
   */
  public boolean has(String key) {
    return value.has(key);
  }

  /**
   * Get the number of fields in metadata.
   */
  public int size() {
    if (value.isObject()) {
      return value.size();
    }
    return 0;
  }

  /**
   * Get all keys in metadata.
   */
  public Set<String> keys() {
    if (!value.isObject()) {
      return Collections.emptySet();
    }
    Set<String> keys = new HashSet<>();
    value.fieldNames().forEachRemaining(keys::add);
    return Collections.unmodifiableSet(keys);
  }

  // ==================== String Operations ====================

  /**
   * Add or update a string value (creates new immutable instance).
   */
  public MetadataVO with(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (value != null) {
      newNode.put(key, value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get string value.
   */
  public String getString(String key) {
    if (!value.has(key)) {
      return null;
    }
    JsonNode node = value.get(key);
    return node.isNull() ? null : node.asText();
  }

  /**
   * Get string value with default.
   */
  public String getString(String key, String defaultValue) {
    String val = getString(key);
    return val != null ? val : defaultValue;
  }

  // ==================== Numeric Operations ====================

  /**
   * Add or update an integer value.
   */
  public MetadataVO withInt(String key, Integer value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (value != null) {
      newNode.put(key, value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get integer value.
   */
  public Integer getInt(String key) {
    if (!value.has(key)) {
      return null;
    }
    JsonNode node = value.get(key);
    return node.isNull() ? null : node.asInt();
  }

  /**
   * Get integer value with default.
   */
  public int getInt(String key, int defaultValue) {
    Integer val = getInt(key);
    return val != null ? val : defaultValue;
  }

  /**
   * Add or update a long value.
   */
  public MetadataVO withLong(String key, Long value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (value != null) {
      newNode.put(key, value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get long value.
   */
  public Long getLong(String key) {
    if (!value.has(key)) {
      return null;
    }
    JsonNode node = value.get(key);
    return node.isNull() ? null : node.asLong();
  }

  /**
   * Add or update a double value.
   */
  public MetadataVO withDouble(String key, Double value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (value != null) {
      newNode.put(key, value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get double value.
   */
  public Double getDouble(String key) {
    if (!value.has(key)) {
      return null;
    }
    JsonNode node = value.get(key);
    return node.isNull() ? null : node.asDouble();
  }

  // ==================== Boolean Operations ====================

  /**
   * Add or update a boolean value.
   */
  public MetadataVO withBoolean(String key, Boolean value) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (value != null) {
      newNode.put(key, value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get boolean value.
   */
  public Boolean getBoolean(String key) {
    if (!value.has(key)) {
      return null;
    }
    JsonNode node = value.get(key);
    return node.isNull() ? null : node.asBoolean();
  }

  /**
   * Get boolean value with default.
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    Boolean val = getBoolean(key);
    return val != null ? val : defaultValue;
  }

  // ==================== Array Operations ====================

  /**
   * Add or update a list of strings.
   */
  public MetadataVO withStringList(String key, List<String> values) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (values != null && !values.isEmpty()) {
      ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
      values.forEach(arrayNode::add);
      newNode.set(key, arrayNode);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get list of strings.
   */
  public List<String> getStringList(String key) {
    if (!value.has(key)) {
      return Collections.emptyList();
    }
    JsonNode node = value.get(key);
    if (!node.isArray()) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<>();
    node.forEach(item -> list.add(item.asText()));
    return Collections.unmodifiableList(list);
  }

  // ==================== Object Operations ====================

  /**
   * Add or update a nested object.
   */
  public MetadataVO withObject(String key, MetadataVO nestedMetadata) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    if (nestedMetadata != null && !nestedMetadata.isEmpty()) {
      newNode.set(key, nestedMetadata.value);
    } else {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  /**
   * Get nested metadata object.
   */
  public MetadataVO getObject(String key) {
    if (!value.has(key)) {
      return MetadataVO.empty();
    }
    JsonNode node = value.get(key);
    if (!node.isObject()) {
      return MetadataVO.empty();
    }
    return new MetadataVO(node);
  }

  // ==================== Removal Operations ====================

  /**
   * Remove a key (creates new immutable instance).
   */
  public MetadataVO without(String key) {
    if (key == null || !has(key)) {
      return this;
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    newNode.remove(key);
    return new MetadataVO(newNode);
  }

  /**
   * Remove multiple keys.
   */
  public MetadataVO without(String... keys) {
    if (keys == null || keys.length == 0) {
      return this;
    }
    ObjectNode newNode = ((ObjectNode) this.value).deepCopy();
    for (String key : keys) {
      newNode.remove(key);
    }
    return new MetadataVO(newNode);
  }

  // ==================== Merge Operations ====================

  /**
   * Merge with another MetadataVO (other values take precedence).
   */
  public MetadataVO merge(MetadataVO other) {
    if (other == null || other.isEmpty()) {
      return this;
    }
    if (this.isEmpty()) {
      return other;
    }

    ObjectNode merged = ((ObjectNode) this.value).deepCopy();
    ObjectNode otherNode = (ObjectNode) other.value;

    otherNode.fields().forEachRemaining(entry -> {
      merged.set(entry.getKey(), entry.getValue());
    });

    return new MetadataVO(merged);
  }

  // ==================== Conversion Operations ====================

  /**
   * Convert to Map.
   */
  public Map<String, Object> toMap() {
    try {
      return OBJECT_MAPPER.convertValue(value, Map.class);
    } catch (IllegalArgumentException e) {
      return Collections.emptyMap();
    }
  }

  /**
   * Convert to JSON string.
   */
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  /**
   * Convert to pretty JSON string.
   */
  public String toPrettyJson() {
    try {
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  @Override
  public String toString() {
    return toJson();
  }

  // ==================== Common Metadata Patterns ====================

  /**
   * Add tags (common metadata pattern).
   */
  public MetadataVO withTags(String... tags) {
    return withStringList("tags", Arrays.asList(tags));
  }

  /**
   * Get tags.
   */
  public List<String> getTags() {
    return getStringList("tags");
  }

  /**
   * Add description (common metadata pattern).
   */
  public MetadataVO withDescription(String description) {
    return with("description", description);
  }

  /**
   * Get description.
   */
  public String getDescription() {
    return getString("description");
  }

  /**
   * Add source (common metadata pattern).
   */
  public MetadataVO withSource(String source) {
    return with("source", source);
  }

  /**
   * Get source.
   */
  public String getSource() {
    return getString("source");
  }

  /**
   * Add version (common metadata pattern).
   */
  public MetadataVO withVersion(String version) {
    return with("version", version);
  }

  /**
   * Get version.
   */
  public String getVersion() {
    return getString("version");
  }
}
