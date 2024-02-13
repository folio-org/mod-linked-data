package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.cleanDate;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeClassificationsInner;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframeInstancesInner;
import org.folio.search.domain.dto.BibframeInstancesInnerEditionStatementsInner;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.folio.search.domain.dto.BibframeInstancesInnerPublicationsInner;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.search.domain.dto.BibframeSubjectsInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaMessageMapperImpl implements KafkaMessageMapper {

  private static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [id = {}] conversion to BibframeIndex message";
  private final SingleResourceMapper singleResourceMapper;

  @Override
  public Optional<BibframeIndex> toIndex(@NonNull Resource resource) {
    var result = extractWork(resource)
      .map(work -> {
        var workIndex = new BibframeIndex(work.getResourceHash().toString());
        workIndex.setTitles(extractTitles(work));
        workIndex.setContributors(extractContributors(work));
        workIndex.setLanguages(extractLanguages(work));
        workIndex.setClassifications(extractClassifications(work));
        workIndex.setSubjects(extractSubjects(work));
        workIndex.setInstances(extractInstances(resource));
        return shouldBeIndexed(workIndex) ? workIndex : null;
      });
    if (result.isEmpty()) {
      log.warn("Only Monograph Work is supported, and there is no Work found");
    }
    return result;
  }

  private boolean shouldBeIndexed(BibframeIndex bi) {
    return isNotEmpty(bi.getTitles())
      || isNotEmpty(bi.getContributors())
      || isNotEmpty(bi.getLanguages())
      || isNotEmpty(bi.getClassifications())
      || isNotEmpty(bi.getSubjects())
      || isNotEmpty(bi.getInstances());
  }

  private Optional<Resource> extractWork(Resource resource) {
    return resource.getTypes().stream().anyMatch(t -> t.getUri().equals(WORK.getUri())) ? Optional.of(resource) :
      resource.getOutgoingEdges().stream()
        .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
        .map(ResourceEdge::getTarget)
        .findFirst();
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
      .distinct()
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

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> CREATOR.getUri().equals(re.getPredicate().getUri())
        || CONTRIBUTOR.getUri().equals(re.getPredicate().getUri()))
      .map(re -> new BibframeContributorsInner()
        .name(getValue(re.getTarget().getDoc(), NAME.getValue()))
        .type(toType(re.getTarget(), BibframeContributorsInner.TypeEnum::fromValue,
          BibframeContributorsInner.TypeEnum.class, re.getPredicate(), Work.class))
        .isCreator(CREATOR.getUri().equals(re.getPredicate().getUri()))
      )
      .filter(ic -> nonNull(ic.getName()))
      .distinct()
      .toList();
  }

  private List<BibframeLanguagesInner> extractLanguages(Resource work) {
    return getPropertyValues(work.getDoc(), LANGUAGE.getValue())
      .map(p -> new BibframeLanguagesInner().value(p))
      .toList();
  }

  private Stream<String> getPropertyValues(JsonNode doc, String... properties) {
    return ofNullable(doc)
      .stream()
      .flatMap(d -> Arrays.stream(properties)
        .filter(p -> d.has(p) && !d.get(p).isEmpty())
        .flatMap(p -> StreamSupport.stream(doc.get(p).spliterator(), true).map(JsonNode::asText)));
  }

  private List<BibframeClassificationsInner> extractClassifications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> CLASSIFICATION.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(tr -> new BibframeClassificationsInner()
        .number(getValue(tr.getDoc(), CODE.getValue()))
        .source(getValue(tr.getDoc(), SOURCE.getValue())))
      .filter(bci -> nonNull(bci.getNumber()))
      .distinct()
      .toList();
  }

  private List<BibframeSubjectsInner> extractSubjects(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> SUBJECT.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(tr -> new BibframeSubjectsInner()
        .value(tr.getLabel()))
      .filter(bci -> nonNull(bci.getValue()))
      .distinct()
      .toList();
  }

  private List<BibframeInstancesInner> extractInstances(Resource resource) {
    return (resource.getTypes().stream().anyMatch(t -> t.getUri().equals(INSTANCE.getUri())) ? Stream.of(resource)
      : resource.getIncomingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getSource))
      .map(ir -> new BibframeInstancesInner()
        .id(String.valueOf(ir.getResourceHash()))
        .titles(extractTitles(ir))
        .identifiers(extractIdentifiers(ir))
        .contributors(extractContributors(ir))
        .publications(extractPublications(ir))
        .editionStatements(getPropertyValues(ir.getDoc(), EDITION_STATEMENT.getValue())
          .map(es -> new BibframeInstancesInnerEditionStatementsInner().value(es)).toList()))
      .filter(bii -> isNotEmpty(bii.getTitles()) || isNotEmpty(bii.getIdentifiers())
        || isNotEmpty(bii.getContributors()) || isNotEmpty(bii.getPublications())
        || isNotEmpty(bii.getEditionStatements()))
      .distinct()
      .toList();
  }

  private List<BibframeInstancesInnerIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeInstancesInnerIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME.getValue(), EAN_VALUE.getValue(), LOCAL_ID_VALUE.getValue()))
        .type(toType(ir, TypeEnum::fromValue, TypeEnum.class, MAP, Instance.class)))
      .filter(identifier -> nonNull(identifier.getValue()))
      .distinct()
      .toList();
  }

  private <E extends Enum<E>> E toType(Resource resource, Function<String, E> typeSupplier, Class<E> enumClass,
                                       Predicate predicate, Class<?> parentDto) {
    if (isNull(resource.getTypes())) {
      return null;
    }
    return resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .filter(type -> singleResourceMapper.getMapperUnit(type, predicate, parentDto, null).isPresent())
      .findFirst()
      .map(typeUri -> typeUri.substring(typeUri.lastIndexOf("/") + 1))
      .map(typeUri -> {
        try {
          return typeSupplier.apply(typeUri);
        } catch (IllegalArgumentException ignored) {
          return null;
        }
      })
      .orElseGet(() -> {
        var enumNameWithParent = getTypeEnumNameWithParent(enumClass);
        log.warn(MSG_UNKNOWN_TYPES,
          resource.getTypes().stream().map(ResourceTypeEntity::getUri).collect(joining(", ")),
          enumNameWithParent, resource.getResourceHash());
        return null;
      });
  }

  @NotNull
  private <E extends Enum<E>> String getTypeEnumNameWithParent(Class<E> enumClass) {
    return enumClass.getName().substring(enumClass.getName().lastIndexOf(".") + 1);
  }

  private String getValue(JsonNode doc, String... values) {
    if (nonNull(doc)) {
      for (String value : values) {
        if (doc.has(value) && !doc.get(value).isEmpty()) {
          return doc.get(value).get(0).asText();
        }
      }
    }
    return null;
  }

  private List<BibframeInstancesInnerPublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PE_PUBLICATION.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeInstancesInnerPublicationsInner()
        .name(getValue(ir.getDoc(), NAME.getValue()))
        .date(cleanDate(getValue(ir.getDoc(), DATE.getValue(), PROVIDER_DATE.getValue()))))
      .filter(ip -> nonNull(ip.getName()) || nonNull(ip.getDate()))
      .distinct()
      .toList();
  }

}
