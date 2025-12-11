package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class ResourcePrePersistValidationIT extends ITBase {
  @Autowired
  private ResourceRepository resourceRepository;
  @MockitoSpyBean
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

  @Test
  void shouldNotPersistResourceWithoutTypes() {
    // given
    var resourceWithoutTypes = new Resource().setIdAndRefreshEdges(999L);

    // then
    assertThatException()
      .isThrownBy(() -> resourceRepository.save(resourceWithoutTypes))
      .withMessageContaining("Cannot save resource [999] without types");
  }
}
