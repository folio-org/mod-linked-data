package org.folio.linked.data.integration.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.mockito.Mockito.verify;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.search.domain.dto.DataImportEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

@Disabled("no tenant id in context must be rewrited")
@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class DataImportEventHandlerIT {

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;

  @SpyBean
  @Autowired
  private KafkaSearchSender kafkaSearchSender;

  @Autowired
  private DataImportEventHandler dataImportEventHandler;

  @Test
  void shouldSendWorkForIndexingWithExistingInstanceAlongsideWithNewlyCreatedOne() {
    //given
    var workResource = getSampleWork(null).setId(9046598627024476842L);
    resourceRepository.save(getSampleInstanceResource(null, workResource));
    resourceRepository.save(workResource);
    var resourceEdge = workResource.getIncomingEdges().iterator().next();
    resourceEdge.computeId();
    resourceEdgeRepository.save(resourceEdge);
    var event = new DataImportEvent()
      .id("eventId")
      .tenant(TENANT_ID)
      .eventType("eventType")
      .marcBib(loadResourceAsString("samples/full_marc_sample.jsonl"));

    //when
    dataImportEventHandler.handle(event);

    //then
    var resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(kafkaSearchSender).sendWorkCreated(resourceCaptor.capture());
    assertThat(resourceCaptor.getValue().getIncomingEdges()).hasSize(2);
  }
}
