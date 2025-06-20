package org.folio.linked.data.mapper.dto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@Mapper(componentModel = SPRING, imports = {Collectors.class, Arrays.class, ResourceTypeEntity.class})
// We cannot use constructor injection in the subclass due to https://github.com/mapstruct/mapstruct/issues/2257
// so, we use field injection here.
@SuppressWarnings("java:S6813")
public abstract class ResourceDtoMapper {

  @Autowired
  private SingleResourceMapper singleResourceMapper;
  @Autowired
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @SneakyThrows
  public Resource toEntity(ResourceRequestDto dto) {
    try {
      return singleResourceMapper.toEntity(dto.getResource(), ResourceRequestDto.class, null, null);
    } catch (RequestProcessingException rpe) {
      throw rpe;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw exceptionBuilder.mappingException(dto.getClass().getSimpleName(), dto.toString());
    }
  }

  public ResourceResponseDto toDto(Resource resource) {
    return singleResourceMapper.toDto(resource, new ResourceResponseDto(), null, null);
  }
}
