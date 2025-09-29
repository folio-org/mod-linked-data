package org.folio.linked.data.controller;

import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ResourceSearchController {
  private final ResourceRepository resourceRepository;

  @Autowired
  public ResourceSearchController(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  @GetMapping("/linked-data/resources/search-by-label")
  public Set<Resource> searchResourcesByLabel(String label) {
    Set<org.folio.linked.data.model.entity.Resource> entities = resourceRepository.findByLabelContainingIgnoreCase(label);
    return entities.stream()
      .map(e -> new Resource(
        e.getId(),
        e.getTypes() != null ? e.getTypes().stream()
          .map(t -> t.getUri()).collect(java.util.stream.Collectors.toSet())
          : java.util.Collections.emptySet(),
        e.getLabel()
      ))
      .collect(java.util.stream.Collectors.toSet());
  }

  record Resource(Long id, Set<String> types, String label) {}
}
