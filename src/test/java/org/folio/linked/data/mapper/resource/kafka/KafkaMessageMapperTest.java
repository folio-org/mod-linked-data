package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.test.MonographTestUtil.createSampleInstance;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.ORGANIZATION;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.PERSON;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.EAN;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.ISBN;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.LCCN;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.LOCALID;
import static org.folio.search.domain.dto.BibframeIdentifiersInner.TypeEnum.UNKNOWN;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.MAIN;
import static org.folio.search.domain.dto.BibframeTitlesInner.TypeEnum.SUB;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.CarrierMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaMessageMapperTest {

  @InjectMocks
  private KafkaMessageMapperImpl kafkaMessageMapper;
  @Mock
  private SubResourceMapper subResourceMapper;

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
    Set.of(
      ID_ISBN.getUri(),
      ID_LCCN.getUri(),
      ID_EAN.getUri(),
      ID_LOCAL.getUri(),
      ID_UNKNOWN.getUri(),
      ResourceTypeDictionary.PERSON.getUri(),
      ResourceTypeDictionary.ORGANIZATION.getUri()
    ).forEach(t ->
      lenient().when(subResourceMapper.getMapperUnit(eq(t), any(), any(), any()))
        .thenReturn(of(new CarrierMapperUnit(null)))
    );

    var instance = createSampleInstance();
    var emptyTitle = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyTitle, TITLE));
    var wrongId = getIdentifier(NAME.getValue(), ANNOTATION);
    var emptyId = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, wrongId, MAP));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyId, MAP));
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    instance.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .forEach(re -> re.getTarget().getOutgoingEdges().add(new ResourceEdge(instance, wrongContributor, CONTRIBUTOR)));
    instance.getOutgoingEdges().stream()
      .filter(re -> INSTANTIATES.getUri().equals(re.getPredicate().getUri()))
      .forEach(re -> re.getTarget().getOutgoingEdges().add(new ResourceEdge(instance, emptyContributor, CONTRIBUTOR)));
    var emptyPublication = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyPublication, PE_PUBLICATION));

    // when
    var result = kafkaMessageMapper.toIndex(instance);

    // then
    assertThat(result.getId()).isEqualTo(instance.getResourceHash().toString());
    assertThat(result.getTitles()).hasSize(6);
    assertTitle(result.getTitles().get(0), "Instance: mainTitle", MAIN);
    assertTitle(result.getTitles().get(1), "Instance: subTitle", SUB);
    assertTitle(result.getTitles().get(2), "Parallel: mainTitle", MAIN);
    assertTitle(result.getTitles().get(3), "Parallel: subTitle", SUB);
    assertTitle(result.getTitles().get(4), "Variant: mainTitle", MAIN);
    assertTitle(result.getTitles().get(5), "Variant: subTitle", SUB);
    assertThat(result.getIdentifiers()).hasSize(6);
    assertId(result.getIdentifiers().get(0), "lccn value", LCCN);
    assertId(result.getIdentifiers().get(1), "isbn value", ISBN);
    assertId(result.getIdentifiers().get(2), "ean value", EAN);
    assertId(result.getIdentifiers().get(3), "localId value", LOCALID);
    assertId(result.getIdentifiers().get(4), "otherId value", UNKNOWN);
    assertId(result.getIdentifiers().get(5), wrongId.getDoc().get(NAME.getValue()).get(0).asText(), null);
    assertThat(result.getPublications()).hasSize(1);
    assertThat(result.getPublications().get(0).getDateOfPublication()).isEqualTo("publication date");
    assertThat(result.getPublications().get(0).getPublisher()).isEqualTo("publication name");
    assertThat(result.getContributors()).hasSize(3);
    assertContributor(result.getContributors().get(0), "Person: name", PERSON, true);
    assertContributor(result.getContributors().get(1), "Organization: name", ORGANIZATION, false);
    assertContributor(result.getContributors().get(2), wrongContributor.getDoc().get(NAME.getValue()).get(0).asText(),
      null, false);
    assertThat(result.getEditionStatement()).isEqualTo(
      instance.getDoc().get(EDITION_STATEMENT.getValue()).get(0).textValue());
  }

  private Resource getIdentifier(String valueField, ResourceTypeDictionary type) {
    var id = new Resource();
    id.setResourceHash(randomLong());
    id.setDoc(getJsonNode(Map.of(valueField, List.of(randomLong()))));
    id.addType(type);
    return id;
  }

  private Resource getContributor(ResourceTypeDictionary type) {
    var contributor = new Resource();
    contributor.setResourceHash(randomLong());
    contributor.setDoc(getJsonNode(Map.of(NAME.getValue(), List.of(UUID.randomUUID().toString()))));
    contributor.addType(type);
    return contributor;
  }

  private void assertTitle(BibframeTitlesInner titleInner, String value, BibframeTitlesInner.TypeEnum type) {
    assertThat(titleInner.getValue()).isEqualTo(value);
    assertThat(titleInner.getType()).isEqualTo(type);
  }

  private void assertId(BibframeIdentifiersInner idInner, String value, TypeEnum type) {
    assertThat(idInner.getValue()).isEqualTo(value);
    assertThat(idInner.getType()).isEqualTo(type);
  }

  private void assertContributor(BibframeContributorsInner contributorInner, String value,
                                 BibframeContributorsInner.TypeEnum type, boolean isCreator) {
    assertThat(contributorInner.getName()).isEqualTo(value);
    assertThat(contributorInner.getType()).isEqualTo(type);
    assertThat(contributorInner.getIsCreator()).isEqualTo(isCreator);
  }
}
