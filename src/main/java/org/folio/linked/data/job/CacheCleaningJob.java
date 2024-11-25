package org.folio.linked.data.job;

import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheCleaningJob {

  @CacheEvict(value = SPEC_RULES, allEntries = true)
  @Scheduled(fixedRateString = "${mod-linked-data.cache.ttl.spec-rules}")
  public void emptySpecRules() {
    log.info("Emptying {} cache", SPEC_RULES);
  }
}
