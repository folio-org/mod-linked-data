package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.HashSet;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class InstanceSingleWorkEntityValidatorTest {

  private final InstanceSingleWorkEntityValidator validator = new InstanceSingleWorkEntityValidator();

  @Test
  void shouldReturnTrue_ifGivenResourceIsNotInstance() {
    // given
    var resource = new Resource().addTypes(HUB);

    // when
    var result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsLightResource() {
    // given
    var resource = new Resource().addTypes(INSTANCE, LIGHT_RESOURCE);

    // when
    var result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenInstanceHasNoOutgoingEdges() {
    // given
    var resource = new Resource().addTypes(INSTANCE);

    // when
    var result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenInstanceHasEmptyOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setOutgoingEdges(new HashSet<>());

    // when
    var result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenInstanceHasNoInstantiatesEdge() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    var title = new Resource().addTypes(org.folio.ld.dictionary.ResourceTypeDictionary.TITLE);
    instance.addOutgoingEdge(new ResourceEdge(instance, title, TITLE));

    // when
    var result = validator.isValid(instance, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenInstanceHasTwoInstantiatesEdges() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    var work1 = new Resource().addTypes(WORK, BOOKS).setIdAndRefreshEdges(1L);
    var work2 = new Resource().addTypes(WORK, BOOKS).setIdAndRefreshEdges(2L);
    instance.addOutgoingEdge(new ResourceEdge(instance, work1, INSTANTIATES));
    instance.addOutgoingEdge(new ResourceEdge(instance, work2, INSTANTIATES));

    // when
    var result = validator.isValid(instance, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenInstanceHasExactlyOneInstantiatesEdgeToWork() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    var work = new Resource().addTypes(WORK, BOOKS);
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    // when
    var result = validator.isValid(instance, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenInstanceHasInstantiatesEdgeToNonWork() {
    // given
    var instance = new Resource().addTypes(INSTANCE);
    var nonWork = new Resource().addTypes(HUB);
    instance.addOutgoingEdge(new ResourceEdge(instance, nonWork, INSTANTIATES));

    // when
    var result = validator.isValid(instance, null);

    // then
    assertThat(result).isFalse();
  }
}
