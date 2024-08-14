package org.folio.linked.data.service.tenant.worker;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.DictionaryService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile(FOLIO_PROFILE)
@Log4j2
@Service
@RequiredArgsConstructor
public class DictionaryWorker implements TenantServiceWorker {

  private final DictionaryService dictionaryService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    dictionaryService.init();
    log.info("Dictionaries updated");
  }
}
