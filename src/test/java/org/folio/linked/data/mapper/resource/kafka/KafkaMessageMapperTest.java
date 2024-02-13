package org.folio.linked.data.mapper.resource.kafka;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.FAMILY;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.MEETING;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.ORGANIZATION;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.PERSON;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.EAN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.ISBN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.LCCN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.LOCALID;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.UNKNOWN;
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
import org.folio.linked.data.mapper.resource.common.SingleResourceMapper;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
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
  private SingleResourceMapper singleResourceMapper;

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
  void mapToIndex_shouldReturnCorrectlyMappedObject() {
    // given
    Set.of(
      ID_ISBN.getUri(),
      ID_LCCN.getUri(),
      ID_EAN.getUri(),
      ID_LOCAL.getUri(),
      ID_UNKNOWN.getUri(),
      ResourceTypeDictionary.PERSON.getUri(),
      ResourceTypeDictionary.MEETING.getUri(),
      ResourceTypeDictionary.FAMILY.getUri(),
      ResourceTypeDictionary.ORGANIZATION.getUri()
    ).forEach(t ->
      lenient().when(singleResourceMapper.getMapperUnit(eq(t), any(), any(), any())).thenReturn(of(genericMapper()))
    );

    var instance = getSampleInstanceResource();
    var emptyTitle = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyTitle, TITLE));
    var wrongId = getIdentifier(NAME.getValue(), ANNOTATION);
    var emptyId = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, wrongId, MAP));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyId, MAP));
    var emptyPublication = new Resource();
    instance.getOutgoingEdges().add(new ResourceEdge(instance, emptyPublication, PE_PUBLICATION));
    var work = getSampleWork(instance);
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    work.getOutgoingEdges().add(new ResourceEdge(instance, wrongContributor, CONTRIBUTOR));
    work.getOutgoingEdges().add(new ResourceEdge(instance, emptyContributor, CONTRIBUTOR));

    // when
    var resultOpt = kafkaMessageMapper.toIndex(work);

    // then
    assertThat(resultOpt).isPresent();
    var result = resultOpt.get();
    assertThat(result.getId()).isEqualTo(work.getResourceHash().toString());
    assertThat(result.getTitles()).isEmpty();
    assertThat(result.getContributors()).hasSize(9);
    assertContributor(result.getContributors().get(0), "name-PERSON", PERSON, true);
    assertContributor(result.getContributors().get(1), "name-MEETING", MEETING, true);
    assertContributor(result.getContributors().get(2), "name-ORGANIZATION", ORGANIZATION, true);
    assertContributor(result.getContributors().get(3), "name-FAMILY", FAMILY, true);
    assertContributor(result.getContributors().get(4), "name-PERSON", PERSON, false);
    assertContributor(result.getContributors().get(5), "name-MEETING", MEETING, false);
    assertContributor(result.getContributors().get(6), "name-ORGANIZATION", ORGANIZATION, false);
    assertContributor(result.getContributors().get(7), "name-FAMILY", FAMILY, false);
    assertContributor(result.getContributors().get(8), wrongContributor.getDoc().get(NAME.getValue()).get(0).asText(),
      null, false);
    assertThat(result.getLanguages()).hasSize(1);
    assertThat(result.getLanguages().get(0).getValue())
      .isEqualTo(work.getDoc().get(LANGUAGE.getValue()).get(0).asText());
    assertThat(result.getClassifications()).hasSize(1);
    assertThat(result.getClassifications().get(0).getNumber()).isEqualTo("709.83");
    assertThat(result.getClassifications().get(0).getSource()).isEqualTo("ddc");
    assertThat(result.getSubjects()).hasSize(2);
    assertThat(result.getSubjects().get(0).getValue()).isEqualTo("subject 1");
    assertThat(result.getSubjects().get(1).getValue()).isEqualTo("subject 2");
    assertThat(result.getInstances()).hasSize(1);
    var instanceIndex = result.getInstances().get(0);
    assertThat(instanceIndex.getId()).isEqualTo(instance.getResourceHash().toString());
    assertTitle(instanceIndex.getTitles().get(0), "Instance: mainTitle", MAIN);
    assertTitle(instanceIndex.getTitles().get(1), "Instance: subTitle", SUB);
    assertTitle(instanceIndex.getTitles().get(2), "Parallel: mainTitle", MAIN);
    assertTitle(instanceIndex.getTitles().get(3), "Parallel: subTitle", SUB);
    assertTitle(instanceIndex.getTitles().get(4), "Variant: mainTitle", MAIN);
    assertTitle(instanceIndex.getTitles().get(5), "Variant: subTitle", SUB);
    assertThat(instanceIndex.getIdentifiers()).hasSize(6);
    assertId(instanceIndex.getIdentifiers().get(0), "lccn value", LCCN);
    assertId(instanceIndex.getIdentifiers().get(1), "isbn value", ISBN);
    assertId(instanceIndex.getIdentifiers().get(2), "ean value", EAN);
    assertId(instanceIndex.getIdentifiers().get(3), "localId value", LOCALID);
    assertId(instanceIndex.getIdentifiers().get(4), "otherId value", UNKNOWN);
    assertId(instanceIndex.getIdentifiers().get(5), wrongId.getDoc().get(NAME.getValue()).get(0).asText(), null);
    assertThat(instanceIndex.getContributors()).isEmpty();
    assertThat(instanceIndex.getPublications()).hasSize(1);
    assertThat(instanceIndex.getPublications().get(0).getDate()).isNull();
    assertThat(instanceIndex.getPublications().get(0).getName()).isEqualTo("publication name");
    assertThat(instanceIndex.getEditionStatements()).hasSize(1);
    assertThat(instanceIndex.getEditionStatements().get(0).getValue())
      .isEqualTo(instance.getDoc().get(EDITION_STATEMENT.getValue()).get(0).asText());
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

  private void assertId(BibframeInstancesInnerIdentifiersInner idInner, String value, TypeEnum type) {
    assertThat(idInner.getValue()).isEqualTo(value);
    assertThat(idInner.getType()).isEqualTo(type);
  }

  private void assertContributor(BibframeContributorsInner contributorInner, String value,
                                 BibframeContributorsInner.TypeEnum type, boolean isCreator) {
    assertThat(contributorInner.getName()).isEqualTo(value);
    assertThat(contributorInner.getType()).isEqualTo(type);
    assertThat(contributorInner.getIsCreator()).isEqualTo(isCreator);
  }

  private SingleResourceMapperUnit genericMapper() {
    return new SingleResourceMapperUnit() {
      @Override
      public Object toDto(Resource source, Object parentDto, Resource parentResource) {
        return null;
      }

      @Override
      public Set<Class<?>> supportedParents() {
        return null;
      }

      @Override
      public Resource toEntity(Object dto, Resource parentEntity) {
        return null;
      }
    };
  }

}
