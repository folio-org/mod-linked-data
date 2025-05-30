package org.folio.linked.data.service.tenant.worker;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Order(1)
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class KafkaAdminWorker implements TenantServiceWorker {

  private final KafkaAdminService kafkaAdminService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    log.info("Creating kafka topics for tenant {}", tenantId);
    kafkaAdminService.createTopics(tenantId);
    kafkaAdminService.restartEventListeners();
  }

  @Override
  public void afterTenantDeletion(String tenantId) {
    log.info("Deleting kafka topics for tenant {}", tenantId);
    kafkaAdminService.deleteTopics(tenantId);
  }
}
