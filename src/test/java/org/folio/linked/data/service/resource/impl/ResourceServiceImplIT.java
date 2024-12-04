package org.folio.linked.data.service.resource.impl;

import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.hash.HashService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@Disabled
class ResourceServiceImplIT {

  @Autowired
  ResourceRepository resourceRepository;

  @Autowired
  CoreMapper coreMapper;

  @Autowired
  HashService hashService;

  @Test
  @SneakyThrows
  void save() {
    var work = new Resource();
    work.addTypes(WORK);
    coreMapper.addOutgoingEdges(work, WorkRequest.class, List.of(), LANGUAGE);

    work.setId(hashService.hash(work));
    Resource save = resourceRepository.save(work);
    Resource saved = resourceRepository.findById(save.getId()).get();
    saved.setDoc(new TextNode("doc"));
    assertNotNull(saved);
    Thread.sleep(2000);
    var updated = resourceRepository.save(saved);
    assertNotNull(updated);
  }
}
