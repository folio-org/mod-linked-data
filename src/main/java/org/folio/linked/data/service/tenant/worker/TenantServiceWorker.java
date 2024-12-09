package org.folio.linked.data.service.tenant.worker;

import org.folio.tenant.domain.dto.TenantAttributes;

public interface TenantServiceWorker {

  default void beforeTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    // no action by default
  }

  default void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    // no action by default
  }

  default void afterTenantDeletion(String tenantId) {
    // no action by default
  }
}
