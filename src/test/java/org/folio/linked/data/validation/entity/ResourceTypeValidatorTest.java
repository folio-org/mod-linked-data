package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class ResourceTypeValidatorTest {

  private final ResourceTypeValidator validator = new ResourceTypeValidator();

  @ParameterizedTest(name = "[{index}] {2}")
  @MethodSource("provideResourcesForValidation")
  void shouldValidateResourceTypes(Resource resource, boolean expectedValid, String testDescription) {
    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result)
      .as(testDescription)
      .isEqualTo(expectedValid);
  }

  private static Stream<Arguments> provideResourcesForValidation() {
    return Stream.of(
      Arguments.of(
        new Resource(),
        false,
        "Resource without any types should be invalid"
      ),
      Arguments.of(
        new Resource().addTypes(MOCKED_RESOURCE),
        false,
        "Resource with only MOCKED_RESOURCE type should be invalid"
      ),
      Arguments.of(
        new Resource().addTypes(WORK),
        false,
        "Resource with only WORK type should be invalid"
      ),
      Arguments.of(
        new Resource().addTypes(MOCKED_RESOURCE, INSTANCE),
        false,
        "Resource with MOCKED_RESOURCE and another type should be invalid"
      ),
      Arguments.of(
        new Resource().addTypes(INSTANCE),
        true,
        "Resource with INSTANCE type should be valid"
      ),
      Arguments.of(
        new Resource().addTypes(FAMILY),
        true,
        "Resource with FAMILY type should be valid"
      ),
      Arguments.of(
        new Resource().addTypes(HUB, INSTANCE),
        true,
        "Resource with HUB and INSTANCE types should be valid"
      ),
      Arguments.of(
        new Resource().addTypes(WORK, INSTANCE),
        true,
        "Resource with WORK and INSTANCE types should be valid"
      ),
      Arguments.of(
        new Resource().addTypes(HUB, WORK, INSTANCE),
        true,
        "Resource with HUB, WORK and INSTANCE types should be valid"
      ),
      Arguments.of(
        new Resource().addTypes(INSTANCE, FAMILY),
        true,
        "Resource with multiple valid types should be valid"
      )
    );
  }
}

