package org.folio.linked.data.service.impl;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.BibframeUtils.isOfType;
import static org.folio.linked.data.util.Constants.*;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.ResourceService;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
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
  private final ResourceEdgeRepository edgeRepo;
  private final ResourceDtoMapper resourceDtoMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final Bibframe2MarcMapper bibframe2MarcMapper;

  @Override
  public ResourceDto createResource(ResourceDto resourceDto) {
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    log.info("createResource\n[{}]\nfrom Marva DTO [{}]", mapped, resourceDto);
    saveMergingGraph(mapped);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(mapped.getId()));
    return resourceDtoMapper.toDto(mapped);
  }

  @Override
  public Long createResource(org.folio.ld.dictionary.model.Resource modelResource) {
    var mapped = resourceModelMapper.toEntity(modelResource);
    var persisted = saveMergingGraph(mapped);
    log.info("createResource [{}]\nfrom modelResource [{}]", persisted, modelResource);
    applicationEventPublisher.publishEvent(new ResourceCreatedEvent(mapped.getId()));
    return persisted.getId();
  }

  @Override
  @Transactional(readOnly = true)
  public ResourceDto getResourceById(Long id) {
    var resource = getResource(id);
    return resourceDtoMapper.toDto(resource);
  }

  @Override
  public ResourceDto updateResource(Long id, ResourceDto resourceDto) {
    log.info("updateResource [{}] from DTO [{}]", id, resourceDto);
    var old = getResource(id);
    addInternalFields(resourceDto, old);
    var oldWork = extractWork(old).map(Resource::new).orElse(null);
    breakCircularEdges(old);
    resourceRepo.delete(old);
    var mapped = resourceDtoMapper.toEntity(resourceDto);
    var persisted = saveMergingGraph(mapped);
    if (isOfType(persisted, WORK)) {
      applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(persisted, oldWork));
    } else {
      reindexParentWorkAfterInstanceUpdate(persisted, oldWork);
    }
    return resourceDtoMapper.toDto(persisted);
  }

  @Override
  public Resource saveMergingGraph(Resource resource) {
    return saveMergingGraphSkippingAlreadySaved(resource, null);
  }

  private Resource saveMergingGraphSkippingAlreadySaved(Resource resource, Resource saved) {
    if (resource.isNew()) {
      if (doesNotExists(resource)) {
        resourceRepo.save(resource);
      }
      saveEdges(resource, resource.getOutgoingEdges(), ResourceEdge::getTarget, saved);
      saveEdges(resource, resource.getIncomingEdges(), ResourceEdge::getSource, saved);
    }
    return resource;
  }

  private void saveEdges(Resource resource, Set<ResourceEdge> edges, Function<ResourceEdge, Resource> resourceSelector,
                         Resource saved) {
    edges.stream()
      .filter(edge -> notEqual(resourceSelector.apply(edge), saved))
      .forEach(edge -> saveEdge(resourceSelector.apply(edge), resource, edge));
  }

  private void saveEdge(Resource edgeResource, Resource resource, ResourceEdge edge) {
    if (edge.isNew()) {
      saveMergingGraphSkippingAlreadySaved(edgeResource, resource);
      edge.computeId();
      if (doesNotExists(edge)) {
        edgeRepo.save(edge);
      }
    }
  }

  private boolean doesNotExists(Resource resource) {
    return isNull(resource) || isNull(resource.getId()) || !resourceRepo.existsById(resource.getId());
  }

  private boolean doesNotExists(ResourceEdge edge) {
    return isNull(edge) || isNull(edge.getId()) || !edgeRepo.existsById(edge.getId());
  }

  private void reindexParentWorkAfterInstanceUpdate(Resource instance, Resource oldWork) {
    extractWork(instance).ifPresentOrElse(newWork -> {
        if (nonNull(oldWork) && newWork.getId().equals(oldWork.getId())) {
          log.info("Instance [{}] update under the same Work [{}] triggered it's reindexing",
            instance.getId(), newWork.getId());
          applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWork, oldWork));
        } else {
          if (nonNull(oldWork)) {
            indexOldWorkDisconnectedFromUpdatedInstance(instance, oldWork, newWork);
          }
          log.info("Instance [{}] update under newly linked Work [{}] triggered it's reindexing",
            instance.getId(), newWork.getId());
          applicationEventPublisher.publishEvent(new ResourceUpdatedEvent(newWork, null));
        }
      },
      () -> {
        if (nonNull(oldWork)) {
          indexOldWorkDisconnectedFromUpdatedInstance(instance, oldWork, null);
        } else {
          log.info("Instance [{}] not linked to Work update under no Work has not triggered any Work reindexing",
            instance.getId());
        }
      }
    );
  }

  private void indexOldWorkDisconnectedFromUpdatedInstance(Resource instance, Resource oldWork, Resource newWork) {
    oldWork.getIncomingEdges().remove(new ResourceEdge(instance, oldWork, INSTANTIATES));
    log.info("Instance [{}] update under {} triggered old Work [{}] reindexing",
      instance.getId(), isNull(newWork) ? "no Work" : "different Work [" + newWork.getId() + "]",
      oldWork.getId());
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

  @Override
  @Transactional(readOnly = true)
  public ResourceMarcViewDto getResourceMarcViewById(Long id) {
    var resource = getResource(id);
    validateMarkViewSupportedType(resource);
    var resourceModel = resourceModelMapper.toModel(resource);
    var marc = bibframe2MarcMapper.toMarcJson(resourceModel);
    return resourceDtoMapper.toMarcViewDto(resource, marc);
  }

  private void validateMarkViewSupportedType(Resource resource) {
    if (isOfType(resource, INSTANCE)) {
      return;
    }
    throw new ValidationException(
      "Resource is not supported for MARC view",
      "type", resource.getTypes().stream()
      .map(ResourceTypeEntity::getUri)
      .collect(Collectors.joining(", ", "[", "]"))
    );
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
    var resource = getResource(id);
    return resourceDtoMapper.toResourceGraphDto(resource);
  }

  private Resource getResource(Long id) {
    return resourceRepo.findById(id)
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
  }
}
