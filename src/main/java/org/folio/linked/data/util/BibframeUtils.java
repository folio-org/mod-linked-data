package org.folio.linked.data.util;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID_AND_SRS_ID;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Identifiable;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;

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

  public static Resource ensureActive(Resource resource) {
    if (resource.isActive()) {
      return resource;
    }
    return resource.getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getUri().equals(REPLACED_BY.getUri()))
      .map(ResourceEdge::getTarget)
      .map(BibframeUtils::ensureActive)
      .findFirst()
      .orElse(resource);
  }

  public static Resource fetchResource(Identifiable identifiable, ResourceRepository resourceRepository) {
    Function<String, Optional<Resource>> fetchByIdFunction = id -> resourceRepository.findById(Long.parseLong(id));
    Function<String, Optional<Resource>> fetchBySrsIdFunction = resourceRepository::findByFolioMetadataSrsId;
    return Optional.ofNullable(identifiable.getId())
      .flatMap(fetchByIdFunction)
      .or(() -> Optional.ofNullable(identifiable.getSrsId())
        .flatMap(fetchBySrsIdFunction))
      .map(BibframeUtils::ensureActive)
      .map(Resource::copyWithNoEdges)
      .orElseThrow(() -> new NotFoundException(String.format("%s%s, %s%s",
        RESOURCE_WITH_GIVEN_ID_AND_SRS_ID, identifiable.getId(), identifiable.getSrsId(), IS_NOT_FOUND)));
  }

}
