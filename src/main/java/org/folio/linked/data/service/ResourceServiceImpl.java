package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.BIBFRAME_PROFILE;
import static org.folio.linked.data.util.Constants.EXISTS_ALREADY;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.IS_NOT_IN_THE_LIST_OF_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.Bibframe2ShortInfoPage;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "resourceHash");
  private final ResourceRepository resourceRepo;
  private final ResourceMapper resourceMapper;
  private final BibframeProperties bibframeProperties;
  private final KafkaSender kafkaSender;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    var mapped = resourceMapper.toEntity(resourceDto);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(RESOURCE_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    kafkaSender.sendResourceCreated(resourceMapper.mapToIndex(persisted));
    return resourceMapper.toDto(persisted);
  }

  @Override
  public Bibframe2Response createBibframe2(Bibframe2Request bibframeRequest) {
    if (!bibframeProperties.getProfiles().contains(bibframeRequest.getProfile())) {
      throw new NotSupportedException(BIBFRAME_PROFILE + bibframeRequest.getProfile()
        + IS_NOT_IN_THE_LIST_OF_SUPPORTED + String.join(", ", bibframeProperties.getProfiles()));
    }
    var mapped = resourceMapper.toEntity2(bibframeRequest);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(RESOURCE_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    kafkaSender.sendResourceCreated(resourceMapper.mapToIndex2(persisted));
    return resourceMapper.toDto2(persisted);
  }

  @Override
  public ResourceDto getResourceById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return resourceMapper.toDto(resource);
  }

  @Override
  public Bibframe2Response getBibframe2ById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return resourceMapper.toDto2(resource);
  }

  @Override
  public ResourceDto updateResource(Long id, ResourceDto resourceDto) {
    if (!resourceRepo.existsById(id)) {
      throw new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
    }
    deleteResource(id);
    return createResource(resourceDto);
  }

  @Override
  public Bibframe2Response updateBibframe2(Long id, Bibframe2Request bibframeRequest) {
    if (!resourceRepo.existsById(id)) {
      throw new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
    }
    deleteResource(id);
    return createBibframe2(bibframeRequest);
  }

  @Override
  public void deleteResource(Long id) {
    resourceRepo.deleteById(id);
    kafkaSender.sendResourceDeleted(id);
  }

  @Override
  public ResourceShortInfoPage getResourceShortInfoPage(String type, Integer pageNumber, Integer pageSize) {
    if (isNull(pageNumber)) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }
    if (isNull(pageSize)) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    var pageRequest = PageRequest.of(pageNumber, pageSize, DEFAULT_SORT);
    var page = isNull(type) ? resourceRepo.findAllPageable(pageRequest)
      : resourceRepo.findResourcesByType(Set.of(type), pageRequest);
    var pageOfDto = page.map(resourceMapper::map);
    return resourceMapper.map(pageOfDto);
  }

  @Override
  public Bibframe2ShortInfoPage getBibframe2ShortInfoPage(String type, Integer pageNumber, Integer pageSize) {
    if (isNull(pageNumber)) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }
    if (isNull(pageSize)) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    var page = resourceRepo.findResourcesByType(Set.of(type), PageRequest.of(pageNumber, pageSize, DEFAULT_SORT));
    var pageOfDto = page.map(resourceMapper::map2);
    return resourceMapper.map2(pageOfDto);
  }

  @Override
  public Bibframe2ShortInfoPage getBibframe2ShortInfoPage(Integer pageNumber, Integer pageSize) {
    return getBibframe2ShortInfoPage(bibframeProperties.getProfiles().iterator().next(), pageNumber, pageSize);
  }

}
