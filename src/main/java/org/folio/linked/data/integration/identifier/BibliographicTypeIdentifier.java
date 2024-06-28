package org.folio.linked.data.integration.identifier;

import org.folio.marc4ld.util.IdentifierTypes;
import org.springframework.stereotype.Component;

@Component
public class BibliographicTypeIdentifier extends AbstractResourceTypeIdentifier {
  public BibliographicTypeIdentifier() {
    super(IdentifierTypes.BIBLIOGRAPHIC_TYPES);
  }
}
