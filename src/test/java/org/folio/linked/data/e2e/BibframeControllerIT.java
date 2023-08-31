package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.bibframeSampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.BibframeConstants.CARRIER;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.ID;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MEDIA;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.TYPE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
public class BibframeControllerIT {

  public static final String BIBFRAME_URI = "/bibframe";
  public static OkapiConfiguration okapi;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private MonographTestService monographTestService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createMonographInstanceBibframe_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URI)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframeSample());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateSampleBibframeResponse(resultActions)
      .andReturn().getResponse().getContentAsString();

    var bibframeResponse = objectMapper.readValue(response, BibframeResponse.class);
    var persistedOptional = resourceRepo.findById(bibframeResponse.getId());
    assertThat(persistedOptional).isPresent();
    var bibframe = persistedOptional.get();
    validateSampleBibframeEntity(bibframe);
    checkKafkaMessageSent(bibframe, null);
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    var requestBuilder = get(BIBFRAME_URI + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    validateSampleBibframeResponse(resultActions);
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomLong();
    var requestBuilder = get(BIBFRAME_URI + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given id ["
        + notExistedId + "] is not found")))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void getBibframeShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      resourceRepo.save(bibframeSampleResource(1L, monographTestService.getMonographType())),
      resourceRepo.save(bibframeSampleResource(2L, monographTestService.getMonographType())),
      resourceRepo.save(bibframeSampleResource(3L, monographTestService.getMonographType()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAME_URI)
      .param(TYPE, monographTestService.getMonographType().getTypeUri())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("number", equalTo(0)))
      .andExpect(jsonPath("total_pages", equalTo(1)))
      .andExpect(jsonPath("total_elements", equalTo(3)))
      .andExpect(jsonPath("content", hasSize(3)))
      .andExpect(jsonPath("content[0].id", equalTo(existed.get(0).getResourceHash().intValue())))
      .andExpect(jsonPath("content[1].id", equalTo(existed.get(1).getResourceHash().intValue())))
      .andExpect(jsonPath("content[2].id", equalTo(existed.get(2).getResourceHash().intValue())));
  }

  @Test
  void deleteBibframeById_shouldDeleteRootResourceAndRootEdge() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(5);
    assertThat(resourceEdgeRepository.count()).isEqualTo(4);
    var requestBuilder = delete(BIBFRAME_URI + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(4);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(3);
    checkKafkaMessageSent(null, existed.getResourceHash());
  }

  protected void checkKafkaMessageSent(Resource persisted, Long deleted) {
    // nothing to check without Folio profile
  }

  @NotNull
  private ResultActions validateSampleBibframeResponse(ResultActions resultActions) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath(ID, notNullValue()))
      .andExpect(jsonPath(TYPE, equalTo(MONOGRAPH)))
      .andExpect(jsonPath("$." + path(INSTANCE), notNullValue()))
      .andExpect(jsonPath("$." + toCarrier(), equalTo("carrier")))
      .andExpect(jsonPath("$." + toCopyrightDate(), equalTo("copyright date")))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath("$." + toInstanceTitlePartName(), equalTo("Instance: partName")))
      .andExpect(jsonPath("$." + toInstanceTitlePartNumber(), equalTo("Instance: partNumber")))
      .andExpect(jsonPath("$." + toInstanceTitleMain(), equalTo("Instance: Laramie holds the range")))
      .andExpect(jsonPath("$." + toInstanceTitleNonSortNum(), equalTo("Instance: nonSortNum")))
      .andExpect(jsonPath("$." + toInstanceTitleSubtitle(), equalTo("Instance: subtitle")))
      .andExpect(jsonPath("$." + toMedia(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toParallelTitlePartName(), equalTo("Parallel: partName")))
      .andExpect(jsonPath("$." + toParallelTitlePartNumber(), equalTo("Parallel: partNumber")))
      .andExpect(jsonPath("$." + toParallelTitleMain(), equalTo("Parallel: Laramie holds the range")))
      .andExpect(jsonPath("$." + toParallelTitleNote(), equalTo("Parallel: noteLabel")))
      .andExpect(jsonPath("$." + toParallelTitleDate(), equalTo("Parallel: date")))
      .andExpect(jsonPath("$." + toParallelTitleSubtitle(), equalTo("Parallel: subtitle")))
      .andExpect(jsonPath("$." + toProjectedProvisionDate(), equalTo("projected provision date")))
      .andExpect(jsonPath("$." + toResponsibilityStatement(), equalTo("responsibility statement")))
      .andExpect(jsonPath("$." + toVariantTitlePartName(), equalTo("Variant: partName")))
      .andExpect(jsonPath("$." + toVariantTitlePartNumber(), equalTo("Variant: partNumber")))
      .andExpect(jsonPath("$." + toVariantTitleMain(), equalTo("Variant: Laramie holds the range")))
      .andExpect(jsonPath("$." + toVariantTitleNote(), equalTo("Variant: noteLabel")))
      .andExpect(jsonPath("$." + toVariantTitleDate(), equalTo("Variant: date")))
      .andExpect(jsonPath("$." + toVariantTitleSubtitle(), equalTo("Variant: subtitle")))
      .andExpect(jsonPath("$." + toVariantTitleType(), equalTo("Variant: variantType")));
  }

  private void validateSampleBibframeEntity(Resource bibframe) {
    assertThat(bibframe.getType().getTypeUri()).isEqualTo(MONOGRAPH);
    assertThat(bibframe.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(bibframe.getDoc()).isNull();
    assertThat(bibframe.getResourceHash()).isNotNull();
    assertThat(bibframe.getOutgoingEdges()).hasSize(1);
    validateSampleInstance(bibframe.getOutgoingEdges().iterator().next(), bibframe);
  }

  private void validateSampleInstance(ResourceEdge instanceEdge, Resource bibframe) {
    assertThat(instanceEdge.getId()).isNotNull();
    assertThat(instanceEdge.getSource()).isEqualTo(bibframe);
    assertThat(instanceEdge.getPredicate().getLabel()).isEqualTo(INSTANCE);
    var instance = instanceEdge.getTarget();
    assertThat(instance.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(instance.getType().getTypeUri()).isEqualTo(INSTANCE);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(7);
    validateLiteral(instance, DIMENSIONS, "20 cm");
    validateLiteral(instance, EDITION_STATEMENT, "edition statement");
    validateLiteral(instance, RESPONSIBILITY_STATEMENT, "responsibility statement");
    validateLiteral(instance, COPYRIGHT_DATE, "copyright date");
    validateLiteral(instance, PROJECTED_PROVISION_DATE, "projected provision date");
    validateLiteral(instance, MEDIA, "unmediated");
    validateLiteral(instance, CARRIER, "carrier");
    assertThat(instance.getOutgoingEdges()).hasSize(3);

    var edgeIterator = instance.getOutgoingEdges().iterator();
    validateSampleInstanceTitle(edgeIterator.next(), instance);
    validateSampleParallelTitle(edgeIterator.next(), instance);
    validateSampleVariantTitle(edgeIterator.next(), instance);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateLiteral(Resource instance, String field, String value) {
    assertThat(instance.getDoc().get(field).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(field).get(0).asText()).isEqualTo(value);
  }

  private void validateSampleInstanceTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, INSTANCE_TITLE, "Instance: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(NON_SORT_NUM).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NON_SORT_NUM).get(0).asText()).isEqualTo("Instance: nonSortNum");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleParallelTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, PARALLEL_TITLE, "Parallel: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(6);
    assertThat(title.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE).get(0).asText()).isEqualTo("Parallel: date");
    assertThat(title.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE).get(0).asText()).isEqualTo("Parallel: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleVariantTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, VARIANT_TITLE, "Variant: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(7);
    assertThat(title.getDoc().get(DATE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE).get(0).asText()).isEqualTo("Variant: date");
    assertThat(title.getDoc().get(VARIANT_TYPE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(VARIANT_TYPE).get(0).asText()).isEqualTo("Variant: variantType");
    assertThat(title.getDoc().get(NOTE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NOTE).get(0).asText()).isEqualTo("Variant: noteLabel");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleTitleBase(ResourceEdge titleEdge, Resource instance, String type, String prefix) {
    assertThat(titleEdge.getId()).isNotNull();
    assertThat(titleEdge.getSource()).isEqualTo(instance);
    assertThat(titleEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_TITLE_PRED);
    var title = titleEdge.getTarget();
    assertThat(title.getLabel()).isEqualTo(title.getDoc().get(MAIN_TITLE).get(0).asText());
    assertThat(title.getType().getTypeUri()).isEqualTo(type);
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE).get(0).asText()).isEqualTo(prefix + "Laramie holds the range");
    assertThat(title.getDoc().get(SUBTITLE).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE).get(0).asText()).isEqualTo(prefix + "subtitle");
  }

  private String toCarrier() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(CARRIER));
  }

  private String toMedia() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(MEDIA));
  }

  private String toDimensions() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(DIMENSIONS));
  }

  private String toEditionStatement() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(EDITION_STATEMENT));
  }

  private String toResponsibilityStatement() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(RESPONSIBILITY_STATEMENT));
  }

  private String toCopyrightDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(COPYRIGHT_DATE));
  }

  private String toProjectedProvisionDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(PROJECTED_PROVISION_DATE));
  }

  private String toInstanceTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NAME));
  }

  private String toInstanceTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toInstanceTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toInstanceTitleNonSortNum() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(NON_SORT_NUM));
  }

  private String toInstanceTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED), path(INSTANCE_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toParallelTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NAME));
  }

  private String toParallelTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toParallelTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toParallelTitleDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(DATE));
  }

  private String toParallelTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toParallelTitleNote() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 1), path(PARALLEL_TITLE),
      arrayPath(NOTE));
  }

  private String toVariantTitlePartName() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NAME));
  }

  private String toVariantTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(PART_NUMBER));
  }

  private String toVariantTitleMain() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(MAIN_TITLE));
  }

  private String toVariantTitleDate() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(DATE));
  }

  private String toVariantTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(SUBTITLE));
  }

  private String toVariantTitleType() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(VARIANT_TYPE));
  }

  private String toVariantTitleNote() {
    return String.join(".", arrayPath(INSTANCE), arrayPath(INSTANCE_TITLE_PRED, 2), path(VARIANT_TITLE),
      arrayPath(NOTE));
  }

  private String toErrorType() {
    return String.join(".", arrayPath("errors"), path("type"));
  }

  private String toErrorCode() {
    return String.join(".", arrayPath("errors"), path("code"));
  }

  private String toErrorKey() {
    return String.join(".", arrayPath("errors"), arrayPath("parameters"), path("key"));
  }

  private String toErrorValue() {
    return String.join(".", arrayPath("errors"), arrayPath("parameters"), path("value"));
  }

  private String filterPath(String... paths) {
    return String.format("[?(@.%s)]", String.join(".", paths));
  }

  private String path(String path) {
    return String.format("['%s']", path);
  }

  private String arrayPath(String path, int index) {
    return String.format("['%s'][%d]", path, index);
  }

  private String arrayPath(String path) {
    return arrayPath(path, 0);
  }

}
