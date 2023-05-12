package org.folio.linked.data.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import java.util.Collections;
import java.util.UUID;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FeignRequestInterceptorTest {

  @InjectMocks
  private FeignRequestInterceptor feignRequestInterceptor;
  @Mock
  private FolioExecutionContext folioExecutionContext;

  @Test
  void apply_shouldSetTokenAndTenantHeaders() {
    // given
    var requestTemplate = new RequestTemplate();
    var tenantId = UUID.randomUUID().toString();
    var token = UUID.randomUUID().toString();
    when(folioExecutionContext.getTenantId()).thenReturn(tenantId);
    when(folioExecutionContext.getToken()).thenReturn(token);

    // when
    feignRequestInterceptor.apply(requestTemplate);

    // then
    assertThat(requestTemplate.headers()).contains(entry(TENANT, Collections.singletonList(tenantId)));
    assertThat(requestTemplate.headers()).contains(entry(TOKEN, Collections.singletonList(token)));
  }
}

