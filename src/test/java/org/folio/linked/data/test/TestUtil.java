package org.folio.linked.data.test;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.jeasy.random.FieldPredicates.named;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.configuration.json.ObjectMapperConfig;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.integration.XOkapiHeaders;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class TestUtil {

  public static final String FOLIO_TEST_PROFILE = "test-folio";
  public static final String TENANT_ID = "test_tenant";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapperConfig().objectMapper();
  private static final String BIBFRAME_SAMPLE = loadResourceAsString("samples/bibframe-full.json");
  private static final EasyRandomParameters PARAMETERS = new EasyRandomParameters();

  private static final EasyRandom GENERATOR = new EasyRandom(PARAMETERS);

  static {
    PARAMETERS.excludeField(named("id"));
    PARAMETERS.randomize(Resource.class, TestUtil::bibframeSampleResource);
    PARAMETERS.randomizationDepth(3);
    PARAMETERS.scanClasspathForConcreteTypes(true);
  }

  @SneakyThrows
  public static String asJsonString(Object value) {
    return OBJECT_MAPPER.writeValueAsString(value);
  }

  public static HttpHeaders defaultHeaders(Environment env) {
    return defaultHeaders(env, null);
  }

  public static HttpHeaders defaultHeaders(Environment env, String okapiUrl) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(APPLICATION_JSON);
    if (Arrays.asList(env.getActiveProfiles()).contains(FOLIO_PROFILE)) {
      httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    }
    if (nonNull(okapiUrl)) {
      httpHeaders.add(XOkapiHeaders.URL, okapiUrl);
    }
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

  public static String getBibframeSampleTest(String changedField) {
    JsonNode jsonNode = getBibframeJsonNodeSample();
    JsonNode instance = jsonNode.get("resource").get(INSTANCE.getUri());
    ((ArrayNode) instance.withArray(DIMENSIONS.getValue())).set(0, new TextNode(changedField));
    return jsonNode.toString();
  }

  public static String getResource(String fileName) {
    return loadResourceAsString(fileName);
  }

  @SneakyThrows
  public static JsonNode getBibframeJsonNodeSample() {
    return OBJECT_MAPPER.readTree(BIBFRAME_SAMPLE);
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

  public static Resource bibframeSampleResource() {
    var resource = new Resource();
    resource.setDoc(getBibframeJsonNodeSample());
    return resource;
  }

  public static Resource bibframeSampleResource(Long resourceHash, ResourceTypeDictionary type) {
    var bibframe = bibframeSampleResource();
    bibframe.setResourceHash(resourceHash);
    bibframe.addType(type);
    return bibframe;
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
}
