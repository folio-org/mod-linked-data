package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ResourcePrePersistValidationIT {
  @Autowired
  private ResourceRepository resourceRepository;

  @Test
  void shouldNotPersistNonInstanceResourceHavingInstanceMetadata() {
    // given
    var nonInstanceResource = new Resource()
      .addType(new ResourceTypeEntity().setHash(WORK.getHash()).setUri(WORK.getUri()));
    nonInstanceResource.setInstanceMetadata(new InstanceMetadata(nonInstanceResource));

    // then
    assertThatException()
      .isThrownBy(() -> resourceRepository.save(nonInstanceResource))
      .withMessageContaining("Instance metadata can be set only for instance resource");
  }
}
