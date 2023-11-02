package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
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
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframePublicationsInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.springframework.stereotype.Component;

@Log4j2
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
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME.getValue(), EAN_VALUE.getValue(), LOCAL_ID_VALUE.getValue()))
        .type(toType(ir, TypeEnum::fromValue, TypeEnum.class)))
      .toList();
  }

  private <E extends Enum<E>> E toType(Resource resource, Function<String, E> typeSupplier, Class<E> enumClass) {
    var typeUri = resource.getFirstType().getUri();
    typeUri = typeUri.substring(typeUri.lastIndexOf("/") + 1);
    E result = null;
    try {
      result = typeSupplier.apply(typeUri);
    } catch (IllegalArgumentException iae) {
      var enumNameWithParent = enumClass.getName().substring(enumClass.getName().lastIndexOf(".") + 1);
      log.error("Unknown type [{}] of [{}] was ignored during Resource [id = {}] conversion to BibframeIndex message",
        typeUri, enumNameWithParent, resource.getResourceHash());
    }
    return result;
  }

  private String getValue(JsonNode doc, String... values) {
    for (String value : values) {
      if (doc.has(value)) {
        return doc.get(value).get(0).asText();
      }
    }
    return null;
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> CREATOR.getUri().equals(re.getPredicate().getUri())
        || CONTRIBUTOR.getUri().equals(re.getPredicate().getUri()))
      .map(re -> new BibframeContributorsInner()
        .name(getValue(re.getTarget().getDoc(), NAME.getValue()))
        .type(toType(re.getTarget(), BibframeContributorsInner.TypeEnum::fromValue,
          BibframeContributorsInner.TypeEnum.class))
        .isCreator(CREATOR.getUri().equals(re.getPredicate().getUri()))
      )
      .toList();
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
