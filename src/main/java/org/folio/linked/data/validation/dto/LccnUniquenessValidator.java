package org.folio.linked.data.validation.dto;

import static java.util.Optional.ofNullable;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.rest.search.SearchService;
import org.folio.linked.data.integration.rest.settings.SettingsService;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.util.LccnUtils;
import org.folio.linked.data.validation.LccnUniqueConstraint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("javaarchitecture:S7091")
public class LccnUniquenessValidator implements ConstraintValidator<LccnUniqueConstraint, ResourceRequestDto> {

  private static final String DUPLICATE_CHECK_SCOPE = "ui-quick-marc.lccn-duplicate-check.manage";
  private static final String DUPLICATE_CHECK_KEY = "lccn-duplicate-check";
  private static final String DUPLICATE_CHECK_PROPERTY = "duplicateLccnCheckingEnabled";

  private final SearchService searchService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;
  private final SettingsService settingsService;

  @Override
  public boolean isValid(ResourceRequestDto resourceRequestDto, ConstraintValidatorContext constraintValidatorContext) {
    if (isValidationDisabled()) {
      return true;
    }
    return isValid(resourceRequestDto);
  }

  private boolean isValid(ResourceRequestDto resourceRequestDto) {
    var resource = resourceRequestDto.getResource();
    if (resource instanceof InstanceField instance && hasCurrentLccn(instance)) {
      var inventoryId = findInventoryId(resourceRequestDto);
      return isUnique(getLccnValues(instance), inventoryId);
    }
    return true;
  }

  private boolean isValidationDisabled() {
    try {
      return !settingsService.isSettingEnabled(DUPLICATE_CHECK_SCOPE, DUPLICATE_CHECK_KEY, DUPLICATE_CHECK_PROPERTY);
    } catch (Exception e) {
      log.error("An exception occurred during settings service call", e);
      return true;
    }
  }

  private String findInventoryId(ResourceRequestDto resourceRequestDto) {
    return ofNullable(resourceRequestDto.getId())
      .flatMap(folioMetadataRepository::findInventoryIdById)
      .map(FolioMetadataRepository.InventoryIdOnly::getInventoryId)
      .orElse(null);
  }

  private boolean isUnique(List<String> lccn, String inventoryId) {
    return ofNullable(countInstanceWithLccn(lccn, inventoryId))
      .map(count -> count == 0)
      .orElse(false);
  }

  private Long countInstanceWithLccn(List<String> lccn, String inventoryId) {
    try {
      return searchService.countInstancesByLccnExcludingSuppressedAndId(lccn, inventoryId);
    } catch (Exception e) {
      log.error(e);
      throw exceptionBuilder.failedDependencyException(
        "Could not validate LCCN for duplicate", "Unable to reach search service");
    }
  }

  private boolean hasCurrentLccn(InstanceField instance) {
    return getLccnRequest(instance).anyMatch(LccnUtils::isCurrent);
  }

  private List<String> getLccnValues(InstanceField instance) {
    return getLccnRequest(instance)
      .filter(LccnUtils::isCurrent)
      .map(IdentifierRequest::getValue)
      .flatMap(Collection::stream)
      .toList();
  }

  private Stream<IdentifierRequest> getLccnRequest(InstanceField instance) {
    return instance.getInstance()
      .getMap()
      .stream()
      .filter(LccnField.class::isInstance)
      .map(i -> (LccnField) i)
      .map(LccnField::getLccn);
  }
}
