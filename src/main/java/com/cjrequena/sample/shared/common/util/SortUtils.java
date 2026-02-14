package com.cjrequena.sample.shared.common.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing sort expressions into Spring Data Sort objects.
 *
 * <p>Supports sort expressions in the format:
 * <ul>
 *   <li>Single field: "property,direction" (e.g., "name,asc")</li>
 *   <li>Multiple fields: "prop1,dir1;prop2,dir2" (e.g., "name,asc;createdAt,desc")</li>
 * </ul>
 *
 * @author cjrequena
 */
public class SortUtils {

  private SortUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Parses a sort expression string into a Spring Data Sort object.
   *
   * @param sortExpression the sort expression (e.g., "name,asc" or "name,asc;createdAt,desc")
   * @return the Sort object, or Sort.unsorted() if expression is null/empty
   * @throws IllegalArgumentException if the sort expression format is invalid
   */
  public static Sort parseSort(String sortExpression) {
    if (sortExpression == null || sortExpression.trim().isEmpty()) {
      return Sort.unsorted();
    }

    List<Sort.Order> orders = new ArrayList<>();
    String[] sortFields = sortExpression.split(";");

    for (String sortField : sortFields) {
      String[] parts = sortField.trim().split(",");
      
      if (parts.length != 2) {
        throw new IllegalArgumentException(
          "Invalid sort format: '" + sortField + "'. Expected format: 'property,direction'"
        );
      }

      String property = parts[0].trim();
      String direction = parts[1].trim().toLowerCase();

      if (property.isEmpty()) {
        throw new IllegalArgumentException("Sort property cannot be empty");
      }

      Sort.Direction sortDirection;
      if ("asc".equals(direction)) {
        sortDirection = Sort.Direction.ASC;
      } else if ("desc".equals(direction)) {
        sortDirection = Sort.Direction.DESC;
      } else {
        throw new IllegalArgumentException(
          "Invalid sort direction: '" + direction + "'. Must be 'asc' or 'desc'"
        );
      }

      orders.add(new Sort.Order(sortDirection, property));
    }

    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
  }
}
