package org.folio.linked.data.validation.dto;

import static java.util.Optional.ofNullable;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.SearchResponseTotalOnly;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.service.search.InstanceSearchService;
import org.folio.linked.data.util.LccnUtils;
import org.folio.linked.data.validation.LccnUniqueConstraint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("javaarchitecture:S7091")
public class LccnUniquenessValidator implements ConstraintValidator<LccnUniqueConstraint, ResourceRequestDto> {

  private final InstanceSearchService instanceSearchService;
  private final FolioMetadataRepository folioMetadataRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public boolean isValid(ResourceRequestDto resourceRequestDto, ConstraintValidatorContext constraintValidatorContext) {
    var resource = resourceRequestDto.getResource();
    if (resource instanceof InstanceField instance && hasCurrentLccn(instance)) {
      var inventoryId = findInventoryId(resourceRequestDto);
      return isUnique(getLccnValues(instance), inventoryId);
    }
    return true;
  }

  private String findInventoryId(ResourceRequestDto resourceRequestDto) {
    return ofNullable(resourceRequestDto.getId())
      .flatMap(folioMetadataRepository::findInventoryIdById)
      .map(FolioMetadataRepository.InventoryIdOnly::getInventoryId)
      .orElse(null);
  }

  private boolean isUnique(List<String> lccn, String inventoryId) {
    return ofNullable(findInstanceWithLccn(lccn, inventoryId))
      .map(SearchResponseTotalOnly::getTotalRecords)
      .map(count -> count == 0)
      .orElse(false);
  }

  private SearchResponseTotalOnly findInstanceWithLccn(List<String> lccn, String inventoryId) {
    try {
      return instanceSearchService.searchByLccnExcludingId(lccn, inventoryId);
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
      .map(LccnRequest::getValue)
      .flatMap(Collection::stream)
      .toList();
  }

  private Stream<LccnRequest> getLccnRequest(InstanceField instance) {
    return instance.getInstance()
      .getMap()
      .stream()
      .filter(LccnField.class::isInstance)
      .map(i -> (LccnField) i)
      .map(LccnField::getLccn);
  }
}
