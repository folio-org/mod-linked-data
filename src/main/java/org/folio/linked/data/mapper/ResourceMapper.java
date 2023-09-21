package org.folio.linked.data.mapper;

import static org.apache.commons.lang3.StringUtils.firstNonEmpty;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.Bibframe2Short;
import org.folio.linked.data.domain.dto.Bibframe2ShortInfoPage;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceField;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.ProfiledMapper;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframePublicationsInner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Log4j2
@Mapper(componentModel = SPRING)
public abstract class ResourceMapper {

  private static final Map<Class<? extends ResourceField>, String> DTO_CLASS_TO_TYPE = new HashMap<>();
  @Autowired
  private ProfiledMapper profiledMapper;
  @Autowired
  private InnerResourceMapper innerMapper;
  @Autowired
  private KafkaMessageMapper kafkaMessageMapper;

  static {
    DTO_CLASS_TO_TYPE.put(InstanceField.class, INSTANCE);
  }

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "profile", expression = "java(resourceShortInfo.getFirstType().getSimpleLabel())")
  @Mapping(target = "type", expression = "java(resourceShortInfo.getFirstType().getTypeUri())")
  public abstract Bibframe2Short map2(ResourceShortInfo resourceShortInfo);

  public abstract Bibframe2ShortInfoPage map2(Page<Bibframe2Short> page);

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "type", expression = "java(resourceShortInfo.getFirstType().getTypeUri())")
  public abstract ResourceShort map(ResourceShortInfo resourceShortInfo);

  public abstract ResourceShortInfoPage map(Page<ResourceShort> page);

  @SneakyThrows
  public Resource toEntity(ResourceDto dto) {
    try {
      var resource = innerMapper.toEntity(dto.getResource(), DTO_CLASS_TO_TYPE.get(dto.getResource().getClass()));
      setEdgesId(resource);
      return resource;
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw new ValidationException(dto.getClass().getSimpleName(), dto.toString());
    }
  }

  public ResourceDto toDto(Resource resource) {
    return innerMapper.toDto(resource, new ResourceDto());
  }

  public Resource toEntity2(Bibframe2Request dto) {
    var resource = profiledMapper.toEntity(dto);
    setEdgesId(resource);
    return resource;
  }

  public Bibframe2Response toDto2(Resource resource) {
    return profiledMapper.toDto(resource);
  }

  public BibframeIndex mapToIndex(@NonNull Resource resource) {
    return kafkaMessageMapper.toIndex(resource);
  }

  public BibframeIndex mapToIndex2(@NonNull Resource resource) {
    var instance = extractInstance(resource);
    var bibframeIndex = new BibframeIndex(resource.getResourceHash().toString());
    bibframeIndex.setTitle(instance.getLabel());
    bibframeIndex.setIdentifiers(extractIdentifiers(instance));
    bibframeIndex.setContributors(extractContributors(instance));
    bibframeIndex.setPublications(extractPublications(instance));
    bibframeIndex.setEditionStatement(getValue(instance.getDoc(), EDITION_STATEMENT_URL));
    return bibframeIndex;
  }

  private Resource extractInstance(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> INSTANCE_URL.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .findFirst()
      .orElseThrow(() -> new NotSupportedException("Only Monograph.Instance bibframe is supported for now, and there "
        + "is no Instance found"));
  }

  private List<BibframeIdentifiersInner> extractIdentifiers(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> IDENTIFIED_BY_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .map(ir -> new BibframeIdentifiersInner()
        .value(getValue(ir.getDoc(), VALUE_PRED))
        .type(TypeEnum.fromValue(ir.getFirstType().getSimpleLabel().replace("lc:RT:bf2:Identifiers:",
          ""))))
      .toList();
  }

  private List<BibframeContributorsInner> extractContributors(Resource resource) {
    return new ArrayList<>(); // Lookup fields are not supported at the moment
  }

  private List<BibframePublicationsInner> extractPublications(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(re -> PROVISION_ACTIVITY_PRED.equals(re.getPredicate().getLabel()))
      .map(ResourceEdge::getTarget)
      .filter(r -> PUBLICATION.equals(r.getFirstType().getSimpleLabel()))
      .map(Resource::getDoc)
      .map(doc -> new BibframePublicationsInner()
        .dateOfPublication(firstNonEmpty(getValue(doc, SIMPLE_DATE_PRED), getValue(doc, DATE_URL)))
        .publisher(firstNonEmpty(getValue(doc, SIMPLE_AGENT_PRED), getValue(doc, SIMPLE_PLACE_PRED)))
      )
      .toList();
  }

  private String getValue(JsonNode doc, String value) {
    return doc.has(value)
      ? doc.get(value).get(0).textValue()
      : null;
  }

  private void setEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getPredicateHash());
      setEdgesId(edge.getTarget());
    });
  }

}
