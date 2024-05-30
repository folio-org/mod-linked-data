package org.folio.linked.data.service.impl.tenant.workers;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.service.impl.tenant.TenantServiceWorker;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaAdminWorker implements TenantServiceWorker {

  private final KafkaAdminService kafkaAdminService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    kafkaAdminService.createTopics(tenantId);
    kafkaAdminService.restartEventListeners();
  }
}
