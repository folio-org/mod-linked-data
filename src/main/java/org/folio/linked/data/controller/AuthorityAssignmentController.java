package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.ok;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AssignmentCheckDto;
import org.folio.linked.data.domain.dto.AssignmentCheckResponseDto;
import org.folio.linked.data.rest.resource.AuthorityApi;
import org.folio.linked.data.service.resource.marc.AssignAuthorityTarget;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorityAssignmentController implements AuthorityApi {

  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public ResponseEntity<AssignmentCheckResponseDto> authorityAssignmentCheck(AssignmentCheckDto dto) {
    var validationResult = resourceMarcAuthorityService.validateAuthorityAssignment(
      dto.getRawMarc(),
      AssignAuthorityTarget.valueOf(dto.getTarget().name()));
    return ok(validationResult);
  }
}
