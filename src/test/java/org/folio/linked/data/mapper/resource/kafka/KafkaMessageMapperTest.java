package org.folio.linked.data.mapper.resource.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.Property.DATE;
import static org.folio.ld.dictionary.Property.EAN_VALUE;
import static org.folio.ld.dictionary.Property.EDITION_STATEMENT;
import static org.folio.ld.dictionary.Property.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.Property.MAIN_TITLE;
import static org.folio.ld.dictionary.Property.NAME;
import static org.folio.ld.dictionary.Property.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.ISBN;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.MAIN;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.SUB;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
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
    resource.addType(new ResourceTypeEntity().setUri("www"));

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
    instance.addType(INSTANCE);
    var title1 = getTitle(TITLE);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, title1,
      new PredicateEntity(PredicateDictionary.TITLE.getUri())));
    var title2 = getTitle(PARALLEL_TITLE);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, title2, new PredicateEntity(PredicateDictionary.TITLE.getUri())));
    var title3 = getTitle(VARIANT_TITLE);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, title3, new PredicateEntity(PredicateDictionary.TITLE.getUri())));
    var isbn = getIdentifier(NAME, ID_ISBN);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, isbn, new PredicateEntity(PredicateDictionary.MAP.getUri())));
    var lccn = getIdentifier(NAME, ID_LCCN);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, lccn, new PredicateEntity(PredicateDictionary.MAP.getUri())));
    var ean = getIdentifier(EAN_VALUE, ID_EAN);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, ean, new PredicateEntity(PredicateDictionary.MAP.getUri())));
    var localId = getIdentifier(LOCAL_ID_VALUE, ID_LOCAL);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, localId, new PredicateEntity(PredicateDictionary.MAP.getUri())));
    var otherId = getIdentifier(NAME, ID_UNKNOWN);
    instance.getOutgoingEdges()
      .add(new ResourceEdge(instance, otherId, new PredicateEntity(PredicateDictionary.MAP.getUri())));
    var pub = new Resource();
    pub.setResourceHash(randomLong());
    pub.setDoc(getJsonNode(Map.of(DATE, List.of("2023"), NAME, List.of(UUID.randomUUID().toString()))));
    pub.addType(new ResourceTypeEntity().setSimpleLabel(PROVIDER_EVENT.getUri()));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, pub, new PredicateEntity(PE_PUBLICATION.getUri())));
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
    assertId(result.getIdentifiers().get(0), isbn.getDoc().get(NAME), ISBN);
    assertId(result.getIdentifiers().get(1), lccn.getDoc().get(NAME), TypeEnum.LCCN);
    assertId(result.getIdentifiers().get(2), ean.getDoc().get(EAN_VALUE), TypeEnum.EAN);
    assertId(result.getIdentifiers().get(3), localId.getDoc().get(LOCAL_ID_VALUE), TypeEnum.LOCALID);
    assertId(result.getIdentifiers().get(4), otherId.getDoc().get(NAME), TypeEnum.UNKNOWN);
    assertThat(result.getPublications().get(0).getDateOfPublication()).isEqualTo(
      pub.getDoc().get(DATE).get(0).textValue());
    assertThat(result.getPublications().get(0).getPublisher()).isEqualTo(pub.getDoc().get(NAME).get(0).textValue());
    assertThat(result.getEditionStatement()).isEqualTo(instance.getDoc().get(EDITION_STATEMENT).get(0).textValue());
  }

  private Resource getTitle(ResourceTypeDictionary type) {
    var title = new Resource();
    title.setResourceHash(randomLong());
    title.setDoc(getJsonNode(Map.of(
      MAIN_TITLE, List.of(UUID.randomUUID().toString()),
      SUBTITLE, List.of(UUID.randomUUID().toString())))
    );
    title.addType(type);
    return title;
  }

  private Resource getIdentifier(String valueField, ResourceTypeDictionary type) {
    var id = new Resource();
    id.setResourceHash(randomLong());
    id.setDoc(getJsonNode(Map.of(valueField, List.of(randomLong()))));
    id.addType(type);
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
