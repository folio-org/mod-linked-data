package org.folio.linked.data.mapper.resource.kafka;

import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_DATE;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframePublicationsInner;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageMapperImpl implements KafkaMessageMapper {

  @Override
  public BibframeIndex toIndex(@NonNull Resource resource) {
    var instance = extractInstance(resource);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    bibframeIndex.setTitle(instance.getLabel());
    bibframeIndex.setIdentifiers(extractIdentifiers(instance));
    bibframeIndex.setContributors(extractContributors(instance));
    bibframeIndex.setPublications(extractPublications(instance));
    bibframeIndex.setEditionStatement(getValue(instance.getDoc(), EDITION_STATEMENT));
    return bibframeIndex;
  }

  private Resource extractInstance(Resource resource) {
    return resource.getTypes().stream().anyMatch(t -> t.getTypeUri().equals(INSTANCE)) ? resource :
      resource.getOutgoingEdges().stream()
        .filter(re -> INSTANCE.equals(re.getPredicate().getLabel()))
        .map(ResourceEdge::getTarget)
        .findFirst()
        .orElseThrow(() -> new NotSupportedException("Only Monograph.Instance bibframe is supported for now, and there "
          + "is no Instance found"));
  }

  private List<BibframeIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> MAP_PRED.equals(re.getPredicate().getLabel()))
      .filter(re -> {
        var typeUri = re.getTarget().getFirstType().getTypeUri();
        return Arrays.stream(BibframeIdentifiersInner.TypeEnum.values())
          .anyMatch(typeEnum -> typeUri.contains(typeEnum.getValue())
            // to be removed after mod-search-ld DTO alignment
            || typeUri.contains("Ean")
            || typeUri.contains("LocalId")
            || typeUri.contains("UNKNOWN"));
      })
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME, EAN_VALUE, LOCAL_ID_VALUE))
        .type(toTypeEnum(ir.getFirstType())))
      .toList();
  }

  private String getValue(JsonNode doc, String... values) {
    for (String value : values) {
      if (doc.has(value)) {
        return doc.get(value).get(0).textValue();
      }
    }
    return null;
  }

  private BibframeIdentifiersInner.TypeEnum toTypeEnum(ResourceType resourceType) {
    var typeUri = resourceType.getTypeUri();
    var extractedTypeWord = typeUri.substring(typeUri.lastIndexOf("/") + 1);
    var reworded = extractedTypeWord
      // to be removed after mod-search-ld DTO alignment
      .replace("Ean", "EAN")
      .replace("LocalId", "Local")
      .replace("UNKNOWN", "Other");
    return BibframeIdentifiersInner.TypeEnum.fromValue(reworded);
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return new ArrayList<>(); // Not supported at the moment
  }

  private List<BibframePublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PUBLICATION_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframePublicationsInner()
        .publisher(getValue(ir.getDoc(), NAME))
        .dateOfPublication(getValue(ir.getDoc(), DATE, PROVIDER_DATE)))
      .toList();
  }

}
