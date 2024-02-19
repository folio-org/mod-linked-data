package org.folio.linked.data.util;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.api.ResourceType;
import org.folio.linked.data.model.entity.Resource;

@Log4j2
@UtilityClass
public class BibframeUtils {

  private static final String DATE_CLEAN_PATTERN = "[^0-9T:\\-+.]";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
    .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendOptional(DateTimeFormatter.ofPattern("yyyy"))
    .toFormatter();


  public static String getFirstValue(Supplier<List<String>> valuesSupplier) {
    if (isNull(valuesSupplier)) {
      return "";
    }
    var values = valuesSupplier.get();
    if (isNotEmpty(values)) {
      return values.stream()
        .filter(StringUtils::isNotBlank)
        .findFirst()
        .orElse("");
    }
    return "";
  }

  public static void putProperty(Map<String, List<String>> map, PropertyDictionary property, List<String> values) {
    ofNullable(values).filter(v -> !v.isEmpty()).ifPresent(v -> map.put(property.getValue(), v));
  }

  public static String cleanDate(String initialValue) {
    return ofNullable(initialValue)
      .map(iv -> iv.replaceAll(DATE_CLEAN_PATTERN, ""))
      .filter(StringUtils::isNotEmpty)
      .map(cleanedValue -> {
        try {
          DATE_TIME_FORMATTER.parse(cleanedValue);
          return cleanedValue;
        } catch (DateTimeParseException e) {
          log.warn("Date {} was ignored as invalid", initialValue);
          return null;
        }
      })
      .orElse(null);
  }

  public static boolean isOfType(Resource resource, ResourceType type) {
    return resource.getTypes().stream().anyMatch(t -> t.getUri().equals(type.getUri()));
  }

  public static void setEdgesId(Resource resource) {
    setIncomingEdgesId(resource);
    setOutgoingEdgesId(resource);
  }

  private static void setIncomingEdgesId(Resource resource) {
    resource.getIncomingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getHash());
      setIncomingEdgesId(edge.getSource());
    });
  }

  private static void setOutgoingEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getHash());
      setOutgoingEdgesId(edge.getTarget());
    });
  }
}
