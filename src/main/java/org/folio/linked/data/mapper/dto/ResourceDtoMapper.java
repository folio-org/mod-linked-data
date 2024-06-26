package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceEdgeDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Log4j2
@Mapper(componentModel = SPRING, imports = {Collectors.class, Arrays.class, ResourceTypeEntity.class})
public abstract class ResourceDtoMapper {

  @Autowired
  private SingleResourceMapper singleResourceMapper;

  @Mapping(target = "type", expression = "java(resourceShortInfo.getFirstType().getUri())")
  public abstract ResourceShort map(ResourceShortInfo resourceShortInfo);

  public abstract ResourceShortInfoPage map(Page<ResourceShort> page);

  @SneakyThrows
  public Resource toEntity(ResourceRequestDto dto) {
    try {
      return singleResourceMapper.toEntity(dto.getResource(), ResourceRequestDto.class, null, null);
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw new ValidationException(dto.getClass().getSimpleName(), dto.toString());
    }
  }

  public ResourceResponseDto toDto(Resource resource) {
    return singleResourceMapper.toDto(resource, new ResourceResponseDto(), null, null);
  }

  @Mapping(target = "recordType", constant = "MARC_BIB")
  @Mapping(target = "parsedRecord.content", source = "marc")
  public abstract ResourceMarcViewDto toMarcViewDto(Resource resource, String marc);

  @Mapping(target = "types", expression = """
    java(resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .toList())""")
  @Mapping(target = "outgoingEdges", expression = "java(getOutgoingEdges(resource))")
  @Mapping(target = "incomingEdges", expression = "java(getIncomingEdges(resource))")
  @Mapping(target = "indexDate", source = "indexDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  public abstract ResourceGraphDto toResourceGraphDto(Resource resource);

  protected ResourceEdgeDto getIncomingEdges(Resource resource) {
    return getEdges(resource.getIncomingEdges(), edge -> edge.getSource().getId());
  }

  protected ResourceEdgeDto getOutgoingEdges(Resource resource) {
    return getEdges(resource.getOutgoingEdges(), edge -> edge.getTarget().getId());
  }

  private ResourceEdgeDto getEdges(Set<ResourceEdge> edges, ToLongFunction<ResourceEdge> hashProvider) {
    Map<String, List<Long>> edgesDtoMap =  edges.stream()
      .limit(1000)
      .collect(Collectors.toMap(re -> re.getPredicate().getUri(),
        re -> new ArrayList<>(List.of(hashProvider.applyAsLong(re))),
        (a, b) -> {
          a.addAll(b);
          return a;
        }));

    return new ResourceEdgeDto(edges.size(), edgesDtoMap);
  }
}
