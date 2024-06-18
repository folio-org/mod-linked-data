package org.folio.linked.data.service.impl.tenant.workers;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.SEARCH_RESOURCE_NAME;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.client.SearchClient;
import org.folio.linked.data.service.impl.tenant.TenantServiceWorker;
import org.folio.search.domain.dto.CreateIndexRequest;
import org.folio.search.domain.dto.FolioCreateIndexResponse;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile(FOLIO_PROFILE)
public class SearchWorker implements TenantServiceWorker {

  private final SearchClient searchClient;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    try {
      var request = new CreateIndexRequest(SEARCH_RESOURCE_NAME);
      ResponseEntity<FolioCreateIndexResponse> response = searchClient.createIndex(request);
      log.info("Index [{}] creation has been completed with a response [{}]", SEARCH_RESOURCE_NAME, response);
    } catch (Exception e) {
      if (e.getMessage().contains("Index already exists")) {
        log.warn("Index [{}] exists already", SEARCH_RESOURCE_NAME);
      } else {
        log.warn("Index [{}] creation call to mod-search is failed", SEARCH_RESOURCE_NAME, e);
      }
    }
  }
}
