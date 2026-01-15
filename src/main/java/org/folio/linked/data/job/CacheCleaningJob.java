package org.folio.linked.data.job;

import static org.folio.linked.data.util.Constants.Cache.AUTHORITY_SOURCE_FILES;
import static org.folio.linked.data.util.Constants.Cache.MODULE_STATE;
import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;
import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;
import static org.folio.linked.data.util.Constants.EMPTY_CACHE_MSG;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class CacheCleaningJob {

  @CacheEvict(value = SPEC_RULES, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.spec-rules}")
  public void emptySpecRules() {
    log.info(EMPTY_CACHE_MSG, SPEC_RULES);
  }

  @CacheEvict(value = SETTINGS_ENTRIES, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.settings-entries}")
  public void emptySettingsEntries() {
    log.info(EMPTY_CACHE_MSG, SETTINGS_ENTRIES);
  }

  @CacheEvict(value = MODULE_STATE, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.module-state}")
  public void emptyModuleState() {
    log.info(EMPTY_CACHE_MSG, MODULE_STATE);
  }

  @CacheEvict(value = AUTHORITY_SOURCE_FILES, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.authority-source-files}")
  public void emptyAuthoritySourceFiles() {
    log.info(EMPTY_CACHE_MSG, AUTHORITY_SOURCE_FILES);
  }
}
