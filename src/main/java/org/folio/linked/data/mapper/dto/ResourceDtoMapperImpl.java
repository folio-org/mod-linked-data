package org.folio.linked.data.mapper.dto;

import static java.lang.Long.parseLong;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dictionary.ResourceTypeMapper;
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
  private final ResourceTypeMapper resourceTypeMapper;

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
    this.setProfileId(dto, resourceProfileService.resolveProfileId(resource));

    // If the Work has a single Instance, UI need to open that instance for viewing
    // Hence, set its profileId
    if (dto.getResource() instanceof WorkResponseField workField) {
      setSingleInstanceProfileId(workField);
    }
  }

  private void setSingleInstanceProfileId(WorkResponseField workField) {
    var instances = workField.getWork().getInstanceReference();
    if (instances.size() != 1) {
      return;
    }
    var instanceDto = instances.getFirst();
    var tempInstance = new Resource()
      .setId(parseLong(instanceDto.getId()))
      .setTypes(Set.of(resourceTypeMapper.toEntity(ResourceTypeDictionary.INSTANCE)));
    var profileId = resourceProfileService.resolveProfileId(tempInstance);

    instanceDto.setProfileId(profileId);
  }

  private void setProfileId(ResourceResponseDto dto, Integer integer) {
    switch (dto.getResource()) {
      case InstanceResponseField instanceField -> instanceField.getInstance().setProfileId(integer);
      case WorkResponseField workField -> workField.getWork().setProfileId(integer);
      default -> throw new IllegalArgumentException("Unexpected dto for getting profileId: " + dto);
    }
  }
}
