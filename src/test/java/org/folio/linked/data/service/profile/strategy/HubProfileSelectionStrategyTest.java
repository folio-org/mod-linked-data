package org.folio.linked.data.service.profile.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class HubProfileSelectionStrategyTest {

  private HubProfileSelectionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new HubProfileSelectionStrategy();
  }

  @ParameterizedTest
  @CsvSource({
    "INSTANCE, false",
    "WORK, false",
    "HUB, true"
  })
  void supports_shouldReturnExpectedResult(ResourceTypeDictionary type, boolean expected) {
    // given
    var resource = new Resource().addTypes(type);

    // when
    var result = strategy.supports(resource);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void selectProfile_shouldReturnHubProfileId() {
    // given
    var resource = new Resource().addTypes(HUB);

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(7);
  }
}
