package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.isOfType;
import static org.folio.linked.data.util.Constants.EXISTS_ALREADY;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "label");
  private final ResourceRepository resourceRepo;
  private final ResourceMapper resourceMapper;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    var mapped = resourceMapper.toEntity(resourceDto);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(RESOURCE_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    log.info("createResource [{}]\nfrom Marva DTO [{}]", persisted, resourceDto);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(persisted));
    return resourceMapper.toDto(persisted);
  }

  @Override
  public Long createResource(org.folio.marc4ld.model.Resource marc4ldResource) {
    var mapped = resourceMapper.toEntity(marc4ldResource);
    var persisted = resourceRepo.save(mapped);
    log.info("createResource [{}]\nfrom marc4ldResource [{}]", persisted, marc4ldResource);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(persisted));
    return persisted.getResourceHash();
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceDto getResourceById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() -> getResourceNotFoundException(id));
    return resourceMapper.toDto(resource);
  }

  @Override
  public ResourceDto updateResource(Long id, ResourceDto resourceDto) {
    log.info("updateResource [{}] from DTO [{}]", id, resourceDto);
    if (!resourceRepo.existsById(id)) {
      throw getResourceNotFoundException(id);
    }
    addInternalFields(resourceDto, id);
    deleteResource(id, false);
    return createResource(resourceDto);
  }

  private void addInternalFields(ResourceDto resourceDto, Long id) {
    if (resourceDto.getResource() instanceof InstanceField instanceField) {
      var resourceInternal = resourceRepo.findByResourceHash(id);
      ofNullable(resourceInternal.getInventoryId()).ifPresent(instanceField.getInstance()::setInventoryId);
      ofNullable(resourceInternal.getSrsId()).ifPresent(instanceField.getInstance()::setSrsId);
    }
  }

  @Override
  public void deleteResource(Long id) {
    log.info("deleteResource [{}]", id);
    deleteResource(id, true);
  }

  private void deleteResource(Long id, boolean reindexParentWork) {
    resourceRepo.findById(id).ifPresent(resource -> {
      breakCircularEdges(resource);
      resourceRepo.delete(resource);
      applicationEventPublisher.publishEvent(new ResourceDeletedEvent(resource));
      reindexParentWork(resource, reindexParentWork);
    });
  }

  private void breakCircularEdges(Resource resource) {
    breakCircularEdges(resource, false);
    breakCircularEdges(resource, true);
  }

  private void breakCircularEdges(Resource resource, boolean isIncoming) {
    (isIncoming ? resource.getIncomingEdges() : resource.getOutgoingEdges()).forEach(edge -> {
      var edges = isIncoming ? edge.getSource().getOutgoingEdges() : edge.getTarget().getIncomingEdges();
      var filtered = edges.stream()
        .filter(e -> resource.equals(isIncoming ? e.getTarget() : e.getSource()))
        .collect(Collectors.toSet());
      edges.removeAll(filtered);
    });
  }

  private void reindexParentWork(Resource resource, boolean reindexParentWork) {
    if (!isOfType(resource, WORK) && reindexParentWork) {
      applicationEventPublisher.publishEvent(new ResourceCreatedEvent(resource));
    }
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
    var page = isNull(type) ? resourceRepo.findAllShort(pageRequest)
      : resourceRepo.findAllShortByType(Set.of(type), pageRequest);
    var pageOfDto = page.map(resourceMapper::map);
    return resourceMapper.map(pageOfDto);
  }

  @Async
  @Override
  public void updateIndexDateBatch(Set<Long> ids) {
    resourceRepo.updateIndexDateBatch(ids);
  }

  @Override
  public ResourceGraphDto getResourceGraphById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() -> getResourceNotFoundException(id));
    return resourceMapper.toResourceGraphDto(resource);
  }

  @Override
  public Resource getResource(Long id, boolean includeIncomingEdges) {
    return resourceRepo.findById(id)
      .map(resource -> {
        if (includeIncomingEdges) {
          // force loading of incoming edges
          resource.getIncomingEdges().size();
        }
        return resource;
      })
      .orElseThrow(() -> getResourceNotFoundException(id));
  }

  private NotFoundException getResourceNotFoundException(Long id) {
    return new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
  }
}
