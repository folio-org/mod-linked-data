package org.folio.linked.data.configuration.standalone;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.DictionaryService;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@Profile(STANDALONE_PROFILE)
@Log4j2
@RequiredArgsConstructor
public class StandaloneLiquibase {

  private final FolioSpringLiquibase folioSpringLiquibase;
  private final DictionaryService dictionaryService;

  @Value("${mod-linked-data.default-schema}")
  private String defaultSchema;

  @SneakyThrows
  @EventListener(ApplicationReadyEvent.class)
  public void updateDatabase() {
    log.info("Standalone mode is on, activating default DB schema [{}]", defaultSchema);
    folioSpringLiquibase.setDefaultSchema(defaultSchema);
    folioSpringLiquibase.performLiquibaseUpdate();
    dictionaryService.init();
  }
}
