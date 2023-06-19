package org.folio.linked.data;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.jeasy.random.FieldPredicates.named;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.folio.linked.data.configuration.ObjectMapperConfig;
import org.folio.linked.data.domain.dto.ResourceCreateRequest;
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
  private static final String BIBFRAME_SAMPLE = loadResourceAsString("bibframe-sample.json");
  private static final EasyRandomParameters PARAMETERS = new EasyRandomParameters();

  private static final EasyRandom GENERATOR = new EasyRandom(PARAMETERS);

  static {
    PARAMETERS.excludeField(named("id"));
    PARAMETERS.randomize(named("configuration"), TestUtil::getResourceJsonNodeSample);
    PARAMETERS.randomize(named("_configuration"), TestUtil::getResourceSample);
    PARAMETERS.randomize(Resource.class, TestUtil::randomResource);
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
      httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    }
    return httpHeaders;
  }

  @SneakyThrows
  public static String loadResourceAsString(String resourceName) {
    var classLoader = ResourceLoader.class.getClassLoader();
    var is = Objects.requireNonNull(classLoader.getResourceAsStream(resourceName));
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }

  public static String getResourceSample() {
    return BIBFRAME_SAMPLE;
  }

  @SneakyThrows
  public static JsonNode getResourceJsonNodeSample() {
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

  public static ResourceCreateRequest randomResourceCreateRequest() {
    return GENERATOR.nextObject(ResourceCreateRequest.class);
  }

  public static Resource randomResource() {
    var resource = new Resource();
    resource.setDoc(getResourceJsonNodeSample());
    return resource;
  }

  public static Resource randomResource(Long resourceHash, ResourceType profile) {
    var bibframe = randomResource();
    bibframe.setResourceHash(resourceHash);
    bibframe.setType(profile);
    return bibframe;
  }

}
