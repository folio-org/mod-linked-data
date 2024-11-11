package org.folio.linked.data.configuration;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.spring.FolioExecutionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class FeignRequestInterceptor implements RequestInterceptor {

  private final FolioExecutionContext folioExecutionContext;

  @Override
  @SneakyThrows
  public void apply(RequestTemplate template) {
    template.header(TOKEN, Collections.singletonList(folioExecutionContext.getToken()));
    template.header(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
  }
}
