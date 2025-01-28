package org.folio.linked.data.job;

import static org.folio.linked.data.util.Constants.Cache.SETTINGS_ENTRIES;
import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;

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
    log.info("Emptying {} cache", SPEC_RULES);
  }

  @CacheEvict(value = SETTINGS_ENTRIES, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.settings-entries}")
  public void emptySettingsEntries() {
    log.info("Emptying {} cache", SETTINGS_ENTRIES);
  }
}
