package org.folio.linked.data.integration.rest.authoritysource;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Optional;
import org.folio.marc4ld.service.marc2ld.authority.identifier.IdentifierLinkProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile(STANDALONE_PROFILE)
@Service("identifierLinkProvider")
public class IdentifierLinkProviderStandalone implements IdentifierLinkProvider {
  @Override
  public Optional<String> getIdentifierLink(String identifier) {
    return Optional.empty();
  }
}
