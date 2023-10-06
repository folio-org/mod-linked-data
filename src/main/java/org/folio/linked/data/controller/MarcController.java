package org.folio.linked.data.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.mapper.marc.Marc2BibframeMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.rest.resource.MarcApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MarcController implements MarcApi {

  private final Marc2BibframeMapper marc2BibframeMapper;
  private final ResourceMapper resourceMapper;
  private final ResourceRepository resourceRepository;

  @SneakyThrows
  @Override
  public ResponseEntity<ResourceDto> createResourceFromMarc(String xOkapiTenant, Resource body) {
    var converted = marc2BibframeMapper.map(body.getContentAsString(UTF_8));
    var saved = resourceRepository.save(converted);
    return ResponseEntity.ok(resourceMapper.toDto(saved));
  }
}
