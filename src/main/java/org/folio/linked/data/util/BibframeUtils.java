package org.folio.linked.data.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.model.ResourceType;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeIndex;

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

  public static boolean isSameResource(BibframeIndex index, Resource resource) {
    if (nonNull(index) && nonNull(resource) && nonNull(resource.getId())) {
      return String.valueOf(resource.getId()).equals(index.getId());
    } else {
      return false;
    }
  }

  public static Optional<Resource> extractWork(Resource resource) {
    if (isOfType(resource, WORK)) {
      return Optional.of(resource);
    }
    return resource.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(resourceEdge -> {
        var work = resourceEdge.getTarget();
        work.addIncomingEdge(resourceEdge);
        return work;
      })
      .findFirst();
  }

  public static List<Resource> extractInstances(Resource resource) {
    if (isOfType(resource, INSTANCE)) {
      return List.of(resource);
    }
    return resource.getIncomingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(resourceEdge -> {
        var instance = resourceEdge.getTarget();
        instance.addOutgoingEdge(resourceEdge);
        return instance;
      })
      .toList();
  }

}
