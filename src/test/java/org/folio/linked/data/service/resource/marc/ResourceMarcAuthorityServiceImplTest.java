package org.folio.linked.data.service.resource.marc;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.TestUtil.randomString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.rest.srs.SrsClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcAuthorityServiceImplTest {

  @InjectMocks
  private ResourceMarcAuthorityServiceImpl resourceMarcAuthorityService;

  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private MarcAuthority2ldMapper marcAuthority2ldMapper;
  @Mock
  private SrsClient srsClient;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;

  @Test
  void importResourceFromSrs_shouldImportAuthorityFromSrs() {
    // given
    var id = "123";
    var createdResource = new Resource().setIdAndRefreshEdges(123L).setLabel("").addTypes(PERSON);
    when(srsClient.getAuthorityBySrsId(id))
      .thenReturn(new ResponseEntity<>(createRecord(), HttpStatusCode.valueOf(200)));

    var dictionaryModelMock = mock(org.folio.ld.dictionary.model.Resource.class);
    when(marcAuthority2ldMapper.fromMarcJson(any())).thenReturn(List.of(dictionaryModelMock));
    when(resourceModelMapper.toEntity(dictionaryModelMock)).thenReturn(createdResource);
    doReturn(new SaveGraphResult(createdResource)).when(resourceGraphService).saveMergingGraph(createdResource);

    // when
    var actualResource = resourceMarcAuthorityService.importResourceFromSrs(id);

    // then
    assertThat(actualResource).isEqualTo(createdResource);
    verify(srsClient).getAuthorityBySrsId(id);
    verify(resourceGraphService).saveMergingGraph(createdResource);
  }

  @Test
  void importResourceFromSrs_shouldThrowNotFound_ifRecordNotExistsInSrs() {
    // given
    var id = "123";
    when(srsClient.getAuthorityBySrsId(id))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(any(), any())).thenReturn(emptyRequestProcessingException());

    // then
    assertThatThrownBy(() -> resourceMarcAuthorityService.importResourceFromSrs(id))
      .isInstanceOf(RequestProcessingException.class);
  }

  @Test
  void saveMarcAuthority_shouldCreateNewAuthority_ifGivenModelDoesNotExistsBySrsId() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setIdAndRefreshEdges(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(new SaveGraphResult(mapped)).when(resourceGraphService).saveMergingGraph(mapped);
    doReturn(Optional.empty()).when(folioMetadataRepo).findIdBySrsId(srsId);

    // when
    var result = resourceMarcAuthorityService.saveMarcAuthority(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldUpdateAuthority_ifGivenModelExistsByIdAndSrsId() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setIdAndRefreshEdges(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(Optional.of((FolioMetadataRepository.IdOnly) () -> id)).when(folioMetadataRepo).findIdBySrsId(srsId);
    doReturn(new SaveGraphResult(mapped)).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcAuthorityService.saveMarcAuthority(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldNotReplaceOldAuthority_ifNewAuthorityHasDifferentTypes() {
    // given
    var id = 12345L;
    var existedAuthority = new Resource().setIdAndRefreshEdges(id).addTypes(PERSON);
    var newAuthority = new Resource().setIdAndRefreshEdges(id - 1).addTypes(PERSON, CONCEPT);

    var srsId = UUID.randomUUID().toString();
    newAuthority.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(newAuthority).setSrsId(srsId));

    doReturn(newAuthority).when(resourceModelMapper).toEntity(any());
    doReturn(of(existedAuthority)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
    doReturn(of((FolioMetadataRepository.IdOnly) () -> id)).when(folioMetadataRepo).findIdBySrsId(srsId);
    doReturn(new SaveGraphResult(newAuthority)).when(resourceGraphService).saveMergingGraph(newAuthority);

    // when
    var actualId = resourceMarcAuthorityService.saveMarcAuthority(new org.folio.ld.dictionary.model.Resource());

    // then
    assertThat(actualId).isEqualTo(newAuthority.getId());
    assertThat(newAuthority.getOutgoingEdges()).isEmpty();
    assertThat(newAuthority.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("true");
    assertThat(existedAuthority.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("false");
  }

  @Test
  void saveMarcAuthority_shouldCreateNewAuthorityVersionAndMarkOldAsObsolete_ifGivenModelExistsBySrsIdButNotById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var existed = new Resource().setIdAndRefreshEdges(id).addTypes(PERSON).setManaged(true);
    existed.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(existed));
    doReturn(of(existed)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
    var model = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setIdAndRefreshEdges(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    var anotherResourceId = Optional.of((FolioMetadataRepository.IdOnly) () -> id - 1);
    doReturn(anotherResourceId).when(folioMetadataRepo).findIdBySrsId(srsId);
    doReturn(new SaveGraphResult(mapped)).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcAuthorityService.saveMarcAuthority(model);

    // then
    assertThat(result).isEqualTo(id);
    assertThat(existed.isActive()).isFalse();
    assertThat(existed.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("false");
    assertThat(existed.getFolioMetadata()).isNull();
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(existed, mapped.getId()));
    assertThat(mapped.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("true");
    assertThat(mapped.getIncomingEdges()).contains(new ResourceEdge(existed, mapped, REPLACED_BY));
  }

  @Test
  void saveMarcAuthority_shouldThrowException_whenSrsIdIsMissing() {
    // given
    var model = new org.folio.ld.dictionary.model.Resource();
    var personWithoutSrsId = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(PERSON);
    doReturn(personWithoutSrsId).when(resourceModelMapper).toEntity(model);

    // then
    assertThatThrownBy(() -> resourceMarcAuthorityService.saveMarcAuthority(model))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("SRS ID is missing in the resource");
  }

  @Test
  void saveMarcAuthority_shouldThrowException_whenResourceIsNotAuthority() {
    // given
    var model = new org.folio.ld.dictionary.model.Resource();
    var work = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(WORK);
    work.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(work).setSrsId(randomString()));
    doReturn(work).when(resourceModelMapper).toEntity(model);

    // then
    assertThatThrownBy(() -> resourceMarcAuthorityService.saveMarcAuthority(model))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Resource is not an authority");
  }

  private org.folio.rest.jaxrs.model.Record createRecord() {
    var leader = "04809n   a2200865 i 4500";
    leader = leader.substring(0, 6) + "am" + leader.substring(8);
    var content = Map.of("leader", leader);
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }
}
