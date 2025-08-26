package org.folio.linked.data.integration;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.linked.data.client.ConfigurationClient;
import org.folio.linked.data.integration.model.Config;
import org.folio.linked.data.integration.model.Configurations;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

  @InjectMocks
  private ConfigurationServiceImpl configurationService;
  @Mock
  private ConfigurationClient configurationClient;

  @Test
  void getFolioHost_returnsFolioHostFromConfigs() {
    // given
    var folioHost = "http://example.com";
    var configurations = getConfigurations("FOLIO_HOST", folioHost);
    when(configurationClient.lookupConfigByModuleName("USERSBL")).thenReturn(configurations);

    // when
    var result = configurationService.getFolioHost();

    // then
    assertThat(result).isEqualTo(folioHost);
  }

  @Test
  void getFolioHost_returnsDefaultFolioHostWhenConfigsAreEmpty() {
    when(configurationClient.lookupConfigByModuleName("USERSBL"))
      .thenReturn(getConfigurations(null, null));

    var result = configurationService.getFolioHost();

    assertThat(result).isEqualTo("http://localhost:8081");
  }

  @Test
  void returnsDefaultFolioHostWhenConfigsDoNotContainFolioHostKey() {
    // given
    when(configurationClient.lookupConfigByModuleName("USERSBL"))
      .thenReturn(getConfigurations("OTHER_KEY", "http://example.com"));

    // when
    var result = configurationService.getFolioHost();

    // then
    assertThat(result).isEqualTo("http://localhost:8081");
  }

  private Configurations getConfigurations(String key, String value) {
    var configurations = new Configurations();
    if (nonNull(key)) {
      var config = new Config();
      config.setCode(key);
      config.setValue(value);
      configurations.setConfigs(List.of(config));
    }
    configurations.setTotalRecords(1);
    return configurations;
  }
}
