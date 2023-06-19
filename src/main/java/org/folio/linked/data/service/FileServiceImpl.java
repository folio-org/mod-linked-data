package org.folio.linked.data.service;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.UploadedFileRef;
import org.folio.linked.data.repo.MinioRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

  private final MinioRepo minioRepo;
  @Value("${mod-linked-data.default-schema}")
  private String defaultSchema;

  @SneakyThrows
  @Override
  public UploadedFileRef upload(String okapiTenant, MultipartFile multipartFile) {
    var bucket = getBucket(okapiTenant);
    ObjectWriteResponse owr = minioRepo.putObject(bucket, multipartFile.getOriginalFilename(),
      multipartFile.getInputStream(), multipartFile.getSize(), -1);
    return new UploadedFileRef()
      .etag(owr.etag())
      .objectName(multipartFile.getOriginalFilename())
      .versionId(owr.versionId());
  }

  @SneakyThrows
  @Override
  public UploadedFileRef upload(String okapiTenant, String name, JsonNode jsonNode) {
    var bucket = getBucket(okapiTenant);
    byte[] jsonBytes = jsonNode.toString().getBytes();
    ObjectWriteResponse owr = minioRepo.putObject(bucket, name,
      new ByteArrayInputStream(jsonBytes), jsonBytes.length, -1);
    return new UploadedFileRef()
      .etag(owr.etag())
      .objectName(name)
      .versionId(owr.versionId());
  }

  @SneakyThrows
  @Override
  public byte[] download(String okapiTenant, String objectName) {
    var bucket = getBucket(okapiTenant);
    GetObjectResponse getObjectResponse = minioRepo.getObject(bucket, objectName);
    return getObjectResponse.readAllBytes();
  }

  @Override
  public void delete(String okapiTenant, String slug) {
    minioRepo.delete(getBucket(okapiTenant), slug);
  }

  private String getBucket(String okapiTenant) {
    String bucket;
    if (isNull(okapiTenant)) {
      bucket = defaultSchema;
    } else {
      bucket = okapiTenant;
    }
    if (!minioRepo.bucketExists(bucket)) {
      minioRepo.makeBucket(bucket);
    }
    return bucket;
  }
}
