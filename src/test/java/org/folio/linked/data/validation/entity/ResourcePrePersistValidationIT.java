package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class ResourcePrePersistValidationIT {
  @Autowired
  private ResourceRepository resourceRepository;
  @MockitoBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void shouldNotPersistNonInstanceResourceHavingFolioMetadata() {
    // given
    var nonInstanceResource = new Resource()
      .addType(new ResourceTypeEntity().setHash(WORK.getHash()).setUri(WORK.getUri()));
    nonInstanceResource.setFolioMetadata(new FolioMetadata(nonInstanceResource));

    // then
    assertThatException()
      .isThrownBy(() -> resourceRepository.save(nonInstanceResource))
      .withMessageContaining("Folio metadata can be set only for instance and authority resources");
  }
}
