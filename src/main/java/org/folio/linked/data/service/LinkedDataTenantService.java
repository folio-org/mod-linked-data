package org.folio.linked.data.service;

import static java.util.Arrays.asList;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.STANDALONE;

import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
@Profile({"folio", "kafka"})
public class LinkedDataTenantService extends TenantService {

  private final KafkaAdminService kafkaAdminService;
  private final Environment env;

  public LinkedDataTenantService(JdbcTemplate jdbcTemplate, FolioExecutionContext context,
                                 FolioSpringLiquibase folioSpringLiquibase, KafkaAdminService kafkaAdminService,
                                 Environment env) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaAdminService = kafkaAdminService;
    this.env = env;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    boolean folioProfileEnabled = asList(env.getActiveProfiles()).contains(FOLIO_PROFILE);
    var tenantId = folioProfileEnabled ? context.getTenantId() : STANDALONE;
    kafkaAdminService.createTopics(tenantId);
    kafkaAdminService.restartEventListeners();
    log.info("Tenant init has been completed");
  }

  @Override
  protected void afterTenantDeletion(TenantAttributes tenantAttributes) {
    boolean folioProfileEnabled = asList(env.getActiveProfiles()).contains(FOLIO_PROFILE);
    var tenantId = folioProfileEnabled ? context.getTenantId() : STANDALONE;
    kafkaAdminService.deleteTopics(tenantId);
  }

}
