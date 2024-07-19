package org.folio.linked.data.mapper.kafka.search;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.BibframeUtils.cleanDate;
import static org.folio.linked.data.util.Constants.MSG_UNKNOWN_TYPES;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.MAIN;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.MAIN_PARALLEL;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.MAIN_VARIANT;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.SUB;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.SUB_PARALLEL;
import static org.folio.search.domain.dto.LinkedDataWorkIndexTitleType.SUB_VARIANT;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeClassificationsInner;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeInstancesInner;
import org.folio.search.domain.dto.BibframeInstancesInnerEditionStatementsInner;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.folio.search.domain.dto.BibframeInstancesInnerPublicationsInner;
import org.folio.search.domain.dto.BibframeLanguagesInner;
import org.folio.search.domain.dto.BibframeSubjectsInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.folio.search.domain.dto.LinkedDataWork;
import org.folio.search.domain.dto.LinkedDataWorkIndexTitleType;
import org.folio.search.domain.dto.ResourceIndexEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@Mapper(componentModel = SPRING)
public abstract class BibliographicSearchMessageMapper {

  @Autowired
  private IndexIdentifierMapper<BibframeInstancesInnerIdentifiersInner> innerIndexIdentifierMapper;
  @Autowired
  private SingleResourceMapper singleResourceMapper;

  @Mapping(target = "resourceName", constant = SEARCH_RESOURCE_NAME)
  @Mapping(target = "_new", expression = "java(toLinkedDataWork(resource))")
  public abstract ResourceIndexEvent toIndex(Resource resource);

  @Mapping(target = "titles", source = "resource")
  @Mapping(target = "contributors", source = "resource")
  @Mapping(target = "languages", source = "resource")
  @Mapping(target = "classifications", source = "resource")
  @Mapping(target = "subjects", source = "resource")
  @Mapping(target = "instances", source = "resource")
  protected abstract LinkedDataWork toLinkedDataWork(Resource resource);

  protected List<BibframeTitlesInner> extractTitles(Resource resource) {
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

  protected void addTitle(Resource t, PropertyDictionary field, List<BibframeTitlesInner> titles) {
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
  protected ResourceTypeDictionary getTitleType(Resource title) {
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
  protected LinkedDataWorkIndexTitleType getIndexTitleType(ResourceTypeDictionary type, PropertyDictionary property) {
    var isMain = property.equals(MAIN_TITLE);
    return switch (type) {
      case TITLE -> isMain ? MAIN : SUB;
      case PARALLEL_TITLE -> isMain ? MAIN_PARALLEL : SUB_PARALLEL;
      case VARIANT_TITLE -> isMain ? MAIN_VARIANT : SUB_VARIANT;
      default -> null;
    };
  }

  protected List<BibframeContributorsInner> extractContributors(Resource resource) {
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

  protected List<BibframeLanguagesInner> extractLanguages(Resource work) {
    return work.getOutgoingEdges()
      .stream()
      .filter(re -> LANGUAGE.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(Resource::getDoc)
      .flatMap(d -> getPropertyValues(d, CODE.getValue()))
      .map(pv -> new BibframeLanguagesInner().value(pv))
      .toList();
  }

  protected Stream<String> getPropertyValues(JsonNode doc, String... properties) {
    return ofNullable(doc)
      .stream()
      .flatMap(d -> Arrays.stream(properties)
        .filter(p -> d.has(p) && !d.get(p).isEmpty())
        .flatMap(p -> StreamSupport.stream(doc.get(p).spliterator(), true).map(JsonNode::asText)));
  }

  protected List<BibframeClassificationsInner> extractClassifications(Resource resource) {
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

  protected List<BibframeSubjectsInner> extractSubjects(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> SUBJECT.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(tr -> new BibframeSubjectsInner()
        .value(tr.getLabel()))
      .filter(bci -> nonNull(bci.getValue()))
      .distinct()
      .toList();
  }

  protected List<BibframeInstancesInner> extractInstances(Resource resource) {
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
        .identifiers(innerIndexIdentifierMapper.extractIdentifiers(ir))
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

  protected List<BibframeInstancesInnerPublicationsInner> extractPublications(Resource resource) {
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

  protected String getValue(JsonNode doc, String... values) {
    if (nonNull(doc)) {
      for (String value : values) {
        if (doc.has(value) && !doc.get(value).isEmpty()) {
          return doc.get(value).get(0).asText();
        }
      }
    }
    return null;
  }

  protected <E extends Enum<E>> E toType(Resource resource,
                                         Function<String, E> typeSupplier,
                                         Class<E> enumClass,
                                         Predicate predicate,
                                         Class<?> parentDto) {
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
          enumNameWithParent, resource.getId());
        return null;
      });
  }

  @NotNull
  protected <E extends Enum<E>> String getTypeEnumNameWithParent(Class<E> enumClass) {
    return enumClass.getName().substring(enumClass.getName().lastIndexOf(".") + 1);
  }
}
