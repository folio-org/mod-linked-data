package org.folio.linked.data.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.controller.TenantController;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${folio.environment}' != 'folio'")
@Log4j2
public class NoFolioConfig implements ApplicationListener<ContextRefreshedEvent> {

  private final TenantController tenantController;
  @Value("${spring.application.name}")
  private String appName;
  @Value("${mod-linked-data.default-schema}")
  private String defaultSchema;
  @Value("${folio.environment}")
  private String folioEnv;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.log(Level.INFO, "Environment is [{}}, since it's not 'folio', activating default DB schema [{}]",
      folioEnv, defaultSchema);
    TenantAttributes defaultTenant = new TenantAttributes();
    defaultTenant.setModuleTo(appName);
    tenantController.postTenant(defaultTenant);
  }

  @Bean
  public FolioModuleMetadata folioModuleMetadata() {
    return new FolioModuleMetadata() {
      @Override
      public String getModuleName() {
        return appName;
      }

      @Override
      public String getDBSchemaName(String tenantId) {
        return defaultSchema;
      }
    };
  }
}
