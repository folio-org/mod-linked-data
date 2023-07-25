package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.INDEX_NAME;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import feign.FeignException;
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
@Profile(FOLIO_PROFILE + " & " + SEARCH_PROFILE)
public class LinkedDataTenantService extends TenantService {

  private final SearchClient searchClient;

  public LinkedDataTenantService(JdbcTemplate jdbcTemplate, FolioExecutionContext context,
                                 FolioSpringLiquibase folioSpringLiquibase, SearchClient searchClient) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.searchClient = searchClient;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    log.log(Level.INFO, "Search feature is on, creating index [{}] for the tenant [{}]", INDEX_NAME,
      context.getTenantId());
    try {
      ResponseEntity<FolioCreateIndexResponse> response = searchClient.createIndex(new CreateIndexRequest(INDEX_NAME));
      log.info("Index [{}] creation for the tenant [{}] has been completed with a response [{}]", INDEX_NAME,
        context.getTenantId(), response);
    } catch (FeignException fe) {
      if (fe.getMessage().contains("Index already exists")) {
        log.warn("Index [{}] exists already for tenant [{}]", INDEX_NAME, context.getTenantId());
      } else {
        throw fe;
      }
    }
  }

}
