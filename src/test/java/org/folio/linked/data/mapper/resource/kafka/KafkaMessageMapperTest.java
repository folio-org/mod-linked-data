package org.folio.linked.data.mapper.resource.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.MAIN;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.SUB;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
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
    var title1 = getTitle(INSTANCE_TITLE);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, title1, new Predicate(INSTANCE_TITLE_PRED)));
    var title2 = getTitle(PARALLEL_TITLE);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, title2, new Predicate(INSTANCE_TITLE_PRED)));
    var title3 = getTitle(VARIANT_TITLE);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, title3, new Predicate(INSTANCE_TITLE_PRED)));
    var isbn = getIdentifier(NAME, ISBN);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, isbn, new Predicate(MAP_PRED)));
    var lccn = getIdentifier(NAME, LCCN);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, lccn, new Predicate(MAP_PRED)));
    var ean = getIdentifier(EAN_VALUE, EAN);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, ean, new Predicate(MAP_PRED)));
    var localId = getIdentifier(LOCAL_ID_VALUE, LOCAL_ID);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, localId, new Predicate(MAP_PRED)));
    var otherId = getIdentifier(NAME, OTHER_ID);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, otherId, new Predicate(MAP_PRED)));
    var pub = new Resource();
    pub.setResourceHash(randomLong());
    pub.setDoc(getJsonNode(Map.of(DATE, List.of("2023"), NAME, List.of(UUID.randomUUID().toString()))));
    pub.addType(new ResourceType().setSimpleLabel(PROVIDER_EVENT));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, pub, new Predicate(PUBLICATION_PRED)));
    instance.setDoc(getJsonNode(Map.of(EDITION_STATEMENT, List.of(UUID.randomUUID().toString()))));

    // when
    var result = kafkaMessageMapper.toIndex(instance);

    // then
    assertThat(result.getId()).isEqualTo(instance.getResourceHash().toString());
    assertThat(result.getTitles()).hasSize(6);
    assertTitle(result.getTitles().get(0), title1.getDoc().get(MAIN_TITLE), MAIN);
    assertTitle(result.getTitles().get(1), title1.getDoc().get(SUBTITLE), SUB);
    assertTitle(result.getTitles().get(2), title2.getDoc().get(MAIN_TITLE), MAIN);
    assertTitle(result.getTitles().get(3), title2.getDoc().get(SUBTITLE), SUB);
    assertTitle(result.getTitles().get(4), title3.getDoc().get(MAIN_TITLE), MAIN);
    assertTitle(result.getTitles().get(5), title3.getDoc().get(SUBTITLE), SUB);
    assertThat(result.getIdentifiers()).hasSize(5);
    assertId(result.getIdentifiers().get(0), isbn.getDoc().get(NAME), TypeEnum.ISBN);
    assertId(result.getIdentifiers().get(1), lccn.getDoc().get(NAME), TypeEnum.LCCN);
    assertId(result.getIdentifiers().get(2), ean.getDoc().get(EAN_VALUE), TypeEnum.EAN);
    assertId(result.getIdentifiers().get(3), localId.getDoc().get(LOCAL_ID_VALUE), TypeEnum.LOCALID);
    assertId(result.getIdentifiers().get(4), otherId.getDoc().get(NAME), TypeEnum.UNKNOWN);
    assertThat(result.getPublications().get(0).getDateOfPublication()).isEqualTo(
      pub.getDoc().get(DATE).get(0).textValue());
    assertThat(result.getPublications().get(0).getPublisher()).isEqualTo(pub.getDoc().get(NAME).get(0).textValue());
    assertThat(result.getEditionStatement()).isEqualTo(instance.getDoc().get(EDITION_STATEMENT).get(0).textValue());
  }

  private Resource getTitle(String type) {
    var title = new Resource();
    title.setResourceHash(randomLong());
    title.setDoc(getJsonNode(Map.of(
      MAIN_TITLE, List.of(UUID.randomUUID().toString()),
      SUBTITLE, List.of(UUID.randomUUID().toString())))
    );
    title.addType(new ResourceType().setTypeUri(type));
    return title;
  }

  private Resource getIdentifier(String valueField, String type) {
    var id = new Resource();
    id.setResourceHash(randomLong());
    id.setDoc(getJsonNode(Map.of(valueField, List.of(randomLong()))));
    id.addType(new ResourceType().setTypeUri(type));
    return id;
  }

  private void assertTitle(BibframeTitlesInner titleInner, JsonNode valueNode, BibframeTitlesInner.TypeEnum type) {
    assertThat(titleInner.getValue()).isEqualTo(valueNode.get(0).asText());
    assertThat(titleInner.getType()).isEqualTo(type);
  }

  private void assertId(BibframeIdentifiersInner idInner, JsonNode valueNode, TypeEnum type) {
    assertThat(idInner.getValue()).isEqualTo(valueNode.get(0).asText());
    assertThat(idInner.getType()).isEqualTo(type);
  }
}
