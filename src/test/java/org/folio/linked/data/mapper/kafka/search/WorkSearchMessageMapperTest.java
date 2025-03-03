package org.folio.linked.data.mapper.kafka.search;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.FAMILY;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.JURISDICTION;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.MEETING;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.ORGANIZATION;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.PERSON;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.EAN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.ISBN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.LCCN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.LOCAL_ID;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.UNKNOWN;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN_PARALLEL;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN_VARIANT;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB_PARALLEL;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB_VARIANT;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.LinkedDataContributor;
import org.folio.linked.data.domain.dto.LinkedDataIdentifier;
import org.folio.linked.data.domain.dto.LinkedDataInstanceOnly;
import org.folio.linked.data.domain.dto.LinkedDataTitle;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkSearchMessageMapperTest {

  @InjectMocks
  private WorkSearchMessageMapperImpl workSearchMessageMapper;

  @Mock
  private NoteMapper noteMapper;
  @Mock
  private SingleResourceMapper singleResourceMapper;
  @Spy
  private IndexIdentifierMapper innerIndexIdentifierMapper = new IndexIdentifierMapperImpl();

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
      ResourceTypeDictionary.ORGANIZATION.getUri(),
      ResourceTypeDictionary.JURISDICTION.getUri()
    ).forEach(t ->
      lenient().when(singleResourceMapper.getMapperUnit(eq(t), any(), any(), any())).thenReturn(of(genericMapper()))
    );
  }

  @Test
  void toIndex_shouldReturnCorrectlyMappedIndex_fromResourceWithIdOnly() {
    // given
    var resource = new Resource().setId(randomLong());

    // when
    var result = workSearchMessageMapper.toIndex(resource);

    // then
    assertThat(result)
      .hasAllNullFieldsOrPropertiesExcept("id", "resourceName", "_new")
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .extracting("_new")
      .isInstanceOf(LinkedDataWork.class);
    var linkedDataWork = (LinkedDataWork) result.getNew();
    assertThat(linkedDataWork)
      .hasFieldOrPropertyWithValue("id", String.valueOf(resource.getId()))
      .hasFieldOrPropertyWithValue("titles", emptyList())
      .hasFieldOrPropertyWithValue("contributors", emptyList())
      .hasFieldOrPropertyWithValue("hubAAPs", emptyList())
      .hasFieldOrPropertyWithValue("languages", emptyList())
      .hasFieldOrPropertyWithValue("notes", emptyList())
      .hasFieldOrPropertyWithValue("classifications", emptyList())
      .hasFieldOrPropertyWithValue("subjects", emptyList())
      .hasFieldOrPropertyWithValue("instances", emptyList());
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
    var result = workSearchMessageMapper.toIndex(work);

    // then
    assertThat(result)
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .extracting("_new")
      .isInstanceOf(LinkedDataWork.class);
    var linkedDataWork = (LinkedDataWork) result.getNew();
    validateWork(linkedDataWork, work, wrongContributor, 2);
    validateInstance(linkedDataWork.getInstances().get(0), instance1);
    validateInstance(linkedDataWork.getInstances().get(1), instance2);
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

  private void validateWork(LinkedDataWork result, Resource work, Resource wrongContributor, int instancesExpected) {
    assertThat(result.getId()).isEqualTo(work.getId().toString());
    assertTitle(result.getTitles().get(0), "Primary: mainTitle", MAIN);
    assertTitle(result.getTitles().get(1), "Primary: subTitle", SUB);
    assertTitle(result.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(result.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(result.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(result.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
    assertThat(result.getContributors()).hasSize(11);
    assertContributor(result.getContributors().get(0), "name-CREATOR-PERSON", PERSON, true);
    assertContributor(result.getContributors().get(1), "name-CREATOR-MEETING", MEETING, true);
    assertContributor(result.getContributors().get(2), "name-CREATOR-ORGANIZATION", ORGANIZATION, true);
    assertContributor(result.getContributors().get(3), "name-CREATOR-FAMILY", FAMILY, true);
    assertContributor(result.getContributors().get(4), "name-CREATOR-JURISDICTION", JURISDICTION, true);
    assertContributor(result.getContributors().get(5), "name-CONTRIBUTOR-PERSON", PERSON, false);
    assertContributor(result.getContributors().get(6), "name-CONTRIBUTOR-MEETING", MEETING, false);
    assertContributor(result.getContributors().get(7), "name-CONTRIBUTOR-ORGANIZATION", ORGANIZATION, false);
    assertContributor(result.getContributors().get(8), "name-CONTRIBUTOR-FAMILY", FAMILY, false);
    assertContributor(result.getContributors().get(9), "name-CONTRIBUTOR-JURISDICTION", JURISDICTION, false);
    assertContributor(result.getContributors().get(10), wrongContributor.getDoc().get(NAME.getValue()).get(0).asText(),
      null, false);
    assertThat(result.getLanguages()).hasSize(1);
    assertThat(result.getLanguages().get(0)).isEqualTo("eng");
    assertThat(result.getClassifications()).hasSize(2);
    assertThat(result.getClassifications().get(0).getNumber()).isEqualTo("lc code");
    assertThat(result.getClassifications().get(0).getSource()).isEqualTo("lc");
    assertThat(result.getClassifications().get(1).getNumber()).isEqualTo("ddc code");
    assertThat(result.getClassifications().get(1).getSource()).isEqualTo("ddc");
    assertThat(result.getSubjects()).hasSize(2);
    assertThat(result.getSubjects().get(0)).isEqualTo("subject 1");
    assertThat(result.getSubjects().get(1)).isEqualTo("subject 2");
    assertThat(result.getInstances()).hasSize(instancesExpected);
  }

  private void validateInstance(LinkedDataInstanceOnly instanceIndex, Resource instance) {
    assertThat(instanceIndex.getId()).isEqualTo(instance.getId().toString());
    assertTitle(instanceIndex.getTitles().get(0), "Primary: mainTitle" + instance.getId(), MAIN);
    assertTitle(instanceIndex.getTitles().get(1), "Primary: subTitle", SUB);
    assertTitle(instanceIndex.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(instanceIndex.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
    assertThat(instanceIndex.getIdentifiers()).hasSize(6);
    assertId(instanceIndex.getIdentifiers().get(0), "lccn value", LCCN);
    assertId(instanceIndex.getIdentifiers().get(1), "isbn value", ISBN);
    assertId(instanceIndex.getIdentifiers().get(2), "ean value", EAN);
    assertId(instanceIndex.getIdentifiers().get(3), "localId value", LOCAL_ID);
    assertId(instanceIndex.getIdentifiers().get(4), "otherId value", UNKNOWN);
    assertId(instanceIndex.getIdentifiers().get(5), "wrongId", null);
    assertThat(instanceIndex.getContributors()).isEmpty();
    assertThat(instanceIndex.getPublications()).hasSize(1);
    assertThat(instanceIndex.getPublications().get(0).getDate()).isNull();
    assertThat(instanceIndex.getPublications().get(0).getName()).isEqualTo("publication name");
    assertThat(instanceIndex.getEditionStatements()).hasSize(1);
    assertThat(instanceIndex.getEditionStatements().get(0))
      .isEqualTo(instance.getDoc().get(EDITION.getValue()).get(0).asText());
  }

  private void assertTitle(LinkedDataTitle titleInner, String value, LinkedDataTitle.TypeEnum type) {
    assertThat(titleInner.getValue()).isEqualTo(value);
    assertThat(titleInner.getType()).isEqualTo(type);
  }

  private void assertId(LinkedDataIdentifier idInner, String value, TypeEnum type) {
    assertThat(idInner.getValue()).isEqualTo(value);
    assertThat(idInner.getType()).isEqualTo(type);
  }

  private void assertContributor(LinkedDataContributor contributorInner, String value,
                                 LinkedDataContributor.TypeEnum type, boolean isCreator) {
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
