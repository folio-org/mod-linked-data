package org.folio.linked.data.service.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.messaging.MessageHeaders;

@UnitTest
@ExtendWith(MockitoExtension.class)
class TenantScopedExecutionServiceTest {

  @InjectMocks
  private TenantScopedExecutionService tenantScopedExecutionService;

  @Mock
  private RetryTemplate retryTemplate;
  @Mock
  private ExecutionContextBuilder contextBuilder;
  @Mock
  private FolioExecutionContext folioExecutionContext;

  @Test
  void executeWithRetry_shouldExcludeTokenHeader_whenKeyDiffersByCase() {
    // given
    var headers = new RecordHeaders(List.of(
      new RecordHeader(TENANT, "test-tenant".getBytes()),
      new RecordHeader(TOKEN.toUpperCase(), "test-token".getBytes())
    ));
    var captor = ArgumentCaptor.forClass(MessageHeaders.class);
    when(contextBuilder.forMessageHeaders(captor.capture())).thenReturn(folioExecutionContext);

    // when
    tenantScopedExecutionService.executeWithRetry(headers, () -> null, ex -> {});

    // then
    var capturedHeaders = captor.getValue();
    assertThat(capturedHeaders.containsKey(TOKEN.toUpperCase())).isFalse();
    assertThat(capturedHeaders).containsKey(TENANT);
  }
}
