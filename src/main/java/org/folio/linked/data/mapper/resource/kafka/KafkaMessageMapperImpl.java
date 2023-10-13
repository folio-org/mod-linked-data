package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_DATE;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
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
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageMapperImpl implements KafkaMessageMapper {

  @Override
  public BibframeIndex toIndex(@NonNull Resource resource) {
    var instance = extractInstance(resource);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    bibframeIndex.setTitles(extractTitles(instance));
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

  private List<BibframeTitlesInner> extractTitles(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> INSTANCE_TITLE_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .flatMap(t -> {
        var titles = new ArrayList<BibframeTitlesInner>();
        addTitle(t, MAIN_TITLE, titles, BibframeTitlesInner.TypeEnum.MAIN);
        addTitle(t, SUBTITLE, titles, BibframeTitlesInner.TypeEnum.SUB);
        return titles.stream();
      })
      .toList();
  }

  private void addTitle(Resource t, String field, List<BibframeTitlesInner> titles,
                        BibframeTitlesInner.TypeEnum type) {
    if (nonNull(getValue(t.getDoc(), field))) {
      titles.add(new BibframeTitlesInner()
        .value(getValue(t.getDoc(), field))
        .type(type));
    }
  }

  private List<BibframeIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> MAP_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .filter(r -> stream(TypeEnum.values())
        .anyMatch(typeEnum -> r.getFirstType().getTypeUri().contains(typeEnum.getValue())))
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME, EAN_VALUE, LOCAL_ID_VALUE))
        .type(toTypeEnum(ir.getFirstType())))
      .toList();
  }

  private String getValue(JsonNode doc, String... values) {
    for (String value : values) {
      if (doc.has(value)) {
        return doc.get(value).get(0).asText();
      }
    }
    return null;
  }

  private TypeEnum toTypeEnum(ResourceType resourceType) {
    var typeUri = resourceType.getTypeUri();
    var extractedTypeWord = typeUri.substring(typeUri.lastIndexOf("/") + 1);
    return TypeEnum.fromValue(extractedTypeWord);
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
