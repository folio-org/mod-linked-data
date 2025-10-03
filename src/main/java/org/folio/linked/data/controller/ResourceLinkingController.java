package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<String> connectResources(
      @RequestParam Long sourceResourceId,
      @RequestParam Long targetResourceId,
      @RequestParam String connectionType) {
    // Find source and target resources
    var sourceOpt = resourceRepository.findById(sourceResourceId);
    var targetOpt = resourceRepository.findById(targetResourceId);
    if (sourceOpt.isEmpty() || targetOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Source or target resource not found");
    }
    Resource source = sourceOpt.get();
    Resource target = targetOpt.get();

    // Normalize connectionType (remove spaces, lower case)
    String normalizedType = connectionType.replace(" ", "").toLowerCase();

    // Find predicate whose URI ends with normalizedType (case-insensitive, ignore spaces/camel case)
    PredicateDictionary predicateDict = null;
    for (PredicateDictionary pd : PredicateDictionary.values()) {
      String uri = pd.getUri();
      if (uri != null && uri.toLowerCase().endsWith(normalizedType)) {
        predicateDict = pd;
        break;
      }
    }
    if (predicateDict == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Predicate not found for connection type: " + connectionType);
    }

    // Create ResourceEdge
    ResourceEdge edge = new ResourceEdge(source, target, predicateDict);
    edge.computeId();
    resourceEdgeRepository.save(edge);

    return ResponseEntity.ok("Resources connected successfully");
  }

  @PostMapping("/disconnect")
  public ResponseEntity<String> disconnectResources(
      @RequestParam Long sourceResourceId,
      @RequestParam Long targetResourceId,
      @RequestParam String connectionType) {

    // Normalize connectionType (remove spaces, lower case)
    String normalizedType = connectionType.replace(" ", "").toLowerCase();

    // Find predicate whose URI ends with normalizedType (case-insensitive, ignore spaces/camel case)
    PredicateDictionary predicateDict = null;
    for (PredicateDictionary pd : PredicateDictionary.values()) {
      String uri = pd.getUri();
      if (uri != null && uri.toLowerCase().endsWith(normalizedType)) {
        predicateDict = pd;
        break;
      }
    }
    if (predicateDict == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body("Predicate not found for connection type: " + connectionType);
    }

    // Delete ResourceEdge directly by hashes
    long deletedCount = resourceEdgeRepository.deleteByIdSourceHashAndIdTargetHashAndIdPredicateHash(
      sourceResourceId,
      targetResourceId,
      predicateDict.getHash()
    );
    if (deletedCount == 0) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("ResourceEdge not found for given parameters");
    }
    return ResponseEntity.ok("Resources disconnected successfully");
  }
}
