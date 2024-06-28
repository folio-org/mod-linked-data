package org.folio.linked.data.integration.identifier;

import org.folio.marc4ld.util.IdentifierTypes;
import org.springframework.stereotype.Component;

@Component
public class AuthorityTypeIdentifier extends AbstractResourceTypeIdentifier {
  public AuthorityTypeIdentifier() {
    super(IdentifierTypes.AUTHORITY_TYPES);
  }
}
