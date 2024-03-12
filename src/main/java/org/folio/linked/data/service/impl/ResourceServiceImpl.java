package org.folio.linked.data.service.impl;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.ResourceService;
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
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    var mapped = resourceDtoMapper.toEntity(resourceDto);
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
    return resourceDtoMapper.toDto(persisted);
  }

  @Override
  public Long createResource(org.folio.ld.dictionary.model.Resource modelResource) {
    var mapped = resourceModelMapper.toEntity(modelResource);
    var persisted = resourceRepo.save(mapped);
    log.info("createResource [{}]\nfrom modelResource [{}]", persisted, modelResource);
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
    return resourceDtoMapper.toDto(resource);
  }

  @Override
  public ResourceDto updateResource(Long id, ResourceDto resourceDto) {
    log.info("updateResource [{}] from DTO [{}]", id, resourceDto);
    var old = resourceRepo.findById(id).orElseThrow(() -> getResourceNotFoundException(id));
    addInternalFields(resourceDto, old);
    var oldWork = extractWork(old).map(Resource::new).orElse(null);
    breakCircularEdges(old);
    resourceRepo.delete(old);
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    var persisted = resourceRepo.save(mapped);
    if (isOfType(persisted, WORK)) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(persisted, oldWork));
    } else {
      reindexParentWorkAfterInstanceUpdate(persisted, oldWork);
    }
    return resourceDtoMapper.toDto(persisted);
  }

  @Override
  public void saveMergingGraph(Resource resource) {
    resource = takeExistingAddingNewEdges(resource);
    resourceRepo.save(resource);
  }

  private Resource takeExistingAddingNewEdges(Resource newResource) {
    return resourceRepo.findById(newResource.getResourceHash())
      .map(existed -> {
        newResource.getOutgoingEdges().stream()
          .map(newOe -> new ResourceEdge(existed, takeExistingAddingNewEdges(newOe.getTarget()), newOe.getPredicate()))
          .forEach(existed.getOutgoingEdges()::add);
        newResource.getIncomingEdges().stream()
          .map(newIe -> new ResourceEdge(takeExistingAddingNewEdges(newIe.getSource()), existed, newIe.getPredicate()))
          .forEach(existed.getIncomingEdges()::add);
        return existed;
      })
      .orElseGet(() -> {
        newResource.getOutgoingEdges().forEach(oe -> oe.setTarget(takeExistingAddingNewEdges(oe.getTarget())));
        newResource.getIncomingEdges().forEach(ie -> ie.setSource(takeExistingAddingNewEdges(ie.getSource())));
        return newResource;
      });
  }

  private void reindexParentWorkAfterInstanceUpdate(Resource instance, Resource oldWork) {
    extractWork(instance).ifPresentOrElse(newWork -> {
        if (nonNull(oldWork) && newWork.getResourceHash().equals(oldWork.getResourceHash())) {
          log.info("Instance [{}] update under the same Work [{}] triggered it's reindexing",
            instance.getResourceHash(), newWork.getResourceHash());
          applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWork, oldWork));
        } else {
          if (nonNull(oldWork)) {
            indexOldWorkDisconnectedFromUpdatedInstance(instance, oldWork, newWork);
          }
          log.info("Instance [{}] update under newly linked Work [{}] triggered it's reindexing",
            instance.getResourceHash(), newWork.getResourceHash());
          applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWork, null));
        }
      },
      () -> {
        if (nonNull(oldWork)) {
          indexOldWorkDisconnectedFromUpdatedInstance(instance, oldWork, null);
        } else {
          log.info("Instance [{}] not linked to Work update under no Work has not triggered any Work reindexing",
            instance.getResourceHash());
        }
      }
    );
  }

  private void indexOldWorkDisconnectedFromUpdatedInstance(Resource instance, Resource oldWork, Resource newWork) {
    oldWork.getIncomingEdges().remove(new ResourceEdge(instance, oldWork, INSTANTIATES));
    log.info("Instance [{}] update under {} triggered old Work [{}] reindexing",
      instance.getResourceHash(), isNull(newWork) ? "no Work" : "different Work [" + newWork.getResourceHash() + "]",
      oldWork.getResourceHash());
    applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(oldWork, null));
  }

  private void addInternalFields(ResourceDto resourceDto, Resource old) {
    if (resourceDto.getResource() instanceof InstanceField instanceField) {
      ofNullable(old.getInventoryId()).ifPresent(instanceField.getInstance()::setInventoryId);
      ofNullable(old.getSrsId()).ifPresent(instanceField.getInstance()::setSrsId);
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
        reindexParentWorkAfterInstanceDeletion(id, resource, oldWork);
      }
    });
  }

  private void reindexParentWorkAfterInstanceDeletion(Long id, Resource resource, Resource oldWork) {
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
    var pageOfDto = page.map(resourceDtoMapper::map);
    return resourceDtoMapper.map(pageOfDto);
  }

  @Async
  @Override
  public void updateIndexDateBatch(Set<Long> ids) {
    resourceRepo.updateIndexDateBatch(ids);
  }

  @Override
  public ResourceGraphDto getResourceGraphById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() -> getResourceNotFoundException(id));
    return resourceDtoMapper.toResourceGraphDto(resource);
  }

  private NotFoundException getResourceNotFoundException(Long id) {
    return new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
  }
}
