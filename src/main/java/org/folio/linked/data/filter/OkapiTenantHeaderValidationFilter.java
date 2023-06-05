package org.folio.linked.data.filter;

import org.folio.spring.filter.TenantOkapiHeaderValidationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("tenantOkapiHeaderValidationFilter")
@ConditionalOnProperty(name = "folio.environment", havingValue = "folio")
public class OkapiTenantHeaderValidationFilter extends TenantOkapiHeaderValidationFilter {
}
