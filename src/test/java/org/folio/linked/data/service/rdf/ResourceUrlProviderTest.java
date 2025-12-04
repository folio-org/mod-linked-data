package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.folio.linked.data.integration.rest.configuration.ConfigurationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceUrlProviderTest {

  @InjectMocks
  private ResourceUrlProvider resourceUrlProvider;
  @Mock
  private ConfigurationService configurationService;

  @BeforeEach
  void setUp() {
    when(configurationService.getFolioHost()).thenReturn("http://localhost");
  }

  @Test
  void returnsCorrectUrlForValidId() {
    // given
    var id = 123L;
    var expectedUrl = "http://localhost/linked-data-editor/resources/123";

    // when
    var result = resourceUrlProvider.apply(id);

    // then
    assertThat(result).isEqualTo(expectedUrl);
  }

}
