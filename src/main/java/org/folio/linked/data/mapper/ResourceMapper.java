package org.folio.linked.data.mapper;

import static org.folio.linked.data.util.BibframeUtils.setEdgesId;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Log4j2
@Mapper(componentModel = SPRING, imports = {Collectors.class, Arrays.class})
public abstract class ResourceMapper {

  @Autowired
  private SingleResourceMapper singleResourceMapper;

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "type", expression = "java(resourceShortInfo.getFirstType().getUri())")
  public abstract ResourceShort map(ResourceShortInfo resourceShortInfo);

  public abstract ResourceShortInfoPage map(Page<ResourceShort> page);

  @Mapping(target = "outgoingEdges", expression = "java(marc4ldResource.getOutgoingEdges().stream()"
    + ".map(marc2ldEdge -> toEntity(marc2ldEdge, resource)).collect(Collectors.toSet()))")
  public abstract Resource toEntity(org.folio.marc4ld.model.Resource marc4ldResource);

  @Mapping(target = "source", source = "source")
  public abstract ResourceEdge toEntity(org.folio.marc4ld.model.ResourceEdge marc4ldEdge, Resource source);

  @SneakyThrows
  public Resource toEntity(ResourceDto dto) {
    try {
      var resource = singleResourceMapper.toEntity(dto.getResource(), ResourceDto.class, null, null);
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
    return singleResourceMapper.toDto(resource, new ResourceDto(), null, null);
  }

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "types", expression = """
    java(resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .toList())""")
  @Mapping(target = "outgoingEdges", expression = """
    java(resource.getOutgoingEdges().stream()
      .collect(Collectors.toMap(re -> re.getPredicate().getUri(),
        re -> new ArrayList<>(Arrays.asList(String.valueOf(re.getTarget().getResourceHash()))),
        (a, b) -> {
          a.addAll(b);
          return a;
        })))""")
  @Mapping(target = "indexDate", source = "indexDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  public abstract ResourceGraphDto toResourceGraphDto(Resource resource);

}
