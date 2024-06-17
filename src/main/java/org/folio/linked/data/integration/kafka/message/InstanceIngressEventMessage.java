package org.folio.linked.data.integration.kafka.message;

import static java.util.Optional.ofNullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.folio.search.domain.dto.InventoryInstanceIngressEventEventMetadata;
import org.folio.spring.tools.kafka.BaseKafkaMessage;

public class InstanceIngressEventMessage extends InstanceIngressEvent implements BaseKafkaMessage {
  private static final String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";

  public InstanceIngressEventMessage withId(String id) {
    super.setId(id);
    return this;
  }

  public InstanceIngressEventMessage withEventType(EventTypeEnum eventType) {
    super.setEventType(eventType);
    return this;
  }

  public InstanceIngressEventMessage withEventPayload(InstanceIngressPayload eventPayload) {
    super.setEventPayload(eventPayload);
    return this;
  }

  @Override
  public String getTenant() {
    return ofNullable(getEventMetadata())
      .map(InventoryInstanceIngressEventEventMetadata::getTenantId)
      .orElse(null);
  }

  @Override
  public void setTenant(String tenantId) {
    ofNullable(getEventMetadata())
      .orElseGet(() -> {
        setEventMetadata(new InventoryInstanceIngressEventEventMetadata());
        return getEventMetadata();
      })
      .setTenantId(tenantId);
  }

  @Override
  public String getTs() {
    return ofNullable(getEventMetadata())
      .map(InventoryInstanceIngressEventEventMetadata::getPublishedDate)
      .map(new SimpleDateFormat(DATE_PATTERN)::format)
      .orElse(null);
  }

  @Override
  public void setTs(String ts) {
    ofNullable(getEventMetadata())
      .orElseGet(() -> {
        setEventMetadata(new InventoryInstanceIngressEventEventMetadata());
        return getEventMetadata();
      })
      .setPublishedDate(parseDate(ts));
  }

  private Date parseDate(String ts) {
    try {
      return new SimpleDateFormat(DATE_PATTERN).parse(ts);
    } catch (ParseException e) {
      return null;
    }
  }

}
