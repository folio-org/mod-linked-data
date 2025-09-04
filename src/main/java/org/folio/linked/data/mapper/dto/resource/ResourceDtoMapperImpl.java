package org.folio.linked.data.mapper.dto.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ResourceDtoMapperImpl implements ResourceDtoMapper {

  private final SingleResourceMapper singleResourceMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

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
