package org.folio.linked.data.repo;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.folio.linked.data.configuration.properties.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MinioRepo {
  private final MinioClient minioClient;

  public MinioRepo(@Autowired MinioProperties minioProperties) {
    minioClient = MinioClient.builder()
      .endpoint(minioProperties.getEndpoint(), 9000, false)
      .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
      .build();
  }

  @SneakyThrows
  public boolean bucketExists(String name) {
    return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
  }

  @SneakyThrows
  public void makeBucket(String name) {
    minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
  }

  @SneakyThrows
  public ObjectWriteResponse putObject(String bucket, String objectName, InputStream is, long size, long partSize) {
    return minioClient.putObject(PutObjectArgs.builder()
      .bucket(bucket)
      .object(objectName)
      .stream(is, size, partSize)
      .build());
  }

  @SneakyThrows
  public GetObjectResponse getObject(String bucket, String objectName) {
    return minioClient.getObject(GetObjectArgs.builder()
      .bucket(bucket)
      .object(objectName)
      .build());
  }

  @SneakyThrows
  public void delete(String bucket, String objectName) {
    minioClient.removeObject(RemoveObjectArgs.builder()
      .bucket(bucket)
      .object(objectName)
      .build());
  }
}
