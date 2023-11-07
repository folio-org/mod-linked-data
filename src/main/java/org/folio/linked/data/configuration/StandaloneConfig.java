package org.folio.linked.data.configuration;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.springframework.http.ResponseEntity.notFound;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resource.TenantApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@RequiredArgsConstructor
@Profile("!" + FOLIO_PROFILE)
@Log4j2
public class StandaloneConfig implements ApplicationListener<ContextRefreshedEvent> {

  private final FolioSpringLiquibase folioSpringLiquibase;
  @Value("${mod-linked-data.default-schema}")
  private String defaultSchema;

  @Override
  @SneakyThrows
  public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
    log.log(Level.INFO, "Standalone mode is on, activating default DB schema [{}]", defaultSchema);
    folioSpringLiquibase.setDefaultSchema(defaultSchema);
    folioSpringLiquibase.performLiquibaseUpdate();
  }

  @Primary
  @Bean(name = "folioTenantController")
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

  @Bean(name = "tenantOkapiHeaderValidationFilter")
  public GenericFilterBean dummyTenantOkapiHeaderValidationFilter() {
    return new GenericFilterBean() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        chain.doFilter(request, response);
      }
    };
  }

  @Primary
  @Bean("folioPrepareSystemUserService")
  public String dummyFolioPrepareSystemUserService() {
    return "";
  }

  @Primary
  @Bean("folioSystemUserService")
  public String dummyFolioSystemUserService() {
    return "";
  }

  @Primary
  @Bean("folioSystemUserScopedExecutionService")
  public String dummyFolioSystemUserScopedExecutionService() {
    return "";
  }

  @Primary
  @Bean("defaultRetryTemplate")
  public RetryTemplate dummyRetryTemplate() {
    return null;
  }

}
