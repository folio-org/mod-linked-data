package org.folio.linked.data.util;

import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

@Log4j2
@UtilityClass
public class ResourceUtils {

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

  public static Optional<Resource> extractWorkFromInstance(Resource resource) {
    if (resource.isOfType(WORK)) {
      return Optional.of(resource);
    }
    if (resource.isNotOfType(INSTANCE)) {
      return empty();
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

  public static List<Resource> extractInstancesFromWork(Resource resource) {
    if (resource.isOfType(INSTANCE)) {
      return List.of(resource);
    }
    if (resource.isNotOfType(WORK)) {
      return emptyList();
    }
    return resource.getIncomingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(resourceEdge -> {
        var instance = resourceEdge.getSource();
        instance.addOutgoingEdge(resourceEdge);
        return instance;
      })
      .toList();
  }

  public static Resource ensureLatestReplaced(Resource resource) {
    return resource.getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getUri().equals(REPLACED_BY.getUri()))
      .map(ResourceEdge::getTarget)
      .map(ResourceUtils::ensureLatestReplaced)
      .findFirst()
      .orElse(resource);
  }

  public static List<String> getPrimaryMainTitles(List<TitleFieldRequestTitleInner> titles) {
    if (isNull(titles)) {
      return new ArrayList<>();
    }
    return titles.stream()
      .filter(PrimaryTitleField.class::isInstance)
      .map(PrimaryTitleField.class::cast)
      .map(PrimaryTitleField::getPrimaryTitle)
      .map(pt -> join(" ", getFirstValue(pt::getMainTitle), getFirstValue(pt::getSubTitle)))
      .map(String::trim)
      .toList();
  }

  public static void setPreferred(Resource resource, boolean preferred) {
    if (isNull(resource.getDoc())) {
      resource.setDoc(JsonNodeFactory.instance.objectNode());
    }
    var arrayNode = JsonNodeFactory.instance.arrayNode().add(String.valueOf(preferred));
    ((ObjectNode) resource.getDoc()).set(RESOURCE_PREFERRED.getValue(), arrayNode);
  }

  public static boolean isPreferred(Resource resource) {
    return ofNullable(resource.getDoc())
      .flatMap(doc -> JsonUtils.getProperty(doc, RESOURCE_PREFERRED.getValue()))
      .filter(JsonNode::isArray)
      .map(value -> value.get(0).asText())
      .filter(value -> value.equals("true"))
      .isPresent();
  }

  public static JsonNode copyWithoutPreferred(Resource subject) {
    return ofNullable(subject.getDoc())
      .map(node -> {
        var copiedDoc = node.deepCopy();
        if (copiedDoc.isObject()) {
          ((ObjectNode) copiedDoc).remove(RESOURCE_PREFERRED.getValue());
        }
        return copiedDoc;
      })
      .orElse(null);
  }
}
