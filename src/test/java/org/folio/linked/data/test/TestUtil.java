package org.folio.linked.data.test;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.linked.data.service.lccn.LccnResourceService.LccnResourceSearchResult;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.JsonUtils.JSON_MAPPER;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.folio.spring.integration.XOkapiHeaders.USER_ID;
import static org.jeasy.random.FieldPredicates.named;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.awaitility.Durations.FIVE_SECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.testcontainers.shaded.org.awaitility.Durations.TWO_MINUTES;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.ResourceResponseField;
import org.folio.linked.data.domain.dto.TitleFieldResponseTitleInner;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.ResourceModelMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceSubgraphView;
import org.folio.linked.data.test.json.IdentifierFieldResponseDeserializer;
import org.folio.linked.data.test.json.ResourceResponseFieldDeserializer;
import org.folio.linked.data.test.json.TitleFieldResponseDeserializer;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.shaded.org.awaitility.core.ThrowingRunnable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@UtilityClass
public class TestUtil {

  public static final String STANDALONE_TEST_PROFILE = "test-standalone";
  public static final String TENANT_ID = "test_tenant";
  public static final String RECORD_DOMAIN_EVENT_TOPIC = "srs.source_records";
  public static final String INVENTORY_INSTANCE_EVENT_TOPIC = "inventory.instance";
  public static final String LD_IMPORT_OUTPUT_TOPIC = "linked_data_import.output";
  public static final JsonMapper TEST_JSON_MAPPER = JSON_MAPPER.rebuild()
    .addModule(new SimpleModule()
      .addDeserializer(ResourceResponseField.class, new ResourceResponseFieldDeserializer())
      .addDeserializer(TitleFieldResponseTitleInner.class, new TitleFieldResponseDeserializer())
      .addDeserializer(IdentifierFieldResponse.class, new IdentifierFieldResponseDeserializer()))
    .build();
  public static final String INSTANCE_WITH_WORK_REF_SAMPLE = loadResourceAsString("samples/instance_and_work_ref.json");
  public static final String WORK_WITH_INSTANCE_REF_SAMPLE = loadResourceAsString("samples/work_and_instance_ref.json");
  public static final ResourceModelMapper RESOURCE_MODEL_MAPPER = new ResourceModelMapperImpl();
  public static final String FOLIO_OKAPI_URL = "folio.okapi-url";
  private static final EasyRandomParameters PARAMETERS = new EasyRandomParameters();
  private static final EasyRandom GENERATOR = new EasyRandom(PARAMETERS);

  static {
    PARAMETERS.excludeField(named("id"));
    PARAMETERS.randomizationDepth(3);
    PARAMETERS.scanClasspathForConcreteTypes(true);
  }

  public static HttpHeaders defaultHeaders(Environment env) {
    var httpHeaders = new HttpHeaders();
    if (!isStandaloneTest(env)) {
      httpHeaders.add(TENANT, TENANT_ID);
      httpHeaders.add(URL, getProperty(FOLIO_OKAPI_URL));
    }
    return httpHeaders;
  }

  public static boolean isStandaloneTest(Environment env) {
    return asList(env.getActiveProfiles()).contains(STANDALONE_PROFILE);
  }

