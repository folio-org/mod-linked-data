package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.EXISTS_ALREADY;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
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
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "label");
  private final ResourceRepository resourceRepo;
  private final ResourceMapper resourceMapper;
  private final KafkaSender kafkaSender;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    var mapped = resourceMapper.toEntity(resourceDto);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(RESOURCE_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    resourceMapper.mapToIndex(persisted).ifPresent(kafkaSender::sendResourceCreated);
    return resourceMapper.toDto(persisted);
  }

  public Long createResource(org.folio.marc2ld.model.Resource marc2ldResource) {
    var mapped = resourceMapper.toEntity(marc2ldResource);
    var persisted = resourceRepo.save(mapped);
    resourceMapper.mapToIndex(persisted).ifPresent(kafkaSender::sendResourceCreated);
    return persisted.getResourceHash();
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceDto getResourceById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return resourceMapper.toDto(resource);
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

}
