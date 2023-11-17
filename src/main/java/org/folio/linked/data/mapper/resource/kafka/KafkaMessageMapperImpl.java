package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
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
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframePublicationsInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaMessageMapperImpl implements KafkaMessageMapper {

  private static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [id = {}] conversion to BibframeIndex message";
  private final SubResourceMapper subResourceMapper;

  @NotNull
  private static <E extends Enum<E>> String getTypeEnumNameWithParent(Class<E> enumClass) {
    return enumClass.getName().substring(enumClass.getName().lastIndexOf(".") + 1);
  }

  @Override
  public Optional<BibframeIndex> toIndex(@NonNull Resource resource) {
    var instance = extractInstance(resource);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    bibframeIndex.setTitles(extractTitles(instance));
    bibframeIndex.setIdentifiers(extractIdentifiers(instance));
    bibframeIndex.setContributors(extractContributors(instance));
    bibframeIndex.setPublications(extractPublications(instance));
    bibframeIndex.setEditionStatement(getValue(instance.getDoc(), EDITION_STATEMENT.getValue()));
    return shouldBeIndexed(bibframeIndex) ? Optional.of(bibframeIndex) : Optional.empty();
  }

  private boolean shouldBeIndexed(BibframeIndex bi) {
    return isNotEmpty(bi.getTitles())
      || isNotEmpty(bi.getIdentifiers())
      || isNotEmpty(bi.getContributors())
      || isNotEmpty(bi.getPublications())
      || isNotBlank(bi.getEditionStatement());
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

  private List<BibframeIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> MAP.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), NAME.getValue(), EAN_VALUE.getValue(), LOCAL_ID_VALUE.getValue()))
        .type(toType(ir, TypeEnum::fromValue, TypeEnum.class, MAP, Instance.class)))
      .filter(ri -> nonNull(ri.getValue()))
      .distinct()
      .toList();
  }

  private <E extends Enum<E>> E toType(Resource resource, Function<String, E> typeSupplier, Class<E> enumClass,
                                       Predicate predicate, Class parentDto) {
    if (isNull(resource.getTypes())) {
      return null;
    }
    return resource.getTypes()
      .stream()
      .map(ResourceTypeEntity::getUri)
      .filter(type -> subResourceMapper.getMapperUnit(type, predicate, parentDto, null).isPresent())
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

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .flatMap(re -> re.getTarget().getOutgoingEdges().stream())
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

  private List<BibframePublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PE_PUBLICATION.getUri().equals(re.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframePublicationsInner()
        .publisher(getValue(ir.getDoc(), NAME.getValue()))
        .dateOfPublication(getValue(ir.getDoc(), DATE.getValue(), PROVIDER_DATE.getValue())))
      .filter(ip -> nonNull(ip.getPublisher()) || nonNull(ip.getDateOfPublication()))
      .distinct()
      .toList();
  }

}
