package org.folio.linked.data.service.tenant.worker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaAdminWorkerTest {
  @Mock
  private KafkaAdminService kafkaAdminService;

  @InjectMocks
  private KafkaAdminWorker kafkaAdminWorker;

  @Test
  void shouldCreateTopics() {
    // given
    var attributes = mock(TenantAttributes.class);
    var tenantId = "tenant-01";

    // when
    kafkaAdminWorker.afterTenantUpdate(tenantId, attributes);

    // then
    verify(kafkaAdminService).createTopics(tenantId);
    verify(kafkaAdminService).restartEventListeners();
  }

  @Test
  void shouldDeleteTopics() {
    // given
    var tenantId = "tenant-01";

    // when
    kafkaAdminWorker.afterTenantDeletion(tenantId);

    // then
    verify(kafkaAdminService).deleteTopics(tenantId);
  }
}
