package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.TestUtil.getJsonNode;

import java.util.List;
import java.util.Map;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AuthorityNameEntityValidatorTest {

  private final AuthorityNameEntityValidator validator = new AuthorityNameEntityValidator();

  @Test
  void shouldReturnTrue_ifGivenResourceIsNotAuthority() {
    // given
    var resource = new Resource().addTypes(ID_ISBN);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsHub() {
    // given
    var resource = new Resource().addTypes(HUB);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsConcept() {
    // given
    var resource = new Resource().addTypes(CONCEPT);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenAuthorityResourceHasNullDoc() {
    // given
    var resource = new Resource().addTypes(PERSON);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenAuthorityResourceDocHasNoNameProperty() {
    // given
    var resource = new Resource()
      .addTypes(PERSON)
      .setDoc(getJsonNode(Map.of()));

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenAuthorityResourceDocHasEmptyNameProperty() {
    // given
    var resource = new Resource()
      .addTypes(PERSON)
      .setDoc(getJsonNode(Map.of(NAME.getValue(), List.of())));

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenAuthorityResourceDocHasNonEmptyNameProperty() {
    // given
    var resource = new Resource()
      .addTypes(PERSON)
      .setDoc(getJsonNode(Map.of(NAME.getValue(), List.of("Authority Name"))));

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }
}
