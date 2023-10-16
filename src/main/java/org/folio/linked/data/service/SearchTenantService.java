package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.linked.data.client.SearchClient;
import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.search.domain.dto.FolioCreateIndexResponse;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
@Profile(SEARCH_PROFILE)
public class SearchTenantService extends TenantService {

  private final SearchClient searchClient;

  public SearchTenantService(JdbcTemplate jdbcTemplate, FolioExecutionContext context,
                             FolioSpringLiquibase folioSpringLiquibase, SearchClient searchClient) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.searchClient = searchClient;
  }

  @Override
  public void afterTenantUpdate(TenantAttributes tenantAttributes) {
    log.log(Level.INFO, "Search feature is on, creating index [{}] for the tenant [{}]", SEARCH_RESOURCE_NAME,
      context.getTenantId());
    try {
      var request = new CreateIndexRequest(SEARCH_RESOURCE_NAME);
      ResponseEntity<FolioCreateIndexResponse> response = searchClient.createIndex(request);
      log.info("Index [{}] creation for the tenant [{}] has been completed with a response [{}]", SEARCH_RESOURCE_NAME,
        context.getTenantId(), response);
    } catch (Exception e) {
      if (e.getMessage().contains("Index already exists")) {
        log.warn("Index [{}] exists already for tenant [{}]", SEARCH_RESOURCE_NAME, context.getTenantId());
      } else {
        log.warn("Index [{}] creation call to mod-search for tenant [{}] is failed", SEARCH_RESOURCE_NAME,
          context.getTenantId(), e);
      }
    }
  }

}
