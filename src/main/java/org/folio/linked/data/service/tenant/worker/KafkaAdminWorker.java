package org.folio.linked.data.service.tenant.worker;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class KafkaAdminWorker implements TenantServiceWorker {

  private final KafkaAdminService kafkaAdminService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    kafkaAdminService.createTopics(tenantId);
    kafkaAdminService.restartEventListeners();
  }

  @Override
  public void afterTenantDeletion(String tenantId) {
    kafkaAdminService.deleteTopics(tenantId);
  }
}
