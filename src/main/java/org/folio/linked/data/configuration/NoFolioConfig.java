package org.folio.linked.data.configuration;

import static org.springframework.http.ResponseEntity.notFound;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resource.TenantApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.ResponseEntity;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${folio.environment}' != 'folio'")
@Log4j2
public class NoFolioConfig implements ApplicationListener<ContextRefreshedEvent> {

  private final TenantService tenantService;
  @Value("${spring.application.name}")
  private String appName;
  @Value("${mod-linked-data.default-schema}")
  private String defaultSchema;
  @Value("${folio.environment}")
  private String folioEnv;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.log(Level.INFO, "Environment is [{}], since it's not 'folio', activating default DB schema [{}]",
      folioEnv, defaultSchema);
    TenantAttributes defaultTenant = new TenantAttributes();
    defaultTenant.setModuleTo(appName);
    tenantService.createOrUpdateTenant(defaultTenant);
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

  @Bean(name = "folioTenantController")
  @Primary
  public TenantApi dummyTenantController() {
    return new TenantApi() {
      @Override
      public ResponseEntity<Void> deleteTenant(String operationId) {
        return notFound().build();
      }

      @Override
      public ResponseEntity<String> getTenant(String operationId) {
        return notFound().build();
      }

      @Override
      public ResponseEntity<Void> postTenant(@Valid TenantAttributes tenantAttributes) {
        return notFound().build();
      }
    };
  }
}
