package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/linked-data/resources")
@RequiredArgsConstructor
public class ResourceLinkingController {

  private final ResourceRepository resourceRepository;
  private final ResourceEdgeRepository resourceEdgeRepository;

  @PostMapping("/connect")
  @Transactional
  public ResponseEntity<String> connectResources(
      @RequestParam Long sourceResourceId,
      @RequestParam Long targetResourceId,
      @RequestParam PredicateDictionary connectionType) {
    // Find source and target resources
    var sourceOpt = resourceRepository.findById(sourceResourceId);
    var targetOpt = resourceRepository.findById(targetResourceId);
    if (sourceOpt.isEmpty() || targetOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source or target resource not found");
    }
    Resource source = sourceOpt.get();
    Resource target = targetOpt.get();


    // Create ResourceEdge
    ResourceEdge edge = new ResourceEdge(source, target, connectionType);
    edge.computeId();
    resourceEdgeRepository.save(edge);

    return ResponseEntity.ok("Resources connected successfully");
  }

  @PostMapping("/disconnect")
  @Transactional
  public ResponseEntity<String> disconnectResources(
      @RequestParam Long sourceResourceId,
      @RequestParam Long targetResourceId,
      @RequestParam PredicateDictionary connectionType) {

    // Delete ResourceEdge directly by hashes
    long deletedCount = resourceEdgeRepository.deleteByIdSourceHashAndIdTargetHashAndIdPredicateHash(
      sourceResourceId,
      targetResourceId,
      connectionType.getHash()
    );
    if (deletedCount == 0) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("ResourceEdge not found for given parameters");
    }
    return ResponseEntity.ok("Resources disconnected successfully");
  }
}
