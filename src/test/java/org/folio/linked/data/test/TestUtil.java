package org.folio.linked.data.test;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_ID;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_LABEL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_URI;
import static org.folio.linked.data.util.Bibframe2Constants.SAME_AS_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.jeasy.random.FieldPredicates.named;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.folio.linked.data.configuration.json.ObjectMapperConfig;
import org.folio.linked.data.domain.dto.Property2;
import org.folio.linked.data.domain.dto.ProvisionActivity2;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.spring.integration.XOkapiHeaders;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class TestUtil {

  public static final String TENANT_ID = "test_tenant";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapperConfig().objectMapper();
  private static final String BIBFRAME_SAMPLE = loadResourceAsString("samples/bibframe-full.json");
  private static final String BIBFRAME_2_SAMPLE = loadResourceAsString("samples/bibframe2-full.json");
  private static final String INDEX_TRUE_SAMPLE = loadResourceAsString("samples/index-true.json");
  private static final String INDEX_FALSE_SAMPLE = loadResourceAsString("samples/index-false.json");
  private static final EasyRandomParameters PARAMETERS = new EasyRandomParameters();

  private static final EasyRandom GENERATOR = new EasyRandom(PARAMETERS);

  static {
    PARAMETERS.excludeField(named("id"));
    PARAMETERS.randomize(named("configuration"), TestUtil::getBibframe2JsonNodeSample);
    PARAMETERS.randomize(named("_configuration"), TestUtil::getBibframe2Sample);
    PARAMETERS.randomize(Resource.class, TestUtil::bibframe2SampleResource);
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

  public static String getBibframe2Sample() {
    return BIBFRAME_2_SAMPLE;
  }

  public static String getIndexTrueSample() {
    return INDEX_TRUE_SAMPLE;
  }

  public static String getIndexFalseSample() {
    return INDEX_FALSE_SAMPLE;
  }

  public static String getResource(String fileName) {
    return loadResourceAsString(fileName);
  }

  @SneakyThrows
  public static JsonNode getBibframeJsonNodeSample() {
    return OBJECT_MAPPER.readTree(BIBFRAME_SAMPLE);
  }

  @SneakyThrows
  public static JsonNode getBibframe2JsonNodeSample() {
    return OBJECT_MAPPER.readTree(BIBFRAME_2_SAMPLE);
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

  public static Resource bibframeSampleResource(Long resourceHash, ResourceType type) {
    var bibframe = bibframeSampleResource();
    bibframe.setResourceHash(resourceHash);
    bibframe.addType(type);
    return bibframe;
  }

  public static Resource bibframe2SampleResource() {
    var resource = new Resource();
    resource.setDoc(getBibframe2JsonNodeSample());
    return resource;
  }

  public static Resource bibframe2SampleResource(Long resourceHash, ResourceType profile) {
    var bibframe = bibframe2SampleResource();
    bibframe.setResourceHash(resourceHash);
    bibframe.addType(profile);
    return bibframe;
  }

  public static JsonNode getSameAsJsonNode(ObjectNode... personNodes) {
    var arrayNode = OBJECT_MAPPER.createArrayNode();
    Arrays.stream(personNodes).forEach(arrayNode::add);
    var node = OBJECT_MAPPER.createObjectNode();
    node.set(SAME_AS_PRED, arrayNode);
    return node;
  }

  public static ObjectNode getPropertyNode(String id, String label, String uri) {
    var node = OBJECT_MAPPER.createObjectNode();
    if (nonNull(id)) {
      node.put(PROPERTY_ID, id);
    }
    if (nonNull(label)) {
      node.put(PROPERTY_LABEL, label);
    }
    if (nonNull(uri)) {
      node.put(PROPERTY_URI, uri);
    }
    return node;
  }

  public static JsonNode getJsonNode(Map<String, ?> map) {
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

  public static JsonNode propertyToDoc(Property2 property) {
    var map = new HashMap<String, String>();
    map.put(PROPERTY_ID, property.getId());
    map.put(PROPERTY_LABEL, property.getLabel());
    map.put(PROPERTY_URI, property.getUri());
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

  public static JsonNode provisionActivityToDoc(ProvisionActivity2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE_URL, dto.getDate());
    map.put(SIMPLE_AGENT_PRED, dto.getSimpleAgent());
    map.put(SIMPLE_DATE_PRED, dto.getSimpleDate());
    map.put(SIMPLE_PLACE_PRED, dto.getSimplePlace());
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }
}
