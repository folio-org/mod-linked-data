package org.folio.linked.data.e2e.base;

import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@Log4j2
public class ElasticSearchContainerExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String SPRING_PROPERTY_NAME = "spring.opensearch.uris";
  private static final String IMAGE_NAME = "dev.folio/searchengine";
  private static final String DOCKERFILE = "opensearch/Dockerfile";
  private static final GenericContainer<?> CONTAINER = createContainer();

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!CONTAINER.isRunning()) {
      CONTAINER.start();
    }
    System.setProperty(SPRING_PROPERTY_NAME, getSearchUrl());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    System.clearProperty(SPRING_PROPERTY_NAME);
  }

  private String getSearchUrl() {
    return "http://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(9200);
  }

  @SneakyThrows
  private static GenericContainer<?> createContainer() {
    var dockerfile = ElasticSearchContainerExtension.class.getClassLoader().getResource(DOCKERFILE);
    log.info("search engine dockerfile: {}", dockerfile);
    var container = new GenericContainer<>(new ImageFromDockerfile(IMAGE_NAME, false)
      .withDockerfile(new File(dockerfile.toURI()).toPath()))
      .withEnv("discovery.type", "single-node")
      .withEnv("DISABLE_SECURITY_PLUGIN", "true")
      .withExposedPorts(9200);
    return container;
  }
}
