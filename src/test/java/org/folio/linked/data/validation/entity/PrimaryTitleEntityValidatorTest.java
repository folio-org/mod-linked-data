package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.createPrimaryTitle;

import java.util.HashSet;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class PrimaryTitleEntityValidatorTest {

  private final PrimaryTitleEntityValidator validator = new PrimaryTitleEntityValidator();

  @Test
  void shouldReturnTrue_ifGivenResourceIsNotInstanceOrWork() {
    // given
    var resource = new Resource();

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsInstanceWithNullOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(INSTANCE);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsInstanceWithEmptyOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setOutgoingEdges(new HashSet<>());

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsInstanceAndSeriesWithEmptyOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(INSTANCE, SERIES)
      .setOutgoingEdges(new HashSet<>());

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsWorkAndSeriesWithEmptyOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(WORK, SERIES)
      .setOutgoingEdges(new HashSet<>());

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsInstanceWithEdgesContainsNoPrimaryTitle() {
    // given
    var edges = new HashSet<ResourceEdge>();
    edges.add(new ResourceEdge(new ResourceEdge(new Resource(), new Resource(), PredicateDictionary.TITLE)));
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsInstanceWithEdgesContainPrimaryTitleWithNoMainTitleProperty() {
    // given
    var edges = new HashSet<ResourceEdge>();
    var title = new Resource().addTypes(TITLE);
    edges.add(new ResourceEdge(new ResourceEdge(new Resource(), title, PredicateDictionary.TITLE)));
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsInstanceWithEdgesContainPrimaryTitleWithMainTitle() {
    // given
    var edges = new HashSet<ResourceEdge>();
    var title = createPrimaryTitle(1L);
    edges.add(new ResourceEdge(new Resource(), title, PredicateDictionary.TITLE));
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsWorkWithNullOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(WORK);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsWorkWithEmptyOutgoingEdges() {
    // given
    var resource = new Resource()
      .addTypes(WORK)
      .setOutgoingEdges(new HashSet<>());

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsWorkWithEdgesContainsNoPrimaryTitle() {
    // given
    var edges = new HashSet<ResourceEdge>();
    edges.add(new ResourceEdge(new ResourceEdge(new Resource(), new Resource(), PredicateDictionary.TITLE)));
    var resource = new Resource()
      .addTypes(WORK)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenResourceIsWorkWithEdgesContainPrimaryTitleWithNoMainTitleProperty() {
    // given
    var edges = new HashSet<ResourceEdge>();
    var title = new Resource().addTypes(TITLE);
    edges.add(new ResourceEdge(new ResourceEdge(new Resource(), title, PredicateDictionary.TITLE)));
    var resource = new Resource()
      .addTypes(WORK)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenResourceIsWorkWithEdgesContainPrimaryTitleWithMainTitle() {
    // given
    var edges = new HashSet<ResourceEdge>();
    var title = createPrimaryTitle(1L);
    edges.add(new ResourceEdge(new Resource(), title, PredicateDictionary.TITLE));
    var resource = new Resource()
      .addTypes(WORK)
      .setOutgoingEdges(edges);

    // when
    boolean result = validator.isValid(resource, null);

    // then
    assertThat(result).isTrue();
  }

}
