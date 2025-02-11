package org.folio.linked.data.service.resource.copy;

import static java.util.Collections.emptySet;
import static org.folio.ld.dictionary.PropertyDictionary.CITATION_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.CREDITS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATES_OF_PUBLICATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.GOVERNING_ACCESS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_ORIGINALS_DUPLICATES;
import static org.folio.ld.dictionary.PropertyDictionary.OTHER_EVENT_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.PARTICIPANT_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PUBLICATION_FREQUENCY;
import static org.folio.ld.dictionary.PropertyDictionary.REFERENCES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResourceCopyServiceImpl implements ResourceCopyService {

  private static final Map<String, Set<String>> PROPERTIES_TO_BE_COPIED = Map.of(
    INSTANCE.getUri(), Set.of(
      PUBLICATION_FREQUENCY.getValue(),
      DATES_OF_PUBLICATION_NOTE.getValue(),
      GOVERNING_ACCESS_NOTE.getValue(),
      CREDITS_NOTE.getValue(),
      PARTICIPANT_NOTE.getValue(),
      CITATION_COVERAGE.getValue(),
      LOCATION_OF_ORIGINALS_DUPLICATES.getValue()
    ),
    WORK.getUri(), Set.of(
      REFERENCES.getValue(),
      OTHER_EVENT_INFORMATION.getValue(),
      GEOGRAPHIC_COVERAGE.getValue()
    )
  );

  private final ResourceEdgeService resourceEdgeService;
  private final ObjectMapper objectMapper;

  @Override
  public void copyEdgesAndProperties(Resource old, Resource updated) {
    resourceEdgeService.copyOutgoingEdges(old, updated);
    copyUnmappedMarc(old, updated);
    copyProperties(old, updated);
  }

  private void copyUnmappedMarc(Resource from, Resource to) {
    if (to.isOfType(INSTANCE)) {
      Optional.ofNullable(from.getUnmappedMarc())
        .ifPresent(unmappedMarc -> {
          var newUnmappedMarc = new RawMarc(to).setContent(unmappedMarc.getContent());
          to.setUnmappedMarc(newUnmappedMarc);
        });
    }
  }

  @SneakyThrows
  private void copyProperties(Resource from, Resource to) {
    if (from.getDoc() == null) {
      return;
    }
    var properties = getProperties(from);
    if (!properties.isEmpty()) {
      var toDoc = to.getDoc() == null
        ? new HashMap<String, List<String>>()
        : objectMapper.treeToValue(to.getDoc(), new TypeReference<HashMap<String, List<String>>>() {});
      properties.forEach(entry -> toDoc.put(entry.getKey(), entry.getValue()));
      to.setDoc(objectMapper.convertValue(toDoc, JsonNode.class));
    }
  }

  private List<Map.Entry<String, List<String>>> getProperties(Resource from) throws JsonProcessingException {
    var fromDoc = objectMapper.treeToValue(from.getDoc(), new TypeReference<HashMap<String, List<String>>>() {});
    var fromType = from.getTypes()
      .stream()
      .findFirst()
      .orElseThrow()
      .getUri();
    return fromDoc.entrySet()
      .stream()
      .filter(entry -> PROPERTIES_TO_BE_COPIED.getOrDefault(fromType, emptySet()).contains(entry.getKey()))
      .toList();
  }
}
