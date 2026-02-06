package org.folio.linked.data.service.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ReferenceServiceImplTest {

  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceMarcAuthorityService marcAuthorityService;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private RdfImportService rdfImportService;

  @InjectMocks
  private ReferenceServiceImpl referenceService;

  @Test
  void resolveReference_withId_fetchesFromRepo() {
    // given
    var ref = new Reference().id("123");
    var resource = new Resource();
    when(resourceRepo.findById(123L)).thenReturn(Optional.of(resource));

    // when
    var result = referenceService.resolveReference(ref);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void resolveReference_withSrsId_fetchesFromRepo() {
    // given
    var ref = new Reference().srsId("srs-1");
    var resource = new Resource();
    when(resourceRepo.findByFolioMetadataSrsId("srs-1")).thenReturn(Optional.of(resource));

    // when
    var result = referenceService.resolveReference(ref);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void resolveReference_withSrsId_importsIfNotFoundInDb() {
    // given
    var ref = new Reference().srsId("srs-2");
    var imported = new Resource();
    when(resourceRepo.findByFolioMetadataSrsId("srs-2")).thenReturn(Optional.empty());
    when(marcAuthorityService.importResourceFromSrs("srs-2")).thenReturn(imported);

    // when
    var result = referenceService.resolveReference(ref);

    // then
    assertThat(result).isEqualTo(imported);
  }

  @Test
  void resolveReference_withId_throwsExceptionIfNotFound() {
    // given
    var ref = new Reference().id("999");
    when(resourceRepo.findById(999L)).thenReturn(Optional.empty());
    var exception = new RequestProcessingException(404, "NOT_FOUND", Map.of(), "Resource not found for id: 999");
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), eq("999"))).thenReturn(exception);

    // when/then
    assertThatThrownBy(() -> referenceService.resolveReference(ref))
      .isInstanceOf(RequestProcessingException.class)
      .hasMessageContaining("Resource not found for id: 999");
  }

  @Test
  void resolveReference_withId_returnsLatestIfReplacedByAnotherResource() {
    // given
    var ref = new Reference().id("1");
    var original = new Resource();
    var replacement = new Resource();
    var edge = new ResourceEdge(original, replacement, REPLACED_BY);
    original.getOutgoingEdges().add(edge);
    when(resourceRepo.findById(1L)).thenReturn(Optional.of(original));

    // when
    var result = referenceService.resolveReference(ref);

    // then
    assertThat(result).isEqualTo(replacement);
  }

  @Test
  void resolveReference_withRdfLink_importsFromUrl() {
    // given
    var rdfLink = "https://example.com/resource.json";
    var ref = new Reference().rdfLink(rdfLink);
    var resource = new Resource();
    when(rdfImportService.importRdfUrl(rdfLink, true)).thenReturn(resource);

    // when
    var result = referenceService.resolveReference(ref);

    // then
    assertThat(result).isEqualTo(resource);
  }
}
