package org.folio.linked.data.controller;

import static org.springframework.http.ResponseEntity.ok;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AssignmentCheckDto;
import org.folio.linked.data.rest.resource.AuthorityApi;
import org.folio.linked.data.service.resource.AssignAuthorityTarget;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorityAssignmentController implements AuthorityApi {

  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public ResponseEntity<String> authorityAssignmentCheck(String okapiTenant, AssignmentCheckDto dto) {
    return ok(String.valueOf(
      resourceMarcAuthorityService.isMarcCompatibleWithTarget(
        dto.getRawMarc(),
        AssignAuthorityTarget.valueOf(dto.getTarget().name()))
      ));
  }
}
