package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.FOLIO_ENV;

import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
public class LinkedDataTenantService extends TenantService {

  private final KafkaAdminService kafkaAdminService;
  @Value("${folio.environment}")
  private String folioEnv;

  public LinkedDataTenantService(JdbcTemplate jdbcTemplate, FolioExecutionContext context,
                                 FolioSpringLiquibase folioSpringLiquibase, KafkaAdminService kafkaAdminService) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaAdminService = kafkaAdminService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    var tenantId = FOLIO_ENV.equals(folioEnv) ? context.getTenantId() : "standalone";
    kafkaAdminService.createTopics(tenantId);
    kafkaAdminService.restartEventListeners();
    log.info("Tenant init has been completed");
  }

  @Override
  protected void afterTenantDeletion(TenantAttributes tenantAttributes) {
    var tenantId = FOLIO_ENV.equals(folioEnv) ? context.getTenantId() : "standalone";
    kafkaAdminService.deleteTopics(tenantId);
  }

}
