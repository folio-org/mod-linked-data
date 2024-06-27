package org.folio.linked.data.mapper.kafka.impl;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
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
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN;
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN_PARALLEL;
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN_VARIANT;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB_PARALLEL;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB_VARIANT;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.ResourceIndexEventType.DELETE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.LinkedDataServiceException;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeClassificationsInner;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframeIndexTitleType;
import org.folio.search.domain.dto.BibframeInstancesInner;
import org.folio.search.domain.dto.BibframeInstancesInnerEditionStatementsInner;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.folio.search.domain.dto.BibframeInstancesInnerPublicationsInner;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.search.domain.dto.BibframeSubjectsInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class KafkaSearchMessageBibframeMapper
  extends AbstractKafkaMessageMapper<BibframeIndex, BibframeInstancesInnerIdentifiersInner> {

  private static final String NO_INDEXABLE_WORK_FOUND =
    "No index-able work found for [{}] operation of the resource [{}]";
  private static final String NOT_A_WORK = "Not a Work resource [%s] has been passed to indexation for [%s] operation";

  @Autowired
  public KafkaSearchMessageBibframeMapper(SingleResourceMapper singleResourceMapper) {
    super(singleResourceMapper);
  }

  @Override
  public Optional<BibframeIndex> toIndex(Resource work, ResourceIndexEventType eventType) {
    if (isNull(work)) {
      log.warn(NO_INDEXABLE_WORK_FOUND, eventType.getValue(), "null");
      return empty();
    }
    if (!work.isOfType(WORK)) {
      throw new LinkedDataServiceException(format(NOT_A_WORK, work, eventType.getValue()));
    }
    var workIndex = new BibframeIndex(String.valueOf(work.getId()));
    workIndex.setTitles(extractTitles(work));
    workIndex.setContributors(extractContributors(work));
    workIndex.setLanguages(extractLanguages(work));
    workIndex.setClassifications(extractClassifications(work));
    workIndex.setSubjects(extractSubjects(work));
    workIndex.setInstances(extractInstances(work));
    if (shouldBeIndexed(workIndex)) {
      return of(workIndex);
    } else {
      log.warn(NO_INDEXABLE_WORK_FOUND, eventType.getValue(), work);
      return empty();
    }
  }

  @Override
  public Optional<Long> toDeleteIndexId(@NonNull Resource work) {
    if (!work.isOfType(WORK)) {
      throw new LinkedDataServiceException(format(NOT_A_WORK, work, DELETE.getValue()));
    }
    return ofNullable(work.getId());
  }

  private boolean shouldBeIndexed(BibframeIndex bi) {
    return isNotEmpty(bi.getTitles())
      || isNotEmpty(bi.getContributors())
      || isNotEmpty(bi.getLanguages())
      || isNotEmpty(bi.getClassifications())
      || isNotEmpty(bi.getSubjects())
      || isNotEmpty(bi.getInstances());
  }

  private List<BibframeTitlesInner> extractTitles(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> TITLE.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .flatMap(t -> {
        var titles = new ArrayList<BibframeTitlesInner>();
        addTitle(t, MAIN_TITLE, titles);
        addTitle(t, SUBTITLE, titles);
        return titles.stream();
      })
      .distinct()
      .toList();
  }

  private void addTitle(Resource t, PropertyDictionary field, List<BibframeTitlesInner> titles) {
    var titleText = getValue(t.getDoc(), field.getValue());
    if (nonNull(titleText)) {
      var titleType = getTitleType(t);
      ofNullable(titleType)
        .map(type -> getIndexTitleType(type, field))
        .map(indexTitleType -> new BibframeTitlesInner().value(titleText).type(indexTitleType))
        .ifPresent(titles::add);
    }
  }

  @Nullable
  private ResourceTypeDictionary getTitleType(Resource title) {
    var typeUris = title.getTypes().stream().map(ResourceTypeEntity::getUri).toList();
    if (typeUris.stream().anyMatch(uri -> ResourceTypeDictionary.TITLE.getUri().equals(uri))) {
      return ResourceTypeDictionary.TITLE;
    } else if (typeUris.stream().anyMatch(uri -> ResourceTypeDictionary.PARALLEL_TITLE.getUri().equals(uri))) {
      return ResourceTypeDictionary.PARALLEL_TITLE;
    } else if (typeUris.stream().anyMatch(uri -> ResourceTypeDictionary.VARIANT_TITLE.getUri().equals(uri))) {
      return ResourceTypeDictionary.VARIANT_TITLE;
    }
    return null;
  }

  @Nullable
  private BibframeIndexTitleType getIndexTitleType(ResourceTypeDictionary type, PropertyDictionary property) {
    var isMain = property.equals(MAIN_TITLE);
    return switch (type) {
      case TITLE -> isMain ? MAIN : SUB;
      case PARALLEL_TITLE -> isMain ? MAIN_PARALLEL : SUB_PARALLEL;
      case VARIANT_TITLE -> isMain ? MAIN_VARIANT : SUB_VARIANT;
      default -> null;
    };
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> CREATOR.getUri().equals(re.getPredicate().getUri())
        || CONTRIBUTOR.getUri().equals(re.getPredicate().getUri()))
      .map(re -> new BibframeContributorsInner()
        .name(getValue(re.getTarget().getDoc(), NAME.getValue()))
        .type(toType(re.getTarget(), BibframeContributorsInner.TypeEnum::fromValue,
          BibframeContributorsInner.TypeEnum.class, re.getPredicate(), WorkResponse.class))
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
    var workStream = resource.isOfType(INSTANCE) ? resource.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget) : Stream.of(resource);
    return workStream
      .flatMap(work -> work.getIncomingEdges().stream()
        .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
        .map(ResourceEdge::getSource))
      .map(ir -> new BibframeInstancesInner()
        .id(String.valueOf(ir.getId()))
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

  @Override
  Optional<BibframeInstancesInnerIdentifiersInner> mapToIdentifier(Resource resource) {
    var value = getValue(resource.getDoc(), NAME.getValue(), EAN_VALUE.getValue(), LOCAL_ID_VALUE.getValue());
    var type = toType(resource, TypeEnum::fromValue, TypeEnum.class, MAP, InstanceResponse.class);
    return Optional.of(new BibframeInstancesInnerIdentifiersInner())
      .map(i -> i.value(value))
      .map(i -> i.type(type))
      .filter(i -> nonNull(i.getValue()));
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
