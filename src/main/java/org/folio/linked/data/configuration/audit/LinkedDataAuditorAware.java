package org.folio.linked.data.configuration.audit;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.spring.FolioExecutionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
@RequiredArgsConstructor
public class LinkedDataAuditorAware implements AuditorAware<UUID> {

  private final FolioExecutionContext folioExecutionContext;

  @Override
  public Optional<UUID> getCurrentAuditor() {
    return Optional.ofNullable(folioExecutionContext.getUserId());
  }
}
