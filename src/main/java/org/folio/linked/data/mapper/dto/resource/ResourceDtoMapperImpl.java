package org.folio.linked.data.mapper.dto.resource;

import static java.lang.Long.parseLong;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

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
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapper;
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
    var profileId = resourceProfileService.resolveProfileId(resource);
    switch (dto.getResource()) {
      case InstanceResponseField instanceField -> {
        instanceField.getInstance().setProfileId(profileId);
        setProfileIdForParentWork(instanceField);
      }
      case WorkResponseField workField -> {
        workField.getWork().setProfileId(profileId);
        setProfileIdForSingleInstance(workField);
      }
      default -> throw exceptionBuilder.mappingException(dto.getClass().getSimpleName(), dto.toString());
    }
  }

  private void setProfileIdForParentWork(InstanceResponseField instanceField) {
    instanceField.getInstance().getWorkReference()
      .forEach(work -> {
        var profileId = getProfileId(work.getId(), WORK);
        work.setProfileId(profileId);
      });
  }

  private void setProfileIdForSingleInstance(WorkResponseField workField) {
    var instances = workField.getWork().getInstanceReference();
    if (instances.size() != 1) {
      return;
    }
    var singleInstanceDto = instances.getFirst();
    var profileId = getProfileId(singleInstanceDto.getId(), INSTANCE);
    singleInstanceDto.setProfileId(profileId);
  }

  private Integer getProfileId(String resourceId, ResourceTypeDictionary type) {
    var tempResource = new Resource()
      .setId(parseLong(resourceId))
      .setTypes(Set.of(resourceTypeMapper.toEntity(type)));
    return resourceProfileService.resolveProfileId(tempResource);
  }
}
