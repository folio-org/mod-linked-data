package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
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
    bibframeIndex.setEditionStatement(getValue(instance.getDoc(), EDITION_STATEMENT.getValue()));
    return bibframeIndex;
  }

  private Resource extractInstance(Resource resource) {
    return resource.getTypes().stream().anyMatch(t -> t.getUri().equals(INSTANCE.getUri())) ? resource :
      resource.getOutgoingEdges().stream()
        .filter(re -> INSTANCE.getUri().equals(re.getPredicate().getUri()))
        .map(ResourceEdge::getTarget)
        .findFirst()
        .orElseThrow(() -> new NotSupportedException("Only Monograph.Instance bibframe is supported for now, and there "
          + "is no Instance found"));
  }

  private List<BibframeTitlesInner> extractTitles(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> TITLE.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .flatMap(t -> {
        var titles = new ArrayList<BibframeTitlesInner>();
        addTitle(t, MAIN_TITLE.getValue(), titles, BibframeTitlesInner.TypeEnum.MAIN);
        addTitle(t, SUBTITLE.getValue(), titles, BibframeTitlesInner.TypeEnum.SUB);
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
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .filter(r -> stream(TypeEnum.values())
        .anyMatch(typeEnum -> r.getFirstType().getUri().contains(typeEnum.getValue())))
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME.getValue(), EAN_VALUE.getValue(), LOCAL_ID_VALUE.getValue()))
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

  private TypeEnum toTypeEnum(ResourceTypeEntity resourceType) {
    var typeUri = resourceType.getUri();
    var extractedTypeWord = typeUri.substring(typeUri.lastIndexOf("/") + 1);
    return TypeEnum.fromValue(extractedTypeWord);
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return new ArrayList<>(); // Not supported at the moment
  }

  private List<BibframePublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PE_PUBLICATION.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframePublicationsInner()
        .publisher(getValue(ir.getDoc(), NAME.getValue()))
        .dateOfPublication(getValue(ir.getDoc(), DATE.getValue(), PROVIDER_DATE.getValue())))
      .toList();
  }

}
