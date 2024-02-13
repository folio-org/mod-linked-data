package org.folio.linked.data.test;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.jeasy.random.FieldPredicates.named;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.awaitility.Durations.FIVE_SECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_MINUTE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.linked.data.configuration.json.ObjectMapperConfig;
import org.folio.linked.data.model.entity.Resource;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.testcontainers.shaded.org.awaitility.core.ThrowingRunnable;

@UtilityClass
public class TestUtil {

  public static final String FOLIO_TEST_PROFILE = "test-folio";
  public static final String TENANT_ID = "test_tenant";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapperConfig().objectMapper();
  // to be removed after wireframe UI migration
  public static final String BIBFRAME_SAMPLE = loadResourceAsString("samples/bibframe-full.json");
  public static final String INSTANCE_WITH_WORK_REF_SAMPLE = loadResourceAsString("samples/instance_and_work_ref.json");
  public static final String WORK_WITH_INSTANCE_REF_SAMPLE = loadResourceAsString("samples/work_and_instance_ref.json");
  private static final EasyRandomParameters PARAMETERS = new EasyRandomParameters();
  private static final EasyRandom GENERATOR = new EasyRandom(PARAMETERS);
  private static final String FOLIO_OKAPI_URL = "folio.okapi-url";

  static {
    PARAMETERS.excludeField(named("id"));
    PARAMETERS.randomize(Resource.class, MonographTestUtil::getSampleInstanceResource);
    PARAMETERS.randomizationDepth(3);
    PARAMETERS.scanClasspathForConcreteTypes(true);
  }

  @SneakyThrows
  public static String asJsonString(Object value) {
    return OBJECT_MAPPER.writeValueAsString(value);
  }

  public static HttpHeaders defaultHeaders(Environment env) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(APPLICATION_JSON);
    if (Arrays.asList(env.getActiveProfiles()).contains(FOLIO_PROFILE)) {
      httpHeaders.add(TENANT, TENANT_ID);
      httpHeaders.add(URL, getProperty(FOLIO_OKAPI_URL));
    }
    return httpHeaders;
  }

  public static List<RecordHeader> defaultKafkaHeaders() {
    return List.of(
      new RecordHeader(TENANT, TENANT_ID.getBytes(UTF_8)),
      new RecordHeader(URL, getProperty(FOLIO_OKAPI_URL).getBytes(UTF_8))
    );
  }

  @SneakyThrows
  public static String loadResourceAsString(String resourceName) {
    var classLoader = ResourceLoader.class.getClassLoader();
    var is = Objects.requireNonNull(classLoader.getResourceAsStream(resourceName));
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }

  // to be removed after wireframe UI migration
  @SneakyThrows
  public static Map<String, Object> getSampleBibframeDtoMap() {
    return OBJECT_MAPPER.readValue(BIBFRAME_SAMPLE, Map.class);
  }

  @SneakyThrows
  public static Map<String, Object> getSampleInstanceDtoMap() {
    return OBJECT_MAPPER.readValue(INSTANCE_WITH_WORK_REF_SAMPLE, Map.class);
  }

  @SneakyThrows
  public static Map<String, Object> getSampleWorkDtoMap() {
    return OBJECT_MAPPER.readValue(WORK_WITH_INSTANCE_REF_SAMPLE, Map.class);
  }

  public static <T> T random(Class<T> clazz) {
    return GENERATOR.nextObject(clazz);
  }

  public static String randomString() {
    return GENERATOR.nextObject(String.class);
  }

  public static Long randomLong() {
    return GENERATOR.nextLong();
  }

  public static ObjectNode getObjectNode(String label, String name, String link) {
    var node = OBJECT_MAPPER.createObjectNode();
    if (nonNull(label)) {
      node.put(LABEL.getValue(), label);
    }
    if (nonNull(label)) {
      node.put(NAME.getValue(), name);
    }
    if (nonNull(link)) {
      node.put(LINK.getValue(), link);
    }
    return node;
  }

  public static JsonNode getJsonNode(Map<String, ?> map) {
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

  public static void awaitAndAssert(ThrowingRunnable throwingRunnable) {
    await().atMost(ONE_MINUTE)
      .pollDelay(FIVE_SECONDS)
      .pollInterval(ONE_HUNDRED_MILLISECONDS)
      .untilAsserted(throwingRunnable);
  }
}
