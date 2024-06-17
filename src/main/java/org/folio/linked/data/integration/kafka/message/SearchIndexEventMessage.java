package org.folio.linked.data.integration.kafka.message;

import java.util.Objects;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.ResourceEvent;
import org.folio.search.domain.dto.ResourceEventType;
import org.folio.spring.tools.kafka.BaseKafkaMessage;

public class SearchIndexEventMessage extends ResourceEvent implements BaseKafkaMessage {

  private String ts;

  public SearchIndexEventMessage withId(String id) {
    super.setId(id);
    return this;
  }

  public SearchIndexEventMessage withType(ResourceEventType type) {
    super.setType(type);
    return this;
  }

  public SearchIndexEventMessage withResourceName(String resourceName) {
    super.setResourceName(resourceName);
    return this;
  }

  public SearchIndexEventMessage withNew(BibframeIndex newBibframeIndex) {
    super.setNew(newBibframeIndex);
    return this;
  }

  public SearchIndexEventMessage withOld(BibframeIndex oldBibframeIndex) {
    super.setOld(oldBibframeIndex);
    return this;
  }

  @Override
  public String getTs() {
    return this.ts;
  }

  @Override
  public void setTs(String ts) {
    this.ts = ts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    SearchIndexEventMessage that = (SearchIndexEventMessage) o;
    return Objects.equals(ts, that.ts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), ts);
  }

}
