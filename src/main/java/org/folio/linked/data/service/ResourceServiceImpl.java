package org.folio.linked.data.service;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.isOfType;
import static org.folio.linked.data.util.Constants.EXISTS_ALREADY;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.Optional;
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
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
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

  private static final String NOT_INDEXED =
    "Resource [%s] has been %s without indexing, because no Work was found in it's graph";
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
    extractWork(persisted)
      .map(ResourceCreatedEvent::new)
      .ifPresentOrElse(applicationEventPublisher::publishEvent,
        () -> log.warn(format(NOT_INDEXED, persisted.getResourceHash(), "created"))
      );
    return resourceMapper.toDto(persisted);
  }

  @Override
  public Long createResource(org.folio.marc4ld.model.Resource marc4ldResource) {
    var mapped = resourceMapper.toEntity(marc4ldResource);
    var persisted = resourceRepo.save(mapped);
    log.info("createResource [{}]\nfrom marc4ldResource [{}]", persisted, marc4ldResource);
    extractWork(persisted)
      .map(ResourceCreatedEvent::new)
      .ifPresentOrElse(applicationEventPublisher::publishEvent,
        () -> log.warn(format(NOT_INDEXED, persisted.getResourceHash(), "created"))
      );
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
    var old = resourceRepo.findById(id).orElseThrow(() -> getResourceNotFoundException(id));
    addInternalFields(resourceDto, id);
    var oldWorkOptional = extractWork(old).map(Resource::new);
    breakCircularEdges(old);
    resourceRepo.delete(old);
    var mapped = resourceMapper.toEntity(resourceDto);
    var persisted = resourceRepo.save(mapped);
    var newWorkOptional = extractWork(persisted);
    if (newWorkOptional.isPresent() && oldWorkOptional.isPresent()) {
      if (newWorkOptional.get().getResourceHash().equals(oldWorkOptional.get().getResourceHash())) {
        applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWorkOptional.get(), oldWorkOptional.get()));
      } else {
        applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(oldWorkOptional.get(), null));
        applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWorkOptional.get(), null));
      }
    } else if (newWorkOptional.isPresent()) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWorkOptional.get(), null));
    } else if (oldWorkOptional.isPresent()) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(oldWorkOptional.get(), null));
    } else {
      log.warn(format(NOT_INDEXED, persisted.getResourceHash(), "updated"));
    }
    return resourceMapper.toDto(persisted);
  }

  private void addInternalFields(ResourceDto resourceDto, Long id) {
    if (resourceDto.getResource() instanceof InstanceField instanceField) {
      ofNullable(resourceRepo.findByResourceHash(id)).ifPresent(resourceInternal -> {
        ofNullable(resourceInternal.getInventoryId()).ifPresent(instanceField.getInstance()::setInventoryId);
        ofNullable(resourceInternal.getSrsId()).ifPresent(instanceField.getInstance()::setSrsId);
      });
    }
  }

  @Override
  public void deleteResource(Long id) {
    log.info("deleteResource [{}]", id);
    resourceRepo.findById(id).ifPresent(resource -> {
      var oldWork = extractWork(resource).map(Resource::new).orElse(null);
      breakCircularEdges(resource);
      resourceRepo.delete(resource);
      if (isOfType(resource, WORK)) {
        applicationEventPublisher.publishEvent(new ResourceDeletedEvent(resource));
      } else {
        reindexParentWork(id, resource, oldWork);
      }
    });
  }

  private void reindexParentWork(Long id, Resource resource, Resource oldWork) {
    extractWork(resource)
      .map(work -> {
        work.getIncomingEdges().remove(new ResourceEdge(resource, work, INSTANTIATES));
        return new ResourceUpdatedEvent(work, oldWork);
      })
      .ifPresentOrElse(applicationEventPublisher::publishEvent,
        () -> log.warn(format(NOT_INDEXED, id, "deleted"))
      );
  }

  private Optional<Resource> extractWork(Resource resource) {
    return isOfType(resource, WORK) ? Optional.of(resource)
      : resource.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .map(resourceEdge -> {
        var work = resourceEdge.getTarget();
        work.getIncomingEdges().add(resourceEdge);
        return work;
      })
      .findFirst();
  }

  private void breakCircularEdges(Resource resource) {
    breakOutgoingCircularEdges(resource);
    breakIncomingCircularEdges(resource);
  }

  private void breakOutgoingCircularEdges(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      var filtered = edge.getTarget().getIncomingEdges().stream()
        .filter(e -> resource.equals(e.getSource()))
        .collect(Collectors.toSet());
      edge.getTarget().getIncomingEdges().removeAll(filtered);
    });
  }

  private void breakIncomingCircularEdges(Resource resource) {
    resource.getIncomingEdges().forEach(edge -> {
      var filtered = edge.getSource().getOutgoingEdges().stream()
        .filter(e -> resource.equals(e.getTarget()))
        .collect(Collectors.toSet());
      edge.getSource().getOutgoingEdges().removeAll(filtered);
    });
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

  private NotFoundException getResourceNotFoundException(Long id) {
    return new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
  }
}
