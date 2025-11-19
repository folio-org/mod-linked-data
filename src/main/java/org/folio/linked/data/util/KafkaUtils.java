package org.folio.linked.data.util;

import static java.util.Optional.ofNullable;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import io.vertx.core.Handler;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.Logger;
import org.folio.linked.data.service.tenant.LinkedDataTenantService;

@UtilityClass
public class KafkaUtils {

  public static Optional<String> getHeaderValueByName(ConsumerRecord<String, ?> consumerRecord, String headerName) {
    return ofNullable(consumerRecord.headers().lastHeader(headerName))
      .map(header -> new String(header.value()));
  }

  public static <T> void handleForExistedTenant(ConsumerRecord<String, T> consumerRecord,
                                                String eventId,
                                                LinkedDataTenantService linkedDataTenantService,
                                                Logger log,
                                                Handler<ConsumerRecord<String, T>> handler) {
    var event = consumerRecord.value();
    var eventName = event.getClass().getSimpleName();
    var tenant = getHeaderValueByName(consumerRecord, TENANT)
      .orElseThrow(() -> new IllegalArgumentException("Received %s [id %s] is missing x-okapi-tenant header"
        .formatted(eventName, eventId)));
    boolean tenantExists = linkedDataTenantService.isTenantExists(tenant);
    if (tenantExists) {
      handler.handle(consumerRecord);
    } else {
      log.debug("Received {} [id {}] will be ignored since module is not installed on tenant {}",
        eventName, eventId, tenant);
    }
  }
}
