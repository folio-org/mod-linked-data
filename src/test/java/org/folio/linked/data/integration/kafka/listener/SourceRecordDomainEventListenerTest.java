package org.folio.linked.data.integration.kafka.listener;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.integration.kafka.listener.handler.srs.SourceRecordDomainEventHandler;
import org.folio.linked.data.service.tenant.LinkedDataTenantService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SourceRecordDomainEventListenerTest {

  private static final String RECORD_TYPE = "folio.srs.recordType";

  @InjectMocks
  private SourceRecordDomainEventListener listener;

  @Mock
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Mock
  private SourceRecordDomainEventHandler sourceRecordDomainEventHandler;
  @Mock
  private LinkedDataTenantService linkedDataTenantService;

  @Test
  void handleSourceRecordDomainEvent_shouldProcessRecord_whenAllRequiredHeadersPresentWithMixedCase() {
    // given
    var tenant = "test-tenant";
    var event = new SourceRecordDomainEvent().id("1");
    var consumerRecord = new ConsumerRecord<String, SourceRecordDomainEvent>("topic", 1, 1, "key", event);
    var headers = new RecordHeaders(List.of(
      new RecordHeader(TENANT.toUpperCase(), tenant.getBytes()),
      new RecordHeader(URL.toUpperCase(), "http://okapi:9130".getBytes()),
      new RecordHeader(RECORD_TYPE.toUpperCase(), "MARC_BIB".getBytes())
    ));
    ReflectionTestUtils.setField(consumerRecord, "headers", headers);
    when(linkedDataTenantService.isTenantExists(tenant)).thenReturn(true);

    // when
    listener.handleSourceRecordDomainEvent(List.of(consumerRecord));

    // then
    verify(tenantScopedExecutionService).executeWithRetry(any(), any(), any());
  }

  @Test
  void handleSourceRecordDomainEvent_shouldIgnoreRecord_whenRequiredHeaderHasEmptyValue() {
    // given
    var tenant = "test-tenant";
    var event = new SourceRecordDomainEvent().id("2");
    var consumerRecord = new ConsumerRecord<String, SourceRecordDomainEvent>("topic", 1, 1, "key", event);
    var headers = new RecordHeaders(List.of(
      new RecordHeader(TENANT, tenant.getBytes()),
      new RecordHeader(URL, "http://okapi:9130".getBytes()),
      new RecordHeader(RECORD_TYPE, new byte[0])
    ));
    ReflectionTestUtils.setField(consumerRecord, "headers", headers);
    when(linkedDataTenantService.isTenantExists(tenant)).thenReturn(true);

    // when
    listener.handleSourceRecordDomainEvent(List.of(consumerRecord));

    // then
    verifyNoInteractions(tenantScopedExecutionService);
  }

  @Test
  void handleSourceRecordDomainEvent_shouldIgnoreRecord_whenRequiredHeaderIsMissing() {
    // given
    var tenant = "test-tenant";
    var event = new SourceRecordDomainEvent().id("3");
    var consumerRecord = new ConsumerRecord<String, SourceRecordDomainEvent>("topic", 1, 1, "key", event);
    var headers = new RecordHeaders(List.of(
      new RecordHeader(TENANT, tenant.getBytes()),
      new RecordHeader(URL, "http://okapi:9130".getBytes())
    ));
    ReflectionTestUtils.setField(consumerRecord, "headers", headers);
    when(linkedDataTenantService.isTenantExists(tenant)).thenReturn(true);

    // when
    listener.handleSourceRecordDomainEvent(List.of(consumerRecord));

    // then
    verifyNoInteractions(tenantScopedExecutionService);
  }
}
