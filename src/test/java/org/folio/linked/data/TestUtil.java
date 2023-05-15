package org.folio.linked.data;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class TestUtil {

  public static final String TENANT_ID = "test_tenant";
  public static final String GRAPH_NAME = "graphName";

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  private static final String BIBFRAME_SAMPLE = loadResourceAsString("bibframe-sample.json");

  public static String getOkapiMockUrl() {
    return System.getProperty("folio.okapi-url");
  }

  @SneakyThrows
  public static String asJsonString(Object value) {
    return OBJECT_MAPPER.writeValueAsString(value);
  }

  public static String randomId() {
    return UUID.randomUUID().toString();
  }

  public static HttpHeaders defaultHeaders(String okapiUrl) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    httpHeaders.add(XOkapiHeaders.URL, okapiUrl);
    return httpHeaders;
  }

  @SneakyThrows
  public static String loadResourceAsString(String resourceName) {
    var classLoader = ResourceLoader.class.getClassLoader();
    var is = Objects.requireNonNull(classLoader.getResourceAsStream(resourceName));
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }

  public static String getBibframeSample() {
    return BIBFRAME_SAMPLE;
  }

}
