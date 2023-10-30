package org.folio.linked.data.mapper;

import static java.util.stream.Collectors.toSet;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceField;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapper;
import org.folio.linked.data.mapper.resource.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.folio.search.domain.dto.BibframeIndex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Log4j2
@Mapper(componentModel = SPRING)
public abstract class ResourceMapper {

  private static final Map<Class<? extends ResourceField>, String> DTO_CLASS_TO_TYPE = new HashMap<>();

  static {
    DTO_CLASS_TO_TYPE.put(InstanceField.class, INSTANCE.getUri());
  }

  @Autowired
  private InnerResourceMapper innerMapper;
  @Autowired
  private KafkaMessageMapper kafkaMessageMapper;

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "type", expression = "java(resourceShortInfo.getFirstType().getUri())")
  public abstract ResourceShort map(ResourceShortInfo resourceShortInfo);

  public abstract ResourceShortInfoPage map(Page<ResourceShort> page);

  public Resource toEntity(org.folio.marc2ld.model.Resource marc2ldResource) {
    Resource resource = new Resource();
    resource.setLabel(marc2ldResource.getLabel());
    resource.setDoc(marc2ldResource.getDoc());
    resource.setResourceHash(marc2ldResource.getResourceHash());
    resource.setTypes(marc2ldResource.getTypes().stream().map(this::toEntity).collect(toSet()));
    resource.setOutgoingEdges(marc2ldResource.getOutgoingEdges().stream().map(marc2ldEdge -> toEntity(marc2ldEdge,
      resource)).collect(toSet()));
    return resource;
  }

  public abstract ResourceTypeEntity toEntity(ResourceTypeDictionary dictionary);

  public ResourceEdge toEntity(org.folio.marc2ld.model.ResourceEdge marc2ldEdge, Resource source) {
    var edge = new ResourceEdge();
    edge.setId(new ResourceEdgePk(marc2ldEdge.getSource().getResourceHash(),
      marc2ldEdge.getTarget().getResourceHash(), marc2ldEdge.getPredicate().getHash()));
    edge.setSource(source);
    edge.setTarget(toEntity(marc2ldEdge.getTarget()));
    edge.setPredicate(toEntity(marc2ldEdge.getPredicate()));
    return edge;
  }

  public abstract PredicateEntity toEntity(PredicateDictionary dictionary);

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

  public BibframeIndex mapToIndex(@NonNull Resource resource) {
    return kafkaMessageMapper.toIndex(resource);
  }

  private void setEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getHash());
      setEdgesId(edge.getTarget());
    });
  }

}
