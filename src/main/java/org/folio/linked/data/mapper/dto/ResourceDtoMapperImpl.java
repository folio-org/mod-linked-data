package org.folio.linked.data.mapper.dto;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceResponseField;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ResourceDtoMapperImpl implements ResourceDtoMapper {

  private final SingleResourceMapper singleResourceMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final ResourceProfileLinkingService resourceProfileService;

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
    var dto = singleResourceMapper.toDto(resource, new ResourceResponseDto(), null, null);
    setProfileIds(resource, dto);
    return dto;
  }

  private void setProfileIds(Resource resource, ResourceResponseDto dto) {
    resourceProfileService.resolveProfileId(resource)
      .ifPresent(profileId -> this.setProfileId(dto.getResource(), profileId));

    // If the Work has a single instance, UI need to open that instance for viewing
    // Hence, set its profileId
    if (dto.getResource() instanceof WorkResponseField workField) {
      getSingleInstance(workField)
        .ifPresent(instance -> resourceProfileService
          .resolveProfileId(Long.parseLong(instance.getId()))
          .ifPresent(instance::setProfileId)
        );
    }
  }

  private Optional<InstanceResponse> getSingleInstance(WorkResponseField workField) {
    var instances = workField.getWork().getInstanceReference();
    if (instances.size() == 1) {
      return instances.stream().findFirst();
    }
    return Optional.empty();
  }

  private void setProfileId(ResourceResponseField dto, Integer integer) {
    switch (dto) {
      case InstanceField instanceField -> instanceField.getInstance().setProfileId(integer);
      case WorkField workField -> workField.getWork().setProfileId(integer);
      default -> throw new IllegalStateException("Unexpected value: " + dto);
    }
  }
}
