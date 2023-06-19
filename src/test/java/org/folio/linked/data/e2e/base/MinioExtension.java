package org.folio.linked.data.e2e.base;

import io.minio.MinioClient;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

public class MinioExtension implements Extension, BeforeEachCallback, AfterEachCallback {

  private GenericContainer<?> minioServer;
  private MinioClient minioClient;

  @Override
  public void beforeEach(ExtensionContext context) {
    minioServer = new GenericContainer<>("minio/minio:latest")
      .withEnv("MINIO_ROOT_USER", "admin")
      .withEnv("MINIO_ROOT_PASSWORD", "admin123")
      .withCommand("server /data")
      .withExposedPorts(9000);

    minioServer.start();

    minioClient = MinioClient.builder()
      .endpoint(minioServer.getHost(), minioServer.getMappedPort(9000), false)
      .credentials("admin", "admin123")
      .build();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    minioServer.stop();
  }

  public MinioClient getMinioClient() {
    return minioClient;
  }
}
