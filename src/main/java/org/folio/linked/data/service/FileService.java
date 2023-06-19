package org.folio.linked.data.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.folio.linked.data.domain.dto.UploadedFileRef;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

  UploadedFileRef upload(String okapiTenant, MultipartFile file);

  UploadedFileRef upload(String okapiTenant, String name, JsonNode jsonNode);

  byte[] download(String okapiTenant, String objectName);

  void delete(String okapiTenant, String slug);
}
