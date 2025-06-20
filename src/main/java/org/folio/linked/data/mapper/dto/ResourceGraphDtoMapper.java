package org.folio.linked.data.mapper.dto;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.Set;
import java.util.function.ToLongFunction;
import org.folio.linked.data.domain.dto.ResourceEdgeDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public abstract class ResourceGraphDtoMapper {

  private static final int EDGES_LIMIT = 1000;

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

  protected String mapType(ResourceTypeEntity type) {
    return type.getUri();
  }

  private ResourceEdgeDto getEdges(Set<ResourceEdge> edges, ToLongFunction<ResourceEdge> hashProvider) {
    var edgesDtoMap = edges.stream()
      .limit(EDGES_LIMIT)
      .collect(groupingBy(
        re -> re.getPredicate().getUri(),
        mapping(hashProvider::applyAsLong, toList())
      ));

    return new ResourceEdgeDto(edges.size(), edgesDtoMap);
  }
}
