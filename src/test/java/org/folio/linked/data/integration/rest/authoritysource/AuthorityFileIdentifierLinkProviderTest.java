package org.folio.linked.data.integration.rest.authoritysource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import org.folio.linked.data.domain.dto.AuthoritySourceFile;
import org.folio.linked.data.domain.dto.AuthoritySourceFiles;
import org.folio.marc4ld.service.marc2ld.authority.identifier.IdentifierPrefixService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthorityFileIdentifierLinkProviderTest {

  @Mock
  private AuthoritySourceFilesClient authoritySourceFilesClient;
  @Mock
  private IdentifierPrefixService identifierPrefixService;
  @InjectMocks
  private AuthorityFileIdentifierLinkProvider provider;

  @BeforeEach
  void setUp() {
    setField(provider, "authoritySourceFilesLimit", 50);
  }

  @Test
  void getBaseUrl_shouldReturnBaseUrl_whenCodeMatches() {
    // given
    var identifier = "abc123456";
    var file1 = new AuthoritySourceFile().codes(List.of("abc", "def")).baseUrl("http://example.com/abc");
    var file2 = new AuthoritySourceFile().codes(List.of("xyz")).baseUrl("http://example.com/xyz");
    var files = new AuthoritySourceFiles(List.of(file1, file2));
    when(authoritySourceFilesClient.getAuthoritySourceFiles(50)).thenReturn(files);
    when(identifierPrefixService.getIdentifierPrefix(identifier)).thenReturn("abc");

    // when
    var result = provider.getIdentifierLink(identifier);

    // then
    assertThat(result).contains("http://example.com/abc/abc123456");
  }

  @Test
  void getBaseUrl_shouldReturnEmpty_whenNoCodeMatches() {
    // given
    var identifier = "nf1234567";
    var file1 = new AuthoritySourceFile().codes(List.of("abc", "def")).baseUrl("http://example.com/abc");
    var files = new AuthoritySourceFiles(List.of(file1));
    when(authoritySourceFilesClient.getAuthoritySourceFiles(50)).thenReturn(files);
    when(identifierPrefixService.getIdentifierPrefix(identifier)).thenReturn("nf");

    // when
    var result = provider.getIdentifierLink(identifier);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getBaseUrl_shouldSkipNullCodes() {
    // given
    var identifier = "xyz123456";
    var file1 = new AuthoritySourceFile().codes(null).baseUrl("http://example.com/abc");
    var file2 = new AuthoritySourceFile().codes(List.of("xyz")).baseUrl("http://example.com/xyz/");
    var files = new AuthoritySourceFiles(List.of(file1, file2));
    when(authoritySourceFilesClient.getAuthoritySourceFiles(50)).thenReturn(files);
    when(identifierPrefixService.getIdentifierPrefix(identifier)).thenReturn("xyz");

    // when
    var result = provider.getIdentifierLink(identifier);

    // then
    assertThat(result).contains("http://example.com/xyz/xyz123456");
  }

  @Test
  void getBaseUrl_shouldMatchPrefixIgnoringCase() {
    // given
    var identifier = "AbC123456";
    var file1 = new AuthoritySourceFile().codes(List.of("ABC", "def")).baseUrl("http://example.com/abc");
    var files = new AuthoritySourceFiles(List.of(file1));
    when(authoritySourceFilesClient.getAuthoritySourceFiles(50)).thenReturn(files);
    when(identifierPrefixService.getIdentifierPrefix(identifier)).thenReturn("abc");

    // when
    var result = provider.getIdentifierLink(identifier);

    // then
    assertThat(result).contains("http://example.com/abc/AbC123456");
  }

}
