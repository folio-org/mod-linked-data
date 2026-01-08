package org.folio.linked.data.integration.rest.authoritysource;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AuthoritySourceFile;
import org.folio.marc4ld.service.marc2ld.authority.identifier.IdentifierUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service("identifierUrlProvider")
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class AuthorityFileIdentifierUrlProvider implements IdentifierUrlProvider {
  private final AuthoritySourceFilesClient authoritySourceFilesClient;
  @Value("${mod-linked-data.authority-source-files.limit}")
  private int authoritySourceFilesLimit;

  @Override
  public Optional<String> getBaseUrl(String identifierPrefix) {
    var authoritySourceFiles = authoritySourceFilesClient.getAuthoritySourceFiles(authoritySourceFilesLimit);

    return authoritySourceFiles.getAuthoritySourceFiles().stream()
        .filter(source -> source.getCodes().stream().anyMatch(identifierPrefix::equals))
        .map(AuthoritySourceFile::getBaseUrl)
        .findFirst();
  }
}
