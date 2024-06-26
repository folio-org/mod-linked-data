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
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.FAMILY;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.MEETING;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.ORGANIZATION;
import static org.folio.search.domain.dto.BibframeContributorsInner.TypeEnum.PERSON;
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN;
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN_PARALLEL;
import static org.folio.search.domain.dto.BibframeIndexTitleType.MAIN_VARIANT;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB_PARALLEL;
import static org.folio.search.domain.dto.BibframeIndexTitleType.SUB_VARIANT;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.EAN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.ISBN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.LCCN;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.LOCALID;
import static org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner.TypeEnum.UNKNOWN;
import static org.folio.search.domain.dto.ResourceIndexEventType.CREATE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.exception.LinkedDataServiceException;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.kafka.impl.KafkaSearchMessageBibframeMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.BibframeContributorsInner;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.search.domain.dto.BibframeIndexTitleType;
import org.folio.search.domain.dto.BibframeInstancesInner;
import org.folio.search.domain.dto.BibframeInstancesInnerIdentifiersInner;
import org.folio.search.domain.dto.BibframeTitlesInner;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KafkaSearchMessageMapperTest {

  @InjectMocks
  private KafkaSearchMessageBibframeMapper kafkaMessageMapper;

  @Mock
  private SingleResourceMapper singleResourceMapper;

  @BeforeEach
  public void setupMocks() {
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
  }

  @Test
  void toIndex_shouldReturnCorrectlyMappedIndex_fromWork() {
    // given
    var work = getSampleWork(null);
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    work.addOutgoingEdge(new ResourceEdge(work, wrongContributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, emptyContributor, CONTRIBUTOR));
    final var instance1 = getInstance(1L, work);
    final var instance2 = getInstance(2L, work);

    // when
    var resultOpt = kafkaMessageMapper.toIndex(work, CREATE);

    // then
    assertThat(resultOpt).isPresent();
    var result = resultOpt.get();
    validateWork(result, work, wrongContributor, 2);
    validateInstance(result.getInstances().get(0), instance1);
    validateInstance(result.getInstances().get(1), instance2);
  }

  @Test
  void toIndex_shouldReturnEmptyResult_fromWorkWithNoIndexableInfo() {
    // given
    var work = new Resource().addTypes(WORK);
    // when
    var resultOpt = kafkaMessageMapper.toIndex(work, CREATE);

    // then
    assertThat(resultOpt).isEmpty();
  }

  @Test
  void toIndex_shouldThrowException_fromInstance() {
    // given
    var work = getSampleWork(null);
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    work.addOutgoingEdge(new ResourceEdge(work, wrongContributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, emptyContributor, CONTRIBUTOR));
    final var instance1 = getInstance(1L, work);
    getInstance(2L, work);
    work.setIncomingEdges(new LinkedHashSet<>());

    // when
    var thrown = assertThrows(LinkedDataServiceException.class, () -> kafkaMessageMapper.toIndex(instance1, CREATE));

    // then
    assertThat(thrown.getMessage()).contains(instance1.toString());
    assertThat(thrown.getMessage()).contains("CREATE");
  }

  @Test
  void toDeleteIndex_shouldThrowNullPointerException_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    var thrown = assertThrows(NullPointerException.class, () -> kafkaMessageMapper.toDeleteIndexId(resource));

    // then
    assertThat(thrown.getMessage()).isEqualTo("work is marked non-null but is null");
  }

  @Test
  void toDeleteIndex_shouldReturnEmptyOptional_ifGivenWorkIdIsNull() {
    // given
    var work = new Resource().addTypes(WORK);

    // when
    var result = kafkaMessageMapper.toDeleteIndexId(work);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void toDeleteIndex_shouldReturnCorrectlyMappedId_fromWork() {
    // given
    var work = getSampleWork(null);
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    work.addOutgoingEdge(new ResourceEdge(work, wrongContributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, emptyContributor, CONTRIBUTOR));
    getInstance(1L, work);
    getInstance(2L, work);

    // when
    var resultOpt = kafkaMessageMapper.toDeleteIndexId(work);

    // then
    assertThat(resultOpt).isPresent().contains(work.getId());
  }

  @Test
  void toDeleteIndex_shouldThrowException_fromInstance() {
    // given
    var work = getSampleWork(null);
    var wrongContributor = getContributor(ANNOTATION);
    var emptyContributor = new Resource();
    work.addOutgoingEdge(new ResourceEdge(work, wrongContributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, emptyContributor, CONTRIBUTOR));
    final var instance1 = getInstance(1L, work);
    getInstance(2L, work);

    // when
    var thrown = assertThrows(LinkedDataServiceException.class, () -> kafkaMessageMapper.toDeleteIndexId(instance1));

    // then
    assertThat(thrown.getMessage()).contains(instance1.toString());
    assertThat(thrown.getMessage()).contains("DELETE");
  }

  private Resource getInstance(Long id, Resource work) {
    var instance = getSampleInstanceResource(id, work);
    var emptyTitle = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyTitle, TITLE));
    var wrongId = getIdentifier();
    var emptyId = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, wrongId, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyId, MAP));
    var emptyPublication = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyPublication, PE_PUBLICATION));
    return instance;
  }

  private Resource getIdentifier() {
    var id = new Resource();
    id.setId(randomLong());
    id.setDoc(getJsonNode(Map.of(NAME.getValue(), List.of("wrongId"))));
    id.addTypes(ANNOTATION);
    return id;
  }

  private Resource getContributor(ResourceTypeDictionary type) {
    var contributor = new Resource();
    contributor.setId(randomLong());
    contributor.setDoc(getJsonNode(Map.of(NAME.getValue(), List.of(UUID.randomUUID().toString()))));
    contributor.addTypes(type);
    return contributor;
  }

  private void validateWork(BibframeIndex result, Resource work, Resource wrongContributor, int instancesExpected) {
    assertThat(result.getId()).isEqualTo(work.getId().toString());
    assertTitle(result.getTitles().get(0), "Basic: mainTitle", MAIN);
    assertTitle(result.getTitles().get(1), "Basic: subTitle", SUB);
    assertTitle(result.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(result.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(result.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(result.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
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
    assertThat(result.getClassifications()).hasSize(2);
    assertThat(result.getClassifications().get(0).getNumber()).isEqualTo("lc code");
    assertThat(result.getClassifications().get(0).getSource()).isEqualTo("lc");
    assertThat(result.getClassifications().get(1).getNumber()).isEqualTo("ddc code");
    assertThat(result.getClassifications().get(1).getSource()).isEqualTo("ddc");
    assertThat(result.getSubjects()).hasSize(2);
    assertThat(result.getSubjects().get(0).getValue()).isEqualTo("subject 1");
    assertThat(result.getSubjects().get(1).getValue()).isEqualTo("subject 2");
    assertThat(result.getInstances()).hasSize(instancesExpected);
  }

  private void validateInstance(BibframeInstancesInner instanceIndex, Resource instance) {
    assertThat(instanceIndex.getId()).isEqualTo(instance.getId().toString());
    assertTitle(instanceIndex.getTitles().get(0), "Basic: mainTitle" + instance.getId(), MAIN);
    assertTitle(instanceIndex.getTitles().get(1), "Basic: subTitle", SUB);
    assertTitle(instanceIndex.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(instanceIndex.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
    assertThat(instanceIndex.getIdentifiers()).hasSize(6);
    assertId(instanceIndex.getIdentifiers().get(0), "lccn value", LCCN);
    assertId(instanceIndex.getIdentifiers().get(1), "isbn value", ISBN);
    assertId(instanceIndex.getIdentifiers().get(2), "ean value", EAN);
    assertId(instanceIndex.getIdentifiers().get(3), "localId value", LOCALID);
    assertId(instanceIndex.getIdentifiers().get(4), "otherId value", UNKNOWN);
    assertId(instanceIndex.getIdentifiers().get(5), "wrongId", null);
    assertThat(instanceIndex.getContributors()).isEmpty();
    assertThat(instanceIndex.getPublications()).hasSize(1);
    assertThat(instanceIndex.getPublications().get(0).getDate()).isNull();
    assertThat(instanceIndex.getPublications().get(0).getName()).isEqualTo("publication name");
    assertThat(instanceIndex.getEditionStatements()).hasSize(1);
    assertThat(instanceIndex.getEditionStatements().get(0).getValue())
      .isEqualTo(instance.getDoc().get(EDITION_STATEMENT.getValue()).get(0).asText());
  }

  private void assertTitle(BibframeTitlesInner titleInner, String value, BibframeIndexTitleType type) {
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
