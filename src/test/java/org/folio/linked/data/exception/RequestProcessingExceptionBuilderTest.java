package org.folio.linked.data.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.util.Constants.LINKED_DATA_STORAGE;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.folio.linked.data.configuration.ErrorResponseConfig;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RequestProcessingExceptionBuilderTest {

  @InjectMocks
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Mock
  private ErrorResponseConfig errorResponseConfig;

  @ParameterizedTest
  @MethodSource("exceptionTestCases")
  void shouldBuildException(TestCase testCase) {
    // given
    when(testCase.configGetter.apply(errorResponseConfig)).thenReturn(testCase.error);

    // when
    var result = testCase.builderMethod.apply(exceptionBuilder);

    // then
    assertThat(result.getStatus()).isEqualTo(testCase.expectedStatus);
    assertThat(result.getCode()).isEqualTo(testCase.expectedCode);
    testCase.expectedParameters.forEach((key, value) ->
      assertThat(result.getParameters()).containsEntry(key, value)
    );
    assertThat(result.getMessage()).isEqualTo(testCase.expectedMessage);
  }

  private static Stream<Arguments> exceptionTestCases() {
    return Stream.of(
      Arguments.of(new TestCase(
        "alreadyExists",
        new ErrorResponseConfig.Error(409, "alreadyExists", List.of("param1", "param2"), "message: %s %s"),
        ErrorResponseConfig::getAlreadyExists,
        builder -> builder.alreadyExistsException("ISBN", "123456"),
        409,
        "alreadyExists",
        Map.of("param1", "ISBN", "param2", "123456"),
        "message: ISBN 123456"
      )),
      Arguments.of(new TestCase(
        "mapping",
        new ErrorResponseConfig.Error(400, "mapping", List.of("class", "value"), "Mapping error: %s %s"),
        ErrorResponseConfig::getMapping,
        builder -> builder.mappingException("WorkDto", "testValue"),
        400,
        "mapping",
        Map.of("class", "WorkDto", "value", "testValue"),
        "Mapping error: WorkDto testValue"
      )),
      Arguments.of(new TestCase(
        "notSupported",
        new ErrorResponseConfig.Error(400, "notSupported", List.of("type", "process"), "Not supported: %s for %s"),
        ErrorResponseConfig::getNotSupported,
        builder -> builder.notSupportedException("Instance", "import"),
        400,
        "notSupported",
        Map.of("type", "Instance", "process", "import"),
        "Not supported: Instance for import"
      )),
      Arguments.of(new TestCase(
        "required",
        new ErrorResponseConfig.Error(400, "required", List.of("object"), "Required: %s"),
        ErrorResponseConfig::getRequired,
        builder -> builder.requiredException("id"),
        400,
        "required",
        Map.of("object", "id"),
        "Required: id"
      )),
      Arguments.of(new TestCase(
        "notFoundByInventoryId",
        new ErrorResponseConfig.Error(404, "notFound",
          List.of("resourceType", "idType", "idValue", "storage"), "%s with %s=%s not found in %s"),
        ErrorResponseConfig::getNotFound,
        builder -> builder.notFoundLdResourceByInventoryIdException("12345"),
        404,
        "notFound",
        Map.of("resourceType", "Resource", "idType", "inventoryId", "idValue", "12345", "storage", LINKED_DATA_STORAGE),
        "Resource with inventoryId=12345 not found in Linked Data storage"
      )),
      Arguments.of(new TestCase(
        "notFoundById",
        new ErrorResponseConfig.Error(404, "notFound",
          List.of("resourceType", "idType", "idValue", "storage"), "%s with %s=%s not found in %s"),
        ErrorResponseConfig::getNotFound,
        builder -> builder.notFoundLdResourceByIdException("Work", "999"),
        404,
        "notFound",
        Map.of("resourceType", "Work", "idType", "id", "idValue", "999", "storage", LINKED_DATA_STORAGE),
        "Work with id=999 not found in Linked Data storage"
      )),
      Arguments.of(new TestCase(
        "notFoundSourceRecord",
        new ErrorResponseConfig.Error(404, "notFound",
          List.of("resourceType", "idType", "idValue", "storage"), "%s with %s=%s not found in %s"),
        ErrorResponseConfig::getNotFound,
        builder -> builder.notFoundSourceRecordException("instanceId", "abc123"),
        404,
        "notFound",
        Map.of("resourceType", "Source Record", "idType", "instanceId", "idValue", "abc123", "storage",
          "Source Record storage"),
        "Source Record with instanceId=abc123 not found in Source Record storage"
      )),
      Arguments.of(new TestCase(
        "notFoundResourceByUri",
        new ErrorResponseConfig.Error(404, "notFound",
          List.of("resourceType", "idType", "idValue", "storage"), "%s with %s=%s not found in %s"),
        ErrorResponseConfig::getNotFound,
        builder -> builder.notFoundResourceByUriException("https://example.com/hub.json"),
        404,
        "notFound",
        Map.of("resourceType", "Resource", "idType", "URI", "idValue", "https://example.com/hub.json", "storage", "remote source"),
        "Resource with URI=https://example.com/hub.json not found in remote source"
      )),
      Arguments.of(new TestCase(
        "failedDependency",
        new ErrorResponseConfig.Error(424, "failedDependency", List.of("message", "reason"), "Failed: %s - %s"),
        ErrorResponseConfig::getFailedDependency,
        builder -> builder.failedDependencyException("Dependency failed", "Service unavailable"),
        424,
        "failedDependency",
        Map.of("message", "Dependency failed", "reason", "Service unavailable"),
        "Failed: Dependency failed - Service unavailable"
      )),
      Arguments.of(new TestCase(
        "badRequest",
        new ErrorResponseConfig.Error(400, "badRequest", List.of("message", "reason"), "Bad request: %s - %s"),
        ErrorResponseConfig::getGenericBadRequest,
        builder -> builder.badRequestException("Invalid request", "Missing field"),
        400,
        "badRequest",
        Map.of("message", "Invalid request", "reason", "Missing field"),
        "Bad request: Invalid request - Missing field"
      ))
    );
  }

  private record TestCase(
    String name,
    ErrorResponseConfig.Error error,
    Function<ErrorResponseConfig, ErrorResponseConfig.Error> configGetter,
    Function<RequestProcessingExceptionBuilder, RequestProcessingException> builderMethod,
    int expectedStatus,
    String expectedCode,
    Map<String, String> expectedParameters,
    String expectedMessage
  ) {
  }
}
