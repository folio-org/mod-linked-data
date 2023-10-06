package org.folio.linked.data.mapper;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
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
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeIndex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Log4j2
@Mapper(componentModel = SPRING)
public abstract class ResourceMapper {

  private static final Map<Class<? extends ResourceField>, String> DTO_CLASS_TO_TYPE = new HashMap<>();
  @Autowired
  private InnerResourceMapper innerMapper;
  @Autowired
  private KafkaMessageMapper kafkaMessageMapper;

  static {
    DTO_CLASS_TO_TYPE.put(InstanceField.class, INSTANCE);
  }

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

  public BibframeIndex mapToIndex(@NonNull Resource resource) {
    return kafkaMessageMapper.toIndex(resource);
  }

  public static void setEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getPredicateHash());
      setEdgesId(edge.getTarget());
    });
  }

}
