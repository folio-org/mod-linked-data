package org.folio.linked.data.service.impl.tenant;

import org.folio.tenant.domain.dto.TenantAttributes;

public interface TenantServiceWorker {

  default void beforeTenantUpdate(TenantAttributes tenantAttributes) {
    // no action by default
  }

  default void afterTenantUpdate(TenantAttributes tenantAttributes) {
    // no action by default
  }
}
