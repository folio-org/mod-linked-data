package org.folio.linked.data.integration.rest.authoritysource;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AuthoritySourceFile;
import org.folio.marc4ld.service.marc2ld.authority.identifier.IdentifierLinkProvider;
import org.folio.marc4ld.service.marc2ld.authority.identifier.IdentifierPrefixService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service("identifierLinkProvider")
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class AuthorityFileIdentifierLinkProvider implements IdentifierLinkProvider {
  private final AuthoritySourceFilesClient authoritySourceFilesClient;
  private final IdentifierPrefixService identifierPrefixService;
  @Value("${mod-linked-data.authority-source-files.limit}")
  private int authoritySourceFilesLimit;

  @Override
  public Optional<String> getIdentifierLink(String identifier) {
    var prefix = identifierPrefixService.getIdentifierPrefix(identifier);
    var authoritySourceFiles = authoritySourceFilesClient.getAuthoritySourceFiles(authoritySourceFilesLimit);

    return authoritySourceFiles.getAuthoritySourceFiles().stream()
      .filter(source -> nonNull(source.getCodes()))
      .filter(source -> source.getCodes().stream().anyMatch(prefix::equalsIgnoreCase))
      .map(AuthoritySourceFile::getBaseUrl)
      .map(baseUrl -> this.createLink(baseUrl, identifier))
      .findFirst();
  }

  private String createLink(String baseUrl, String identifier) {
    if (!baseUrl.endsWith("/")) {
      baseUrl = baseUrl + "/";
    }
    return baseUrl + identifier;
  }
}
