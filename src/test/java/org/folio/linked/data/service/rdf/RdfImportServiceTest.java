package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.model.ImportEventResultMapper;
import org.folio.linked.data.model.entity.imprt.ImportEventResult;
import org.folio.linked.data.repo.ImportEventResultRepository;
import org.folio.linked.data.service.lccn.LccnResourceService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RdfImportServiceTest {

  @InjectMocks
  private RdfImportServiceImpl rdfImportService;
  @Mock
  private Rdf4LdService rdf4LdService;
  @Mock
  private MetadataService metadataService;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private ImportEventResultMapper importEventResultMapper;
  @Mock
  private ImportEventResultRepository importEventResultRepository;
  @Mock
  private LccnResourceService lccnResourceService;

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
    var jobInstanceId = 456L;
    var event = new ImportOutputEvent()
      .ts(ts)
      .jobInstanceId(jobInstanceId)
      .resources(Set.of(resource1, resource2, resource3));
    var expectedImportEventResult = new ImportEventResult().setEventTs(Long.parseLong(ts));
    when(importEventResultMapper.fromImportReport(eq(event), any(), any()))
      .thenReturn(expectedImportEventResult);
    when(lccnResourceService.unMockLccnEdges(any(), any()))
      .thenAnswer(inv -> inv.getArgument(0));


    // when
    rdfImportService.importOutputEvent(event, LocalDateTime.now());

    // then
    verify(metadataService).ensure(entity1);
    verify(metadataService).ensure(entity2);
    verify(importEventResultRepository).save(expectedImportEventResult);
  }

}
