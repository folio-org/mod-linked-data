package org.folio.linked.data.service.impl.tenant.workers;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.service.impl.tenant.TenantServiceWorker;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({FOLIO_PROFILE, SEARCH_PROFILE})
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
