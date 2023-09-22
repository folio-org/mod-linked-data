package org.folio.linked.data.mapper.resource.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class KafkaMessageMapperTest {

  private final KafkaMessageMapper kafkaMessageMapper = new KafkaMessageMapperImpl();

  @Test
  void toIndex_shouldThrowNullPointerException_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    var thrown = assertThrows(NullPointerException.class, () -> kafkaMessageMapper.toIndex(resource));

    // then
    assertThat(thrown.getMessage()).isEqualTo("resource is marked non-null but is null");
  }

  @Test
  void toIndex_shouldThrowNotSupportedException_ifThereIsNoInstance() {
    // given
    var resource = new Resource();
    resource.addType(new ResourceType().setTypeUri("www"));

    // when
    var thrown = assertThrows(NotSupportedException.class, () -> kafkaMessageMapper.toIndex(resource));

    // then
    assertThat(thrown.getMessage()).isEqualTo(
      "Only Monograph.Instance bibframe is supported for now, and there is no Instance found");
  }

  @Test
  void mapToIndex_shouldReturnCorrectlyMappedObject() {
    // given
    var instance = new Resource();
    instance.setResourceHash(randomLong());
    instance.setLabel(UUID.randomUUID().toString());
    instance.addType(new ResourceType().setTypeUri(INSTANCE));
    var identifier = new Resource();
    identifier.setResourceHash(randomLong());
    identifier.setDoc(getJsonNode(Map.of(NAME, List.of(randomLong()))));
    identifier.addType(new ResourceType().setTypeUri(ISBN));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, identifier, new Predicate(MAP_PRED)));
    var publication = new Resource();
    publication.setResourceHash(randomLong());
    publication.setDoc(getJsonNode(Map.of(DATE, List.of("2023"), NAME, List.of(UUID.randomUUID().toString()))));
    publication.addType(new ResourceType().setSimpleLabel(PROVIDER_EVENT));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, publication, new Predicate(PUBLICATION_PRED)));
    instance.setDoc(getJsonNode(Map.of(EDITION_STATEMENT, List.of(UUID.randomUUID().toString()))));

    // when
    var result = kafkaMessageMapper.toIndex(instance);

    // then
    assertThat(result.getId()).isEqualTo(instance.getResourceHash().toString());
    assertThat(result.getTitle()).isEqualTo(instance.getLabel());
    assertThat(result.getIdentifiers().get(0).getValue()).isEqualTo(
      identifier.getDoc().get(NAME).get(0).textValue());
    assertThat(result.getIdentifiers().get(0).getType()).isEqualTo(BibframeIdentifiersInner.TypeEnum.ISBN);
    assertThat(result.getPublications().get(0).getDateOfPublication()).isEqualTo(
      publication.getDoc().get(DATE).get(0).textValue());
    assertThat(result.getPublications().get(0).getPublisher()).isEqualTo(
      publication.getDoc().get(NAME).get(0).textValue());
    assertThat(result.getEditionStatement()).isEqualTo(
      instance.getDoc().get(EDITION_STATEMENT).get(0).textValue());
  }
}
