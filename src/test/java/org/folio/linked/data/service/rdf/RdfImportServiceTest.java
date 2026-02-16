package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResultEvent;
import org.folio.linked.data.domain.dto.ResourceWithLineNumber;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.http.HttpClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.kafka.ldimport.ImportEventResultMapper;
import org.folio.linked.data.service.lccn.LccnResourceService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.folio.spring.testing.type.UnitTest;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RdfImportServiceTest {

  @InjectMocks
  private RdfImportServiceImpl rdfImportService;
  @Mock
  private Rdf4LdService rdf4LdService;
  @Mock
  private HttpClient httpClient;
  @Mock
  private MetadataService metadataService;
  @Mock
  private LccnResourceService lccnResourceService;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ImportEventResultMapper importEventResultMapper;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private FolioMessageProducer<ImportResultEvent> importResultEventProducer;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(rdfImportService, "importResultEventProducer", importResultEventProducer);
  }

  @Test
  void importFile_createsResources_whenValidFileProvided() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var inputStream = mock(InputStream.class);
    var resources = Set.of(mock(org.folio.ld.dictionary.model.Resource.class));
    var entity = mock(org.folio.linked.data.model.entity.Resource.class);
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(rdf4LdService.mapBibframe2RdfToLd(inputStream, multipartFile.getContentType())).thenReturn(resources);
    var searchResults = new HashMap<String, LccnResourceService.LccnResourceSearchResult>();
    when(lccnResourceService.findMockResources(resources)).thenReturn(searchResults);
    var unMockedResource = new Resource().setId(1L);
    when(lccnResourceService.unMockLccnEdges(any(), any())).thenReturn(unMockedResource);
    when(resourceModelMapper.toEntity(unMockedResource)).thenReturn(entity);
    var saveGraphResult = new SaveGraphResult(entity, Set.of(entity), Set.of());
    when(resourceGraphService.saveMergingGraphInNewTransaction(entity)).thenReturn(saveGraphResult);

    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result.getResources()).hasSize(1);
    verify(metadataService).ensure(entity);
  }

  @Test
  void importFile_updatesResources_whenValidFileProvidedAndResourceExists() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var inputStream = mock(InputStream.class);
    var resources = Set.of(mock(org.folio.ld.dictionary.model.Resource.class));
    var entity = mock(org.folio.linked.data.model.entity.Resource.class);
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(rdf4LdService.mapBibframe2RdfToLd(inputStream, multipartFile.getContentType())).thenReturn(resources);
    var searchResults = new HashMap<String, LccnResourceService.LccnResourceSearchResult>();
    when(lccnResourceService.findMockResources(resources)).thenReturn(searchResults);
    var unMockedResource = new Resource().setId(1L);
    when(lccnResourceService.unMockLccnEdges(any(), any())).thenReturn(unMockedResource);
    when(resourceModelMapper.toEntity(unMockedResource)).thenReturn(entity);
    var saveGraphResult = new SaveGraphResult(entity, Set.of(), Set.of(entity));
    when(resourceGraphService.saveMergingGraphInNewTransaction(entity)).thenReturn(saveGraphResult);
    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result.getResources()).hasSize(1);
    verify(metadataService).ensure(entity);
  }

  @Test
  void importFile_notSavesAnything_whenResourceMappingFailed() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var inputStream = mock(InputStream.class);
    var resources = Set.of(mock(org.folio.ld.dictionary.model.Resource.class));
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(rdf4LdService.mapBibframe2RdfToLd(inputStream, multipartFile.getContentType())).thenReturn(resources);
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(resourceModelMapper.toEntity(any())).thenThrow(new RuntimeException());

    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result.getResources()).isEmpty();
    verify(resourceGraphService, never()).saveMergingGraphInNewTransaction(any());
  }

  @Test
  void importFile_throwsException_whenFileCannotBeRead() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    when(multipartFile.getInputStream()).thenThrow(IOException.class);
    var expectedException = new RequestProcessingException(400, "code", new HashMap<>(), "message");
    when(exceptionBuilder.badRequestException(any(), any())).thenReturn(expectedException);

    // when
    assertThatThrownBy(() -> rdfImportService.importFile(multipartFile))
      // then
      .isEqualTo(expectedException);
  }

  @Test
  void importFile_returnsEmptyResult_whenRdf2LdMappingFails() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var message = "mapping exception";
    when(multipartFile.getInputStream()).thenThrow(new RuntimeException(message));

    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result).isEqualTo(new ImportFileResponseDto(List.of(), message));
  }

  @Test
  void saveImportEventResources_shouldSaveResources() {
    // given
    var resource1 = new Resource().setId(1L).setLabel("for creation");
    var resource2 = new Resource().setId(2L).setLabel("for update");
    var resource3 = new Resource().setId(3L).setLabel("for failure");
    var entity1 = new org.folio.linked.data.model.entity.Resource().setIdAndRefreshEdges(1L)
      .setLabel(resource1.getLabel());
    when(resourceModelMapper.toEntity(resource1)).thenReturn(entity1);
    var entity2 = new org.folio.linked.data.model.entity.Resource().setIdAndRefreshEdges(2L)
      .setLabel(resource2.getLabel());
    when(resourceModelMapper.toEntity(resource2)).thenReturn(entity2);
    doThrow(new RuntimeException()).when(resourceModelMapper).toEntity(resource3);
    var saveGraphResult1 = new SaveGraphResult(entity1, Set.of(entity1), Set.of());
    when(resourceGraphService.saveMergingGraphInNewTransaction(entity1)).thenReturn(saveGraphResult1);
    var saveGraphResult2 = new SaveGraphResult(entity2, Set.of(), Set.of(entity2));
    when(resourceGraphService.saveMergingGraphInNewTransaction(entity2)).thenReturn(saveGraphResult2);
    var ts = "123";
    var jobExecutionId = 456L;
    var event = new ImportOutputEvent()
      .ts(ts)
      .jobExecutionId(jobExecutionId)
      .resourcesWithLineNumbers(Set.of(
        new ResourceWithLineNumber(1L, resource1),
        new ResourceWithLineNumber(2L, resource2),
        new ResourceWithLineNumber(3L, resource3))
      );
    var expectedImportEventResult = new ImportResultEvent().originalEventTs(ts);
    when(importEventResultMapper.fromImportReport(eq(event), any(), any()))
      .thenReturn(expectedImportEventResult);
    when(lccnResourceService.unMockLccnEdges(any(), any()))
      .thenAnswer(inv -> inv.getArgument(0));


    // when
    rdfImportService.importOutputEvent(event, OffsetDateTime.now());

    // then
    verify(metadataService).ensure(entity1);
    verify(metadataService).ensure(entity2);
    verify(importResultEventProducer).sendMessages(List.of(expectedImportEventResult));
  }

  @Test
  void importRdfUrl_shouldConvertJsonToResourcesWithoutSaving() {
    // given
    var rdfJson = "{\"@context\":\"test\"}";
    var resource1 = new Resource().setId(1L);
    var resource2 = new Resource().setId(2L);
    var rdfUrl = "https://example.com/resource-123.json";
    var entity1 = new org.folio.linked.data.model.entity.Resource()
      .setIdAndRefreshEdges(1L)
      .setDoc(JSON_MAPPER.readTree("{\"http://bibfra.me/vocab/lite/link\":[\"" + rdfUrl + "\"]}"));
    var entity2 = new org.folio.linked.data.model.entity.Resource()
      .setIdAndRefreshEdges(2L)
      .setDoc(JSON_MAPPER.readTree("{\"http://bibfra.me/vocab/lite/link\":[\"https://example.com/resource-456.json\"]}"));
    when(httpClient.downloadString(rdfUrl)).thenReturn(rdfJson);
    when(rdf4LdService.mapBibframe2RdfToLd(any(InputStream.class), eq("application/ld+json")))
      .thenReturn(Set.of(resource1, resource2));
    var searchResults = new HashMap<String, LccnResourceService.LccnResourceSearchResult>();
    when(lccnResourceService.findMockResources(any())).thenReturn(searchResults);
    when(lccnResourceService.unMockLccnEdges(resource1, searchResults)).thenReturn(resource1);
    when(lccnResourceService.unMockLccnEdges(resource2, searchResults)).thenReturn(resource2);
    when(resourceModelMapper.toEntity(resource1)).thenReturn(entity1);
    when(resourceModelMapper.toEntity(resource2)).thenReturn(entity2);

    // when
    var result = rdfImportService.importRdfUrl(rdfUrl, false);

    // then
    assertThat(result).isEqualTo(entity1);
    verify(metadataService).ensure(entity1);
    verify(metadataService).ensure(entity2);
    verify(resourceGraphService, never()).saveMergingGraphInNewTransaction(any());
    verify(httpClient).downloadString(rdfUrl);
  }

  @Test
  void importRdfUrl_shouldSaveResources_whenSaveIsTrue() {
    // given
    var rdfUrl = "https://example.com/resource-123.json";
    var rdfJson = "{\"@context\":\"test\"}";
    var resource = new Resource().setId(1L);
    var entity = new org.folio.linked.data.model.entity.Resource()
      .setIdAndRefreshEdges(1L)
      .setDoc(JSON_MAPPER.readTree("{\"http://bibfra.me/vocab/lite/link\":[\"" + rdfUrl + "\"]}"));
    when(httpClient.downloadString(rdfUrl)).thenReturn(rdfJson);
    when(rdf4LdService.mapBibframe2RdfToLd(any(InputStream.class), eq("application/ld+json")))
      .thenReturn(Set.of(resource));
    var searchResults = new HashMap<String, LccnResourceService.LccnResourceSearchResult>();
    when(lccnResourceService.findMockResources(any())).thenReturn(searchResults);
    when(lccnResourceService.unMockLccnEdges(resource, searchResults)).thenReturn(resource);
    when(resourceModelMapper.toEntity(resource)).thenReturn(entity);
    var saveGraphResult = new SaveGraphResult(entity, Set.of(entity), Set.of());
    when(resourceGraphService.saveMergingGraphInNewTransaction(entity)).thenReturn(saveGraphResult);

    // when
    var result = rdfImportService.importRdfUrl(rdfUrl, true);

    // then
    assertThat(result).isEqualTo(entity);
    verify(metadataService).ensure(entity);
    verify(resourceGraphService).saveMergingGraphInNewTransaction(entity);
    verify(httpClient).downloadString(rdfUrl);
  }

  @Test
  void importRdfUrl_shouldThrowException_whenMappingFails() {
    // given
    var rdfUrl = "https://example.com/resource.json";
    var rdfJson = "{\"@context\":\"test\"}";
    var mappingError = new RuntimeException("Mapping error");
    when(httpClient.downloadString(rdfUrl)).thenReturn(rdfJson);
    when(rdf4LdService.mapBibframe2RdfToLd(any(InputStream.class), eq("application/ld+json")))
      .thenThrow(mappingError);

    // when & then
    assertThatThrownBy(() -> rdfImportService.importRdfUrl(rdfUrl, false))
      .isEqualTo(mappingError);
  }

}