  public static HttpHeaders defaultHeadersWithUserId(Environment env, String value) {
    var httpHeaders = defaultHeaders(env);
    httpHeaders.add(USER_ID, value);
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

  public static Map<String, Object> getSampleInstanceDtoMap() {
    return TEST_JSON_MAPPER.readValue(INSTANCE_WITH_WORK_REF_SAMPLE, Map.class);
  }

  public static Map<String, Object> getSampleWorkDtoMap() {
    return TEST_JSON_MAPPER.readValue(WORK_WITH_INSTANCE_REF_SAMPLE, Map.class);
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

  public static JsonNode getJsonNode(Map<String, ?> map) {
    return TEST_JSON_MAPPER.convertValue(map, JsonNode.class);
  }

  public static void awaitAndAssert(ThrowingRunnable throwingRunnable) {
    await().atMost(TWO_MINUTES)
      .pollDelay(FIVE_SECONDS)
      .pollInterval(ONE_HUNDRED_MILLISECONDS)
      .untilAsserted(throwingRunnable);
  }

  public static void assertAuthority(Resource resource,
                                     String label,
                                     boolean isActive,
                                     boolean isPreferred,
                                     Resource replacedBy) {
    assertThat(resource)
      .hasFieldOrPropertyWithValue("label", label)
      .hasFieldOrPropertyWithValue("active", isActive)
      .satisfies(r -> assertThat(r.getDoc()).isNotEmpty())
      .satisfies(r ->
        assertThat(resource.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).asBoolean()).isEqualTo(isPreferred)
      )
      .satisfies(r -> assertThat(r.getOutgoingEdges()).isNotEmpty())
      .extracting(Resource::getOutgoingEdges)
      .satisfies(resourceEdges -> assertThat(resourceEdges)
        .isNotEmpty()
        .allMatch(edge -> Objects.equals(edge.getSource(), resource))
        .allMatch(edge -> nonNull(edge.getTarget()))
        .allMatch(edge -> nonNull(edge.getPredicate()))
        .anyMatch(edge -> isNull(replacedBy) || edge.getPredicate().getUri().equals(REPLACED_BY.getUri())
          && edge.getTarget().equals(replacedBy))
      );
  }

  public static void assertResourceMetadata(Resource resource, UUID createdBy, UUID updatedBy) {
    assertNotNull(resource.getCreatedDate());
    assertNotNull(resource.getUpdatedDate());
    assertEquals(createdBy, resource.getCreatedBy());
    assertEquals(updatedBy, resource.getUpdatedBy());
  }

  public static void cleanResourceTables(JdbcTemplate jdbcTemplate) {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "folio_metadata", "resource_edges", "resource_type_map",
      "resources");
  }

  public static ErrorResponseConfig.Error genericError(int parametersCount) {
    return new ErrorResponseConfig.Error(
      GENERATOR.nextInt(100, 999),
      "genericCode",
      genericParameters(parametersCount),
      genericMessage(parametersCount));
  }

  public static RequestProcessingException emptyRequestProcessingException() {
    return new RequestProcessingException(0, "", emptyMap(), "");
  }

  private static List<String> genericParameters(int parametersCount) {
    return IntStream.range(0, parametersCount)
      .mapToObj(i -> "parameter_" + i)
      .collect(toCollection(ArrayList::new));
  }

  private static String genericMessage(int parametersCount) {
    return IntStream.range(0, parametersCount)
      .mapToObj(i -> "message_part_" + i)
      .collect(Collectors.joining());
  }

  public static LccnResourceSearchResult getLccnResourceSearchResult(String resourceSubgraph, String inventoryId) {
    return new LccnResourceSearchResult(
      new ResourceSubgraphView().setResourceSubgraph(resourceSubgraph).setInventoryId(inventoryId), inventoryId
    );
  }

  public static <T> T executeWithContext(ExecutionContextBuilder contextBuilder, String tenantId,
                                         java.util.concurrent.Callable<T> callable) {
    var context = buildContext(contextBuilder, tenantId);
    try (@SuppressWarnings("unused") var fex = new FolioExecutionContextSetter(context)) {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException("Failed to execute with context", e);
    }
  }

  public static void executeAsyncWithContext(ExecutionContextBuilder contextBuilder, String tenantId,
                                             Runnable runnable) {
    var context = buildContext(contextBuilder, tenantId);
    try (@SuppressWarnings("unused") var fex = new FolioExecutionContextSetter(context)) {
      runnable.run();
    }
  }

  private static FolioExecutionContext buildContext(ExecutionContextBuilder contextBuilder, String tenantId) {
    var okapiUrl = getProperty(FOLIO_OKAPI_URL);
    return contextBuilder.builder()
      .withTenantId(tenantId)
      .withOkapiUrl(okapiUrl)
      .build();
  }
}
