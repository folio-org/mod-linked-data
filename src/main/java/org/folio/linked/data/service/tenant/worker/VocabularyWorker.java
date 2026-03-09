package org.folio.linked.data.service.tenant.worker;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.vocabulary.VocabularyService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Order(4)
@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class VocabularyWorker implements TenantServiceWorker {

  private final VocabularyService vocabularyService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    log.info("Saving vocabularies for tenant: {}", tenantId);
    vocabularyService.saveAllVocabularies();
  }
}
