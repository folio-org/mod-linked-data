package org.folio.linked.data.configuration.audit;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LinkedDataAuditEntityListener extends AuditingEntityListener {

  private ObjectFactory<AuditingHandler> handler;

  @Override
  @PrePersist
  public void touchForCreate(Object target) {
    if (target instanceof Resource resource && isNull(resource.getCreatedBy())) {
      ofNullable(handler)
        .map(ObjectFactory::getObject)
        .ifPresent(object -> object.markCreated(target));
    }
  }
}
