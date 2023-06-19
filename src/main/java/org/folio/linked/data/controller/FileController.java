package org.folio.linked.data.controller;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.UploadedFileRef;
import org.folio.linked.data.rest.resource.FilesApi;
import org.folio.linked.data.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class FileController implements FilesApi {

  private final FileService fileService;

  @Override
  public ResponseEntity<UploadedFileRef> uploadFile(String okapiTenant, MultipartFile file) {
    return ResponseEntity.ok(fileService.upload(okapiTenant, file));
  }

  @Override
  public ResponseEntity<Resource> downloadFile(String objectName, String okapiTenant) {
    return ResponseEntity.ok(new ByteArrayResource(fileService.download(okapiTenant, objectName)));
  }
}
