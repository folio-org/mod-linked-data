package org.folio.linked.data.validation.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.service.SettingsService;
import org.folio.linked.data.service.search.SearchService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LccnUniquenessValidatorTest {

  @InjectMocks
  private LccnUniquenessValidator validator;

  @Mock
  private SearchService searchService;
  @Mock
  private FolioMetadataRepository folioMetadataRepository;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private SettingsService settingsService;

  @Test
  void shouldThrowFailedDependency_ifSearchServiceThrownException() {
    // given
    when(searchService.getTotalInstancesByLccnExcludingSuppressedAndId(any(), any()))
      .thenThrow(new RuntimeException("msg"));
    when(exceptionBuilder.failedDependencyException(any(), any()))
      .thenReturn(new RequestProcessingException(HttpStatus.FAILED_DEPENDENCY.value(), "failed", null, "msg"));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);
    var requestDto = new ResourceRequestDto()
      .id(123L)
      .resource(new InstanceField().instance(createInstanceRequest(createLccnRequest())));

    // expect
    assertThrows(RequestProcessingException.class,
      () -> validator.isValid(requestDto, null), "msg");
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnTrue_ifSettingsServiceThrownException() {
    // given
    when(settingsService.isSettingEnabled(any(), any(), any())).thenThrow(RuntimeException.class);


    // when
    boolean valid = validator.isValid(new ResourceRequestDto(), null);

    //then
    assertTrue(valid);
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnTrue_ifResourceIsNotInstanceField() {
    // given
    var requestDto = new ResourceRequestDto().resource(new WorkField());

    // expect
    assertTrue(validator.isValid(requestDto, null));
  }

  @Test
  void shouldReturnTrue_ifResourceDoesNotHaveCurrentLccn() {
    // given
    var requestDto = new ResourceRequestDto().resource(
      new InstanceField().instance(createInstanceRequest()));

    // expect
    assertTrue(validator.isValid(requestDto, null));
  }

  @Test
  void shouldReturnTrue_ifValidationDisabled() {
    // given
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(false);

    // when
    boolean isValid = validator.isValid(new ResourceRequestDto(), null);

    // then
    assertTrue(isValid);
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnTrue_ifResourceNotExistsInRepoAndInSearch() {
    // given
    var requestDto = new ResourceRequestDto()
      .id(123L)
      .resource(new InstanceField().instance(createInstanceRequest(createLccnRequest())));
    when(folioMetadataRepository.findInventoryIdById(123L)).thenReturn(Optional.empty());
    when(searchService.getTotalInstancesByLccnExcludingSuppressedAndId(any(), any()))
      .thenReturn(new SearchResponseTotalOnly().totalRecords(0L));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    boolean isValid = validator.isValid(requestDto, null);

    // then
    assertTrue(isValid);
    verify(folioMetadataRepository).findInventoryIdById(123L);
    verify(searchService).getTotalInstancesByLccnExcludingSuppressedAndId(any(), any());
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnTrue_ifResourceExistsInRepoButNotInSearch() {
    // given
    var requestDto = new ResourceRequestDto()
      .id(123L)
      .resource(new InstanceField().instance(createInstanceRequest(createLccnRequest())));
    when(folioMetadataRepository.findInventoryIdById(123L))
      .thenReturn(Optional.of(() -> "6dcb9a08-9884-4a15-b990-89c879a8e999"));
    when(searchService.getTotalInstancesByLccnExcludingSuppressedAndId(any(), any()))
      .thenReturn(new SearchResponseTotalOnly().totalRecords(0L));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    boolean isValid = validator.isValid(requestDto, null);

    // then
    assertTrue(isValid);
    verify(folioMetadataRepository).findInventoryIdById(123L);
    verify(searchService).getTotalInstancesByLccnExcludingSuppressedAndId(any(), any());
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnFalse_ifResourceExistsInRepoAndSearch() {
    // given
    var requestDto = new ResourceRequestDto()
      .id(123L)
      .resource(new InstanceField().instance(createInstanceRequest(createLccnRequest())));
    when(folioMetadataRepository.findInventoryIdById(123L))
      .thenReturn(Optional.of(() -> "6dcb9a08-9884-4a15-b990-89c879a8e999"));
    when(searchService.getTotalInstancesByLccnExcludingSuppressedAndId(any(), any()))
      .thenReturn(new SearchResponseTotalOnly().totalRecords(1L));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    boolean isValid = validator.isValid(requestDto, null);

    // then
    assertFalse(isValid);
    verify(folioMetadataRepository).findInventoryIdById(123L);
    verify(searchService).getTotalInstancesByLccnExcludingSuppressedAndId(any(), any());
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  @Test
  void shouldReturnFalse_ifResourceNotExistsInReposButExistsInSearch() {
    // given
    var requestDto = new ResourceRequestDto()
      .id(123L)
      .resource(new InstanceField().instance(createInstanceRequest(createLccnRequest())));
    when(folioMetadataRepository.findInventoryIdById(123L)).thenReturn(Optional.empty());
    when(searchService.getTotalInstancesByLccnExcludingSuppressedAndId(any(), any()))
      .thenReturn(new SearchResponseTotalOnly().totalRecords(1L));
    when(settingsService.isSettingEnabled(any(), any(), any())).thenReturn(true);

    // when
    boolean isValid = validator.isValid(requestDto, null);

    // then
    assertFalse(isValid);
    verify(folioMetadataRepository).findInventoryIdById(123L);
    verify(searchService).getTotalInstancesByLccnExcludingSuppressedAndId(any(), any());
    verify(settingsService).isSettingEnabled(any(), any(), any());
  }

  private InstanceRequest createInstanceRequest(IdentifierRequest lccnRequest) {
    return new InstanceRequest(3, List.of())
      .addMapItem(new LccnField().lccn(lccnRequest));
  }

  private InstanceRequest createInstanceRequest() {
    return new InstanceRequest(3, List.of());
  }

  private IdentifierRequest createLccnRequest() {
    return new IdentifierRequest()
      .value(List.of("n-0123456789"))
      .status(List.of(new Status().link(List.of("http://id.loc.gov/vocabulary/mstatus/current"))));
  }
}
