package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.model.ErrorCode.VALIDATION_ERROR;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.bibframe2SampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframe2Sample;
import static org.folio.linked.data.test.TestUtil.getResource;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Bibframe2Constants.AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.CARRIER2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CARRIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.COPYRIGHT_DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DIMENSIONS_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DISTRIBUTION;
import static org.folio.linked.data.util.Bibframe2Constants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ELECTRONIC_LOCATOR_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.FAMILY;
import static org.folio.linked.data.util.Bibframe2Constants.FAMILY_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_EAN_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_ISBN_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LCCN_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LOCAL_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_OTHER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IMM_ACQUISITION;
import static org.folio.linked.data.util.Bibframe2Constants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IMM_ACQUISITION_URI;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_2;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ISSUANCE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ISSUANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ITEM_URL;
import static org.folio.linked.data.util.Bibframe2Constants.JURISDICTION;
import static org.folio.linked.data.util.Bibframe2Constants.JURISDICTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.LABEL_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MANUFACTURE;
import static org.folio.linked.data.util.Bibframe2Constants.MANUFACTURE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MEDIA2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MEDIA_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MEETING;
import static org.folio.linked.data.util.Bibframe2Constants.MEETING_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MONOGRAPH_2;
import static org.folio.linked.data.util.Bibframe2Constants.NON_SORT_NUM_URL;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_2;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_TYPE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_TYPE_URI;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ORGANIZATION;
import static org.folio.linked.data.util.Bibframe2Constants.ORGANIZATION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PARALLEL_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NAME_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NUMBER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PERSON;
import static org.folio.linked.data.util.Bibframe2Constants.PERSON_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE2;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PRODUCTION;
import static org.folio.linked.data.util.Bibframe2Constants.PRODUCTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROFILE;
import static org.folio.linked.data.util.Bibframe2Constants.PROJECTED_PROVISION_DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_ID;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_LABEL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_URI;
import static org.folio.linked.data.util.Bibframe2Constants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.QUALIFIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.RESPONSIBILITY_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SAME_AS_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SUBTITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SUPP_CONTENT;
import static org.folio.linked.data.util.Bibframe2Constants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SUPP_CONTENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.URL;
import static org.folio.linked.data.util.Bibframe2Constants.URL_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TYPE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.WORK_URL;
import static org.folio.linked.data.util.BibframeConstants.ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
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
public class Bibframe2ControllerIT {

  public static final String BIBFRAME_URL = "/bibframe2";
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
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframe2Sample());

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = validateSampleBibframeResponse(resultActions)
      .andReturn().getResponse().getContentAsString();

    var bibframeResponse = objectMapper.readValue(response, Bibframe2Response.class);
    var persistedOptional = resourceRepo.findById(bibframeResponse.getId());
    assertThat(persistedOptional).isPresent();
    var monograph = persistedOptional.get();
    validateSampleMonographEntity(monograph);
    checkKafkaMessageSent(monograph, null);
  }

  @Test
  void createTwoMonographInstancesWithSharedResources_shouldSaveBothCorrectly() throws Exception {
    // given
    var requestBuilder1 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframe2Sample());
    var resultActions1 = mockMvc.perform(requestBuilder1);
    var response1 = resultActions1.andReturn().getResponse().getContentAsString();
    var bibframeResponse1 = objectMapper.readValue(response1, Bibframe2Response.class);
    var persistedOptional1 = resourceRepo.findById(bibframeResponse1.getId());
    assertThat(persistedOptional1).isPresent();
    var requestBuilder2 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframe2Sample().replace("volume", "length"));
    var expectedDifference = "length\"}]}],\"id\":1574458046,\"profile\":\"lc:profile:bf2:Monograph\"}";

    // when
    var response2 = mockMvc.perform(requestBuilder2).andReturn().getResponse().getContentAsString();

    // then
    assertThat(StringUtils.difference(response1, response2)).isEqualTo(expectedDifference);
  }

  @Test
  void createMonographInstanceWithNotCorrectStructure_shouldReturnValidationError() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getResource("samples/bibframe-wrong-field.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);


    // then
    resultActions.andExpect(status().is(UNPROCESSABLE_ENTITY.value()))
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors", notNullValue()))
      .andExpect(jsonPath("$." + toErrorType(), equalTo(ValidationException.class.getSimpleName())))
      .andExpect(jsonPath("$." + toErrorCode(), equalTo(VALIDATION_ERROR.getValue())))
      .andExpect(jsonPath("$." + toErrorKey(), equalTo(ELECTRONIC_LOCATOR_2_PRED)))
      .andExpect(jsonPath("$." + toErrorValue(), equalTo("{}")));
  }

  @Test
  void createMonographInstanceBibframeWithNoLabelInProperty_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getResource("samples/bibframe-property-no-label.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);


    // then
    resultActions.andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()));
  }

  @Test
  void createMonographInstanceBibframeWithMultipleIdenticalResources_shouldSaveEntityCorrectly() throws Exception {
    // given
    var requestBuilder = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getResource("samples/bibframe-multiple-identical-resources.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);


    // then
    resultActions.andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()));
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = resourceRepo.save(monographTestService.createSampleMonograph_2());
    var requestBuilder = get(BIBFRAME_URL + "/" + existed.getResourceHash())
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
    var requestBuilder = get(BIBFRAME_URL + "/" + notExistedId)
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
      resourceRepo.save(bibframe2SampleResource(1L, monographTestService.getMonograph2Type())),
      resourceRepo.save(bibframe2SampleResource(2L, monographTestService.getMonograph2Type())),
      resourceRepo.save(bibframe2SampleResource(3L, monographTestService.getMonograph2Type()))
    ).stream().sorted(comparing(Resource::getResourceHash)).toList();
    var requestBuilder = get(BIBFRAME_URL)
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
    var existed = resourceRepo.save(monographTestService.createSampleMonograph_2());
    assertThat(resourceRepo.findById(existed.getResourceHash())).isPresent();
    assertThat(resourceRepo.count()).isEqualTo(47);
    assertThat(resourceEdgeRepository.count()).isEqualTo(46);
    var requestBuilder = delete(BIBFRAME_URL + "/" + existed.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    // when
    mockMvc.perform(requestBuilder);

    // then
    assertThat(resourceRepo.findById(existed.getResourceHash())).isNotPresent();
    assertThat(resourceRepo.count()).isEqualTo(46);
    assertThat(resourceEdgeRepository.findById(existed.getOutgoingEdges().iterator().next().getId())).isNotPresent();
    assertThat(resourceEdgeRepository.count()).isEqualTo(45);
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
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("$." + path(WORK_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(ITEM_URL)).doesNotExist())
      .andExpect(jsonPath("$." + path(INSTANCE_URL), notNullValue()))
      .andExpect(jsonPath("$." + toApplicableInstitutionId(), equalTo("applicableInstitutionId")))
      .andExpect(jsonPath("$." + toApplicableInstitutionUri(), equalTo(APPLICABLE_INSTITUTION_URL)))
      .andExpect(jsonPath("$." + toApplicableInstitutionLabel(), equalTo("some applicableInstitution")))
      .andExpect(jsonPath("$." + toCarrierId(), equalTo("carrierId")))
      .andExpect(jsonPath("$." + toCarrierUri(), equalTo(CARRIER_URL)))
      .andExpect(jsonPath("$." + toCarrierLabel(), equalTo("volume")))
      .andExpect(jsonPath("$." + toContributionAgentProperty(PERSON_URL, PROPERTY_URI)).value(
        "http://id.loc.gov/authorities/names/n87914389"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(PERSON_URL, PROPERTY_LABEL)).value(
        "Spearman, Frank H. (Frank Hamilton), 1859-1937"))
      .andExpect(jsonPath("$." + toContributionRoleProperty(PERSON_URL, PROPERTY_ID)).value(ROLE))
      .andExpect(jsonPath("$." + toContributionRoleProperty(PERSON_URL, PROPERTY_URI)).value(ROLE_URL))
      .andExpect(jsonPath("$." + toContributionRoleProperty(PERSON_URL, PROPERTY_LABEL)).value("Author"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(FAMILY_URL, PROPERTY_URI)).value(
        "http://id.loc.gov/authorities/subjects/sh85061960"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(FAMILY_URL, PROPERTY_LABEL)).value(
        "Hopwood family"))
      .andExpect(jsonPath("$." + toContributionRoleProperty(FAMILY_URL, PROPERTY_ID)).value(ROLE))
      .andExpect(jsonPath("$." + toContributionRoleProperty(FAMILY_URL, PROPERTY_URI)).value(ROLE_URL))
      .andExpect(jsonPath("$." + toContributionRoleProperty(FAMILY_URL, PROPERTY_LABEL)).value("Contributor"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(ORGANIZATION_URL, PROPERTY_URI)).value(
        "http://id.loc.gov/authorities/names/n81050810"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(ORGANIZATION_URL, PROPERTY_LABEL)).value(
        "Charles Scribner's Sons"))
      .andExpect(jsonPath("$." + toContributionRoleProperty(ORGANIZATION_URL, PROPERTY_ID)).value(ROLE))
      .andExpect(jsonPath("$." + toContributionRoleProperty(ORGANIZATION_URL, PROPERTY_URI)).value(ROLE_URL))
      .andExpect(jsonPath("$." + toContributionRoleProperty(ORGANIZATION_URL, PROPERTY_LABEL)).value("Provider"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(JURISDICTION_URL, PROPERTY_URI)).value(
        "http://id.loc.gov/authorities/names/n87837615"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(JURISDICTION_URL, PROPERTY_LABEL)).value(
        "United States. Congress. House. Library"))
      .andExpect(jsonPath("$." + toContributionRoleProperty(JURISDICTION_URL, PROPERTY_ID)).value(ROLE))
      .andExpect(jsonPath("$." + toContributionRoleProperty(JURISDICTION_URL, PROPERTY_URI)).value(ROLE_URL))
      .andExpect(jsonPath("$." + toContributionRoleProperty(JURISDICTION_URL, PROPERTY_LABEL)).value("Contractor"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(MEETING_URL, PROPERTY_URI)).value(
        "http://id.loc.gov/authorities/names/nr93009771"))
      .andExpect(jsonPath("$." + toContributionAgentProperty(MEETING_URL, PROPERTY_LABEL)).value(
        "Workshop on Electronic Texts (1992 : Library of Congress)"))
      .andExpect(jsonPath("$." + toContributionRoleProperty(MEETING_URL, PROPERTY_ID)).value(ROLE))
      .andExpect(jsonPath("$." + toContributionRoleProperty(MEETING_URL, PROPERTY_URI)).value(ROLE_URL))
      .andExpect(jsonPath("$." + toContributionRoleProperty(MEETING_URL, PROPERTY_LABEL)).value("Other"))
      .andExpect(jsonPath("$." + toDimensions(), equalTo("20 cm")))
      .andExpect(jsonPath("$." + toResponsibilityStatement(), equalTo("responsibility statement")))
      .andExpect(jsonPath("$." + toEditionStatement(), equalTo("edition statement")))
      .andExpect(jsonPath("$." + toCopyrightDate(), equalTo("copyright date")))
      .andExpect(jsonPath("$." + toProjectedProvisionDate(), equalTo("projected provision date")))
      .andExpect(jsonPath("$." + toElectronicLocatorNoteLabel(), equalTo("electronicLocatorNoteLabel")))
      .andExpect(jsonPath("$." + toElectronicLocatorValue(), equalTo("electronicLocatorValue")))
      .andExpect(jsonPath("$." + toExtentNoteLabel(), equalTo("extent note label")))
      .andExpect(jsonPath("$." + toExtentLabel(), equalTo("extent label")))
      .andExpect(jsonPath("$." + toExtentAppliesToLabel(), equalTo("extent appliesTo label")))
      .andExpect(jsonPath("$." + toId(), notNullValue()))
      .andExpect(jsonPath("$." + toIdentifiedByEanValue(), equalTo("12345670")))
      .andExpect(jsonPath("$." + toIdentifiedByEanQualifier(), equalTo("07654321")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbnValue(), equalTo("12345671")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbnQualifier(), equalTo("17654321")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbnStatusId(), equalTo("isbnStatusId")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbnStatusLabel(), equalTo("isbnStatusLabel")))
      .andExpect(jsonPath("$." + toIdentifiedByIsbnStatusUri(), equalTo("isbnStatusUri")))
      .andExpect(jsonPath("$." + toIdentifiedByLccnValue(), equalTo("12345672")))
      .andExpect(jsonPath("$." + toIdentifiedByLccnStatusId(), equalTo("lccnStatusId")))
      .andExpect(jsonPath("$." + toIdentifiedByLccnStatusLabel(), equalTo("lccnStatusLabel")))
      .andExpect(jsonPath("$." + toIdentifiedByLccnStatusUri(), equalTo("lccnStatusUri")))
      .andExpect(jsonPath("$." + toIdentifiedByLocalValue(), equalTo("12345673")))
      .andExpect(jsonPath("$." + toIdentifiedByLocalAssignerId(), equalTo("assignerId")))
      .andExpect(jsonPath("$." + toIdentifiedByLocalAssignerLabel(), equalTo("assignerLabel")))
      .andExpect(jsonPath("$." + toIdentifiedByLocalAssignerUri(), equalTo("assignerUri")))
      .andExpect(jsonPath("$." + toIdentifiedByOtherValue(), equalTo("12345674")))
      .andExpect(jsonPath("$." + toIdentifiedByOtherQualifier(), equalTo("47654321")))
      .andExpect(jsonPath("$." + toImmediateAcquisitionLabel(), equalTo("some immediateAcquisition")))
      .andExpect(jsonPath("$." + toInstanceTitlePartName(), equalTo("Instance: partName")))
      .andExpect(jsonPath("$." + toInstanceTitlePartNumber(), equalTo("Instance: partNumber")))
      .andExpect(jsonPath("$." + toInstanceTitleMain(), equalTo("Instance: Laramie holds the range")))
      .andExpect(jsonPath("$." + toInstanceTitleNonSortNum(), equalTo("Instance: nonSortNum")))
      .andExpect(jsonPath("$." + toInstanceTitleSubtitle(), equalTo("Instance: subtitle")))
      .andExpect(jsonPath("$." + toIssuanceId(), equalTo("issuanceId")))
      .andExpect(jsonPath("$." + toIssuanceLabel(), equalTo("single unit")))
      .andExpect(jsonPath("$." + toIssuanceUri(), equalTo(ISSUANCE_URL)))
      .andExpect(jsonPath("$." + toMediaId(), equalTo("mediaId")))
      .andExpect(jsonPath("$." + toMediaLabel(), equalTo("unmediated")))
      .andExpect(jsonPath("$." + toMediaUri(), equalTo(MEDIA_URL)))
      .andExpect(jsonPath("$." + toNoteLabel(), equalTo("note label")))
      .andExpect(jsonPath("$." + toNoteTypeId(), equalTo("noteTypeId")))
      .andExpect(jsonPath("$." + toNoteTypeLabel(), equalTo("Accompanying material")))
      .andExpect(jsonPath("$." + toNoteTypeUri(), equalTo("http://id.loc.gov/vocabulary/mnotetype/accmat")))
      .andExpect(jsonPath("$." + toDistributionSimpleAgent(), equalTo("Distribution: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toDistributionSimpleDate(), equalTo("Distribution: 1921")))
      .andExpect(jsonPath("$." + toDistributionSimplePlace(), equalTo("Distribution: New York")))
      .andExpect(jsonPath("$." + toDistributionPlaceId(), equalTo(PLACE2)))
      .andExpect(jsonPath("$." + toDistributionPlaceLabel(), equalTo("Distribution: New York (State)")))
      .andExpect(jsonPath("$." + toDistributionPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toDistributionDate(), equalTo("Distribution: 1921")))
      .andExpect(jsonPath("$." + toManufactureSimpleAgent(), equalTo("Manufacture: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toManufactureSimpleDate(), equalTo("Manufacture: 1921")))
      .andExpect(jsonPath("$." + toManufactureSimplePlace(), equalTo("Manufacture: New York")))
      .andExpect(jsonPath("$." + toManufacturePlaceId(), equalTo(PLACE2)))
      .andExpect(jsonPath("$." + toManufacturePlaceLabel(), equalTo("Manufacture: New York (State)")))
      .andExpect(jsonPath("$." + toManufacturePlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toManufactureDate(), equalTo("Manufacture: 1921")))
      .andExpect(jsonPath("$." + toParallelTitlePartName(), equalTo("Parallel: partName")))
      .andExpect(jsonPath("$." + toParallelTitlePartNumber(), equalTo("Parallel: partNumber")))
      .andExpect(jsonPath("$." + toParallelTitleMain(), equalTo("Parallel: Laramie holds the range")))
      .andExpect(jsonPath("$." + toParallelTitleDate(), equalTo("Parallel: date")))
      .andExpect(jsonPath("$." + toParallelTitleSubtitle(), equalTo("Parallel: subtitle")))
      .andExpect(jsonPath("$." + toParallelTitleNoteLabel(), equalTo("Parallel: noteLabel")))
      .andExpect(jsonPath("$." + toProductionSimpleAgent(), equalTo("Production: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toProductionSimpleDate(), equalTo("Production: 1921")))
      .andExpect(jsonPath("$." + toProductionSimplePlace(), equalTo("Production: New York")))
      .andExpect(jsonPath("$." + toProductionPlaceId(), equalTo(PLACE2)))
      .andExpect(jsonPath("$." + toProductionPlaceLabel(), equalTo("Production: New York (State)")))
      .andExpect(jsonPath("$." + toProductionPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toProductionDate(), equalTo("Production: 1921")))
      .andExpect(jsonPath("$." + toProfile(), equalTo(MONOGRAPH_2)))
      .andExpect(jsonPath("$." + toPublicationSimpleAgent(), equalTo("Publication: Charles Scribner's Sons")))
      .andExpect(jsonPath("$." + toPublicationSimpleDate(), equalTo("Publication: 1921")))
      .andExpect(jsonPath("$." + toPublicationSimplePlace(), equalTo("Publication: New York")))
      .andExpect(jsonPath("$." + toPublicationPlaceId(), equalTo(PLACE2)))
      .andExpect(jsonPath("$." + toPublicationPlaceLabel(), equalTo("Publication: New York (State)")))
      .andExpect(jsonPath("$." + toPublicationPlaceUri(), equalTo(PLACE_URL)))
      .andExpect(jsonPath("$." + toPublicationDate(), equalTo("Publication: 1921")))
      .andExpect(jsonPath("$." + toSupplementaryContentLabel(), equalTo("supplementaryContentLabel")))
      .andExpect(jsonPath("$." + toSupplementaryContentValue(), equalTo("supplementaryContentValue")))
      .andExpect(jsonPath("$." + toVariantTitlePartName(), equalTo("Variant: partName")))
      .andExpect(jsonPath("$." + toVariantTitlePartNumber(), equalTo("Variant: partNumber")))
      .andExpect(jsonPath("$." + toVariantTitleMain(), equalTo("Variant: Laramie holds the range")))
      .andExpect(jsonPath("$." + toVariantTitleDate(), equalTo("Variant: date")))
      .andExpect(jsonPath("$." + toVariantTitleSubtitle(), equalTo("Variant: subtitle")))
      .andExpect(jsonPath("$." + toVariantTitleType(), equalTo("Variant: variantType")))
      .andExpect(jsonPath("$." + toVariantTitleNoteLabel(), equalTo("Variant: noteLabel")));
  }

  private void validateSampleMonographEntity(Resource monograph) {
    assertThat(monograph.getType().getSimpleLabel()).isEqualTo(MONOGRAPH_2);
    assertThat(monograph.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(monograph.getDoc()).isNull();
    assertThat(monograph.getResourceHash()).isNotNull();
    assertThat(monograph.getOutgoingEdges()).hasSize(1);
    validateSampleInstance(monograph.getOutgoingEdges().iterator().next(), monograph);
  }

  private void validateSampleInstance(ResourceEdge instanceEdge, Resource monograph) {
    assertThat(instanceEdge.getId()).isNotNull();
    assertThat(instanceEdge.getSource()).isEqualTo(monograph);
    assertThat(instanceEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_URL);
    var instance = instanceEdge.getTarget();
    assertThat(instance.getLabel()).isEqualTo("Instance: Laramie holds the range");
    assertThat(instance.getType().getSimpleLabel()).isEqualTo(INSTANCE_2);
    assertThat(instance.getResourceHash()).isNotNull();
    assertThat(instance.getDoc().size()).isEqualTo(5);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(DIMENSIONS_URL).get(0).asText()).isEqualTo("20 cm");
    assertThat(instance.getDoc().get(EDITION_STATEMENT_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(EDITION_STATEMENT_URL).get(0).asText()).isEqualTo("edition statement");
    assertThat(instance.getDoc().get(RESPONSIBILITY_STATEMENT_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(RESPONSIBILITY_STATEMENT_URL).get(0).asText()).isEqualTo(
      "responsibility statement");
    assertThat(instance.getDoc().get(COPYRIGHT_DATE_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(COPYRIGHT_DATE_URL).get(0).asText()).isEqualTo("copyright date");
    assertThat(instance.getDoc().get(PROJECTED_PROVISION_DATE_URL).size()).isEqualTo(1);
    assertThat(instance.getDoc().get(PROJECTED_PROVISION_DATE_URL).get(0).asText()).isEqualTo(
      "projected provision date");
    assertThat(instance.getOutgoingEdges()).hasSize(25);

    var edgeIterator = instance.getOutgoingEdges().iterator();
    validateSampleInstanceTitle(edgeIterator.next(), instance);
    validateSampleParallelTitle(edgeIterator.next(), instance);
    validateSampleVariantTitle(edgeIterator.next(), instance);
    validateSampleProvision(edgeIterator.next(), instance, DISTRIBUTION_URL, DISTRIBUTION, "Distribution: ");
    validateSampleProvision(edgeIterator.next(), instance, MANUFACTURE_URL, MANUFACTURE, "Manufacture: ");
    validateSampleProvision(edgeIterator.next(), instance, PRODUCTION_URL, PRODUCTION, "Production: ");
    validateSampleProvision(edgeIterator.next(), instance, PUBLICATION_URL, PUBLICATION, "Publication: ");
    validateSampleContribution(edgeIterator.next(), instance, PERSON_URL, PERSON,
      "Spearman, Frank H. (Frank Hamilton), 1859-1937", "http://id.loc.gov/authorities/names/n87914389", "Author");
    validateSampleContribution(edgeIterator.next(), instance, FAMILY_URL, FAMILY,
      "Hopwood family", "http://id.loc.gov/authorities/subjects/sh85061960", "Contributor");
    validateSampleContribution(edgeIterator.next(), instance, ORGANIZATION_URL, ORGANIZATION,
      "Charles Scribner's Sons", "http://id.loc.gov/authorities/names/n81050810", "Provider");
    validateSampleContribution(edgeIterator.next(), instance, JURISDICTION_URL, JURISDICTION,
      "United States. Congress. House. Library", "http://id.loc.gov/authorities/names/n87837615", "Contractor");
    validateSampleContribution(edgeIterator.next(), instance, MEETING_URL, MEETING,
      "Workshop on Electronic Texts (1992 : Library of Congress)", "http://id.loc.gov/authorities/names/nr93009771",
      "Other");
    validateSampleIdentifiedByEan(edgeIterator.next(), instance);
    validateSampleIdentifiedByIsbn(edgeIterator.next(), instance);
    validateSampleIdentifiedByLccn(edgeIterator.next(), instance);
    validateSampleIdentifiedByLocal(edgeIterator.next(), instance);
    validateSampleIdentifiedByOther(edgeIterator.next(), instance);
    validateNote(edgeIterator.next(), instance, "note label", "noteTypeId", "Accompanying material",
      "http://id.loc.gov/vocabulary/mnotetype/accmat");
    validateSampleSupplementaryContent(edgeIterator.next(), instance);
    validateSampleImmediateAcquisition(edgeIterator.next(), instance);
    validateSampleExtent(edgeIterator.next(), instance);
    validateSampleElectronicLocator(edgeIterator.next(), instance);
    validateSampleProperty(edgeIterator.next(), instance, ISSUANCE_PRED, ISSUANCE_URL, "issuanceId", "single unit",
      ISSUANCE_URL);
    validateSampleProperty(edgeIterator.next(), instance, MEDIA2_PRED, MEDIA_URL, "mediaId", "unmediated",
      MEDIA_URL);
    validateSampleProperty(edgeIterator.next(), instance, CARRIER2_PRED, CARRIER_URL, "carrierId", "volume",
      CARRIER_URL);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleIdentifiedByEan(ResourceEdge identifiedByEdge, Resource instance) {
    validateSampleIdentifiedByBase(identifiedByEdge, instance, IDENTIFIERS_EAN_URL, IDENTIFIERS_EAN, "12345670");
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getDoc().size()).isEqualTo(2);
    assertThat(identifiedBy.getDoc().get(QUALIFIER_URL).size()).isEqualTo(1);
    assertThat(identifiedBy.getDoc().get(QUALIFIER_URL).get(0).asText()).isEqualTo("07654321");
    assertThat(identifiedBy.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleIdentifiedByIsbn(ResourceEdge identifiedByEdge, Resource instance) {
    validateSampleIdentifiedByBase(identifiedByEdge, instance, IDENTIFIERS_ISBN_URL, IDENTIFIERS_ISBN, "12345671");
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getDoc().size()).isEqualTo(2);
    assertThat(identifiedBy.getDoc().get(QUALIFIER_URL).size()).isEqualTo(1);
    assertThat(identifiedBy.getDoc().get(QUALIFIER_URL).get(0).asText()).isEqualTo("17654321");
    var edgeIterator = identifiedBy.getOutgoingEdges().iterator();
    validateSampleProperty(edgeIterator.next(), identifiedBy, STATUS2_PRED, STATUS_URL, "isbnStatusId",
      "isbnStatusLabel", "isbnStatusUri");
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleIdentifiedByLccn(ResourceEdge identifiedByEdge, Resource instance) {
    validateSampleIdentifiedByBase(identifiedByEdge, instance, IDENTIFIERS_LCCN_URL, IDENTIFIERS_LCCN, "12345672");
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getDoc().size()).isEqualTo(1);
    var edgeIterator = identifiedBy.getOutgoingEdges().iterator();
    validateSampleProperty(edgeIterator.next(), identifiedBy, STATUS2_PRED, STATUS_URL, "lccnStatusId",
      "lccnStatusLabel", "lccnStatusUri");
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleIdentifiedByLocal(ResourceEdge identifiedByEdge, Resource instance) {
    validateSampleIdentifiedByBase(identifiedByEdge, instance, IDENTIFIERS_LOCAL_URL, IDENTIFIERS_LOCAL, "12345673");
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getDoc().size()).isEqualTo(1);
    var edgeIterator = identifiedBy.getOutgoingEdges().iterator();
    validateSampleProperty(edgeIterator.next(), identifiedBy, ASSIGNER_PRED, ASSIGNER_URL, "assignerId",
      "assignerLabel", "assignerUri");
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleIdentifiedByOther(ResourceEdge identifiedByEdge, Resource instance) {
    validateSampleIdentifiedByBase(identifiedByEdge, instance, IDENTIFIERS_OTHER_URL, IDENTIFIERS_OTHER, "12345674");
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getDoc().size()).isEqualTo(2);
    assertThat(identifiedBy.getDoc().get(QUALIFIER_URL).get(0).asText()).isEqualTo("47654321");
    assertThat(identifiedBy.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleIdentifiedByBase(ResourceEdge identifiedByEdge, Resource instance, String url,
                                              String type, String value) {
    assertThat(identifiedByEdge.getId()).isNotNull();
    assertThat(identifiedByEdge.getSource()).isEqualTo(instance);
    assertThat(identifiedByEdge.getPredicate().getLabel()).isEqualTo(IDENTIFIED_BY_PRED);
    var identifiedBy = identifiedByEdge.getTarget();
    assertThat(identifiedBy.getLabel()).isEqualTo(url);
    assertThat(identifiedBy.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(identifiedBy.getResourceHash()).isNotNull();
    assertThat(identifiedBy.getDoc().get(VALUE_PRED).size()).isEqualTo(1);
    assertThat(identifiedBy.getDoc().get(VALUE_PRED).get(0).asText()).isEqualTo(value);
  }

  private void validateSampleInstanceTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, INSTANCE_TITLE_URL, INSTANCE_TITLE_2, "Instance: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(NON_SORT_NUM_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(NON_SORT_NUM_URL).get(0).asText()).isEqualTo("Instance: nonSortNum");
    assertThat(title.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleParallelTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, PARALLEL_TITLE_URL, PARALLEL_TITLE_2, "Parallel: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(5);
    assertThat(title.getDoc().get(DATE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE_URL).get(0).asText()).isEqualTo("Parallel: date");
    assertThat(title.getOutgoingEdges()).hasSize(1);
    var edgeIterator = title.getOutgoingEdges().iterator();
    validateNote(edgeIterator.next(), title, "Parallel: noteLabel", null, null, null);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleVariantTitle(ResourceEdge titleEdge, Resource instance) {
    validateSampleTitleBase(titleEdge, instance, VARIANT_TITLE_URL, VARIANT_TITLE_2, "Variant: ");
    var title = titleEdge.getTarget();
    assertThat(title.getDoc().size()).isEqualTo(6);
    assertThat(title.getDoc().get(DATE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(DATE_URL).get(0).asText()).isEqualTo("Variant: date");
    assertThat(title.getDoc().get(VARIANT_TYPE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(VARIANT_TYPE_URL).get(0).asText()).isEqualTo("Variant: variantType");
    assertThat(title.getOutgoingEdges()).hasSize(1);
    var edgeIterator = title.getOutgoingEdges().iterator();
    validateNote(edgeIterator.next(), title, "Variant: noteLabel", null, null, null);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleTitleBase(ResourceEdge titleEdge, Resource instance, String label, String type,
                                       String prefix) {
    assertThat(titleEdge.getId()).isNotNull();
    assertThat(titleEdge.getSource()).isEqualTo(instance);
    assertThat(titleEdge.getPredicate().getLabel()).isEqualTo(INSTANCE_TITLE_2_PRED);
    var title = titleEdge.getTarget();
    assertThat(title.getLabel()).isEqualTo(label);
    assertThat(title.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(title.getResourceHash()).isNotNull();
    assertThat(title.getDoc().get(PART_NAME_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NAME_URL).get(0).asText()).isEqualTo(prefix + "partName");
    assertThat(title.getDoc().get(PART_NUMBER_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(PART_NUMBER_URL).get(0).asText()).isEqualTo(prefix + "partNumber");
    assertThat(title.getDoc().get(MAIN_TITLE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(MAIN_TITLE_URL).get(0).asText()).isEqualTo(prefix + "Laramie holds the range");
    assertThat(title.getDoc().get(SUBTITLE_URL).size()).isEqualTo(1);
    assertThat(title.getDoc().get(SUBTITLE_URL).get(0).asText()).isEqualTo(prefix + "subtitle");
  }

  private void validateSampleProperty(ResourceEdge propertyEdge, Resource source, String pred, String type,
                                      String propertyId, String propertyLabel, String propertyUri) {
    assertThat(propertyEdge.getId()).isNotNull();
    assertThat(propertyEdge.getSource()).isEqualTo(source);
    assertThat(propertyEdge.getPredicate().getLabel()).isEqualTo(pred);
    var property = propertyEdge.getTarget();
    assertThat(property.getLabel()).isEqualTo(propertyLabel);
    assertThat(property.getType().getTypeUri()).isEqualTo(type);
    assertThat(property.getResourceHash()).isNotNull();
    assertThat(property.getDoc().get(PROPERTY_ID).asText()).isEqualTo(propertyId);
    assertThat(property.getDoc().get(PROPERTY_URI).asText()).isEqualTo(propertyUri);
    assertThat(property.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo(propertyLabel);
    assertThat(property.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleExtent(ResourceEdge extentEdge, Resource instance) {
    assertThat(extentEdge.getId()).isNotNull();
    assertThat(extentEdge.getSource()).isEqualTo(instance);
    assertThat(extentEdge.getPredicate().getLabel()).isEqualTo(EXTENT_PRED);
    var extent = extentEdge.getTarget();
    assertThat(extent.getLabel()).isEqualTo(EXTENT_URL);
    assertThat(extent.getType().getSimpleLabel()).isEqualTo(EXTENT);
    assertThat(extent.getResourceHash()).isNotNull();
    assertThat(extent.getDoc().size()).isEqualTo(1);
    assertThat(extent.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(extent.getDoc().get(LABEL_PRED).get(0).asText())
      .isEqualTo("extent label");
    assertThat(extent.getOutgoingEdges()).hasSize(2);
    var edgeIterator = extent.getOutgoingEdges().iterator();
    validateAppliesTo(edgeIterator.next(), extent);
    validateNote(edgeIterator.next(), extent, "extent note label", null, null, null);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateNote(ResourceEdge noteEdge, Resource source, String noteLabel, String noteTypeId,
                            String noteTypeLabel, String noteTypeUri) {
    assertThat(noteEdge.getId()).isNotNull();
    assertThat(noteEdge.getSource()).isEqualTo(source);
    assertThat(noteEdge.getPredicate().getLabel()).isEqualTo(NOTE_PRED);
    var note = noteEdge.getTarget();
    assertThat(note.getLabel()).isEqualTo(NOTE_URL);
    assertThat(note.getType().getSimpleLabel()).isEqualTo(NOTE_2);
    assertThat(note.getResourceHash()).isNotNull();
    assertThat(note.getDoc().size()).isEqualTo(1);
    assertThat(note.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(note.getDoc().get(LABEL_PRED).get(0).asText()).isEqualTo(noteLabel);
    if (nonNull(noteTypeId) || nonNull(noteTypeLabel) || nonNull(noteTypeUri)) {
      var edgeIterator = note.getOutgoingEdges().iterator();
      validateSampleProperty(edgeIterator.next(), note, NOTE_TYPE_PRED, NOTE_TYPE_URI, noteTypeId, noteTypeLabel,
        noteTypeUri);
      assertThat(edgeIterator.hasNext()).isFalse();
    } else {
      assertThat(note.getOutgoingEdges()).isEmpty();
    }
  }

  private void validateAppliesTo(ResourceEdge appliesToEdge, Resource extent) {
    assertThat(appliesToEdge.getId()).isNotNull();
    assertThat(appliesToEdge.getSource()).isEqualTo(extent);
    assertThat(appliesToEdge.getPredicate().getLabel()).isEqualTo(APPLIES_TO_PRED);
    var appliesTo = appliesToEdge.getTarget();
    assertThat(appliesTo.getLabel()).isEqualTo(APPLIES_TO_URL);
    assertThat(appliesTo.getType().getSimpleLabel()).isEqualTo(APPLIES_TO);
    assertThat(appliesTo.getResourceHash()).isNotNull();
    assertThat(appliesTo.getDoc().size()).isEqualTo(1);
    assertThat(appliesTo.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(appliesTo.getDoc().get(LABEL_PRED).get(0).asText()).isEqualTo("extent appliesTo label");
    assertThat(appliesTo.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleSupplementaryContent(ResourceEdge edge, Resource instance) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(instance);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(SUPP_CONTENT_PRED);
    var suppContent = edge.getTarget();
    assertThat(suppContent.getLabel()).isEqualTo(SUPP_CONTENT_URL);
    assertThat(suppContent.getType().getSimpleLabel()).isEqualTo(SUPP_CONTENT);
    assertThat(suppContent.getResourceHash()).isNotNull();
    assertThat(suppContent.getDoc().size()).isEqualTo(2);
    assertThat(suppContent.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(suppContent.getDoc().get(LABEL_PRED).get(0).asText()).isEqualTo("supplementaryContentLabel");
    assertThat(suppContent.getDoc().get(VALUE_PRED).size()).isEqualTo(1);
    assertThat(suppContent.getDoc().get(VALUE_PRED).get(0).asText()).isEqualTo("supplementaryContentValue");
    assertThat(suppContent.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleImmediateAcquisition(ResourceEdge edge, Resource instance) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(instance);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(IMM_ACQUISITION_PRED);
    var immediateAcquisition = edge.getTarget();
    assertThat(immediateAcquisition.getLabel()).isEqualTo(IMM_ACQUISITION_URI);
    assertThat(immediateAcquisition.getType().getSimpleLabel()).isEqualTo(IMM_ACQUISITION);
    assertThat(immediateAcquisition.getResourceHash()).isNotNull();
    assertThat(immediateAcquisition.getDoc().size()).isEqualTo(1);
    assertThat(immediateAcquisition.getDoc().get(LABEL_PRED).size()).isEqualTo(1);
    assertThat(immediateAcquisition.getDoc().get(LABEL_PRED).get(0).asText())
      .isEqualTo("some immediateAcquisition");
    assertThat(immediateAcquisition.getOutgoingEdges()).hasSize(1);
    var edgeIterator = immediateAcquisition.getOutgoingEdges().iterator();
    validateSampleProperty(edgeIterator.next(), immediateAcquisition, APPLICABLE_INSTITUTION_PRED,
      APPLICABLE_INSTITUTION_URL, "applicableInstitutionId", "some applicableInstitution", APPLICABLE_INSTITUTION_URL);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleContribution(ResourceEdge contributionEdge, Resource instance, String agenTypeUrl,
                                          String agentTypeLabel, String agentSameAsLabel, String agentSameAsUri,
                                          String roleLabel) {
    assertThat(contributionEdge.getId()).isNotNull();
    assertThat(contributionEdge.getSource()).isEqualTo(instance);
    assertThat(contributionEdge.getPredicate().getLabel()).isEqualTo(CONTRIBUTION_PRED);
    var contribution = contributionEdge.getTarget();
    assertThat(contribution.getLabel()).isEqualTo(CONTRIBUTION_URL);
    assertThat(contribution.getType().getTypeUri()).isEqualTo(CONTRIBUTION_URL);
    assertThat(contribution.getResourceHash()).isNotNull();
    assertThat(contribution.getDoc()).isNull();
    assertThat(contribution.getOutgoingEdges()).hasSize(2);
    var edgeIterator = contribution.getOutgoingEdges().iterator();
    validateSampleContributionAgent(edgeIterator.next(), contribution, agenTypeUrl, agentTypeLabel, agentSameAsLabel,
      agentSameAsUri);
    validateSampleContributionRole(edgeIterator.next(), contribution, roleLabel);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private void validateSampleContributionRole(ResourceEdge contributionRoleEdge, Resource contribution,
                                              String roleLabel) {
    assertThat(contributionRoleEdge.getId()).isNotNull();
    assertThat(contributionRoleEdge.getSource()).isEqualTo(contribution);
    assertThat(contributionRoleEdge.getPredicate().getLabel()).isEqualTo(ROLE_PRED);
    var contributionRole = contributionRoleEdge.getTarget();
    assertThat(contributionRole.getLabel()).isEqualTo(roleLabel);
    assertThat(contributionRole.getType().getTypeUri()).isEqualTo(ROLE_URL);
    assertThat(contributionRole.getResourceHash()).isNotNull();
    assertThat(contributionRole.getDoc().size()).isEqualTo(3);
    assertThat(contributionRole.getDoc().get(PROPERTY_URI).asText()).isEqualTo(ROLE_URL);
    assertThat(contributionRole.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo(roleLabel);
    assertThat(contributionRole.getDoc().get(PROPERTY_ID).asText()).isEqualTo(ROLE);
    assertThat(contributionRole.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleContributionAgent(ResourceEdge contributionAgentEdge,
                                               Resource contribution,
                                               String agenTypeUrl,
                                               String agenTypeLabel,
                                               String agentSameAsLabel,
                                               String agentSameAsUri) {
    assertThat(contributionAgentEdge.getId()).isNotNull();
    assertThat(contributionAgentEdge.getSource()).isEqualTo(contribution);
    assertThat(contributionAgentEdge.getPredicate().getLabel()).isEqualTo(AGENT_PRED);
    var contributionAgent = contributionAgentEdge.getTarget();
    assertThat(contributionAgent.getLabel()).isEqualTo(agenTypeUrl);
    assertThat(contributionAgent.getType().getSimpleLabel()).isEqualTo(agenTypeLabel);
    assertThat(contributionAgent.getResourceHash()).isNotNull();
    assertThat(contributionAgent.getDoc().size()).isEqualTo(1);
    assertThat(contributionAgent.getDoc().get(SAME_AS_PRED).get(0).get(PROPERTY_LABEL).asText())
      .isEqualTo(agentSameAsLabel);
    assertThat(contributionAgent.getDoc().get(SAME_AS_PRED).get(0).get(PROPERTY_URI).asText())
      .isEqualTo(agentSameAsUri);
    assertThat(contributionAgent.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleProvision(ResourceEdge provisionEdge, Resource instance, String label, String type,
                                       String prefix) {
    assertThat(provisionEdge.getId()).isNotNull();
    assertThat(provisionEdge.getSource()).isEqualTo(instance);
    assertThat(provisionEdge.getPredicate().getLabel()).isEqualTo(PROVISION_ACTIVITY_PRED);
    var provision = provisionEdge.getTarget();
    assertThat(provision.getLabel()).isEqualTo(label);
    assertThat(provision.getType().getSimpleLabel()).isEqualTo(type);
    assertThat(provision.getResourceHash()).isNotNull();
    assertThat(provision.getDoc().size()).isEqualTo(4);
    assertThat(provision.getDoc().get(SIMPLE_DATE_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_DATE_PRED).get(0).asText()).isEqualTo(prefix + "1921");
    assertThat(provision.getDoc().get(SIMPLE_AGENT_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_AGENT_PRED).get(0).asText()).isEqualTo(prefix + "Charles Scribner's Sons");
    assertThat(provision.getDoc().get(SIMPLE_PLACE_PRED).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(SIMPLE_PLACE_PRED).get(0).asText()).isEqualTo(prefix + "New York");
    assertThat(provision.getDoc().get(DATE_URL).size()).isEqualTo(1);
    assertThat(provision.getDoc().get(DATE_URL).get(0).asText()).isEqualTo(prefix + "1921");
    assertThat(provision.getOutgoingEdges()).hasSize(1);
    validateSamplePublicationPlace(provision.getOutgoingEdges().iterator().next(), provision, prefix);
  }

  private void validateSamplePublicationPlace(ResourceEdge publicationPlaceEdge, Resource publication, String prefix) {
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getId()).isNotNull();
    assertThat(publicationPlaceEdge.getSource()).isEqualTo(publication);
    assertThat(publicationPlaceEdge.getPredicate().getLabel()).isEqualTo(PLACE2_PRED);
    var publicationPlace = publicationPlaceEdge.getTarget();
    assertThat(publicationPlace.getLabel()).isEqualTo(prefix + "New York (State)");
    assertThat(publicationPlace.getType().getSimpleLabel()).isEqualTo(PLACE_COMPONENTS);
    assertThat(publicationPlace.getResourceHash()).isNotNull();
    assertThat(publicationPlace.getDoc().size()).isEqualTo(3);
    assertThat(publicationPlace.getDoc().get(PROPERTY_URI).asText()).isEqualTo(PLACE_URL);
    assertThat(publicationPlace.getDoc().get(PROPERTY_LABEL).asText()).isEqualTo(prefix + "New York (State)");
    assertThat(publicationPlace.getDoc().get(PROPERTY_ID).asText()).isEqualTo(PLACE2);
    assertThat(publicationPlace.getOutgoingEdges()).isEmpty();
  }

  private void validateSampleElectronicLocator(ResourceEdge edge, Resource instance) {
    assertThat(edge.getId()).isNotNull();
    assertThat(edge.getSource()).isEqualTo(instance);
    assertThat(edge.getPredicate().getLabel()).isEqualTo(ELECTRONIC_LOCATOR_2_PRED);
    var locator = edge.getTarget();
    assertThat(locator.getLabel()).isEqualTo(URL_URL);
    assertThat(locator.getType().getSimpleLabel()).isEqualTo(URL);
    assertThat(locator.getResourceHash()).isNotNull();
    assertThat(locator.getDoc().size()).isEqualTo(1);
    assertThat(locator.getDoc().get(VALUE_PRED).size()).isEqualTo(1);
    assertThat(locator.getDoc().get(VALUE_PRED).get(0).asText())
      .isEqualTo("electronicLocatorValue");
    assertThat(locator.getOutgoingEdges()).hasSize(1);
    var edgeIterator = locator.getOutgoingEdges().iterator();
    validateNote(edgeIterator.next(), locator, "electronicLocatorNoteLabel", null, null, null);
    assertThat(edgeIterator.hasNext()).isFalse();
  }

  private String toCarrierLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER2_PRED), path(PROPERTY_LABEL));
  }

  private String toCarrierId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER2_PRED), path(PROPERTY_ID));
  }

  private String toCarrierUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(CARRIER2_PRED), path(PROPERTY_URI));
  }

  private String toSupplementaryContentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(SUPP_CONTENT_PRED), path(SUPP_CONTENT_URL),
      arrayPath(LABEL_PRED));
  }

  private String toSupplementaryContentValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(SUPP_CONTENT_PRED), path(SUPP_CONTENT_URL),
      arrayPath(VALUE_PRED));
  }

  private String toMediaId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA2_PRED), path(PROPERTY_ID));
  }

  private String toMediaLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA2_PRED), path(PROPERTY_LABEL));
  }

  private String toMediaUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(MEDIA2_PRED), path(PROPERTY_URI));
  }

  private String toNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(LABEL_PRED));
  }

  private String toNoteTypeId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(
        NOTE_TYPE_PRED),
      path(PROPERTY_ID));
  }

  private String toNoteTypeLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(
        NOTE_TYPE_PRED),
      path(PROPERTY_LABEL));
  }

  private String toNoteTypeUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(
        NOTE_TYPE_PRED),
      path(PROPERTY_URI));
  }

  private String toImmediateAcquisitionLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IMM_ACQUISITION_PRED),
      path(IMM_ACQUISITION_URI), arrayPath(LABEL_PRED));
  }

  private String toApplicableInstitutionId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IMM_ACQUISITION_PRED),
      path(IMM_ACQUISITION_URI), arrayPath(APPLICABLE_INSTITUTION_PRED), path(PROPERTY_ID));
  }

  private String toApplicableInstitutionUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IMM_ACQUISITION_PRED),
      path(IMM_ACQUISITION_URI), arrayPath(APPLICABLE_INSTITUTION_PRED), path(PROPERTY_URI));
  }

  private String toApplicableInstitutionLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IMM_ACQUISITION_PRED),
      path(IMM_ACQUISITION_URI), arrayPath(APPLICABLE_INSTITUTION_PRED), path(PROPERTY_LABEL));
  }

  private String toIssuanceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_ID));
  }

  private String toIssuanceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_LABEL));
  }

  private String toIssuanceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ISSUANCE_PRED), path(PROPERTY_URI));
  }

  private String toDimensions() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(DIMENSIONS_URL));
  }

  private String toEditionStatement() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EDITION_STATEMENT_URL));
  }

  private String toResponsibilityStatement() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(RESPONSIBILITY_STATEMENT_URL));
  }

  private String toCopyrightDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(COPYRIGHT_DATE_URL));
  }

  private String toProjectedProvisionDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROJECTED_PROVISION_DATE_URL));
  }

  private String toExtentNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EXTENT_PRED),
      path(EXTENT_URL), arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(LABEL_PRED));
  }

  private String toExtentLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EXTENT_PRED),
      path(EXTENT_URL), arrayPath(LABEL_PRED));
  }

  private String toExtentAppliesToLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(EXTENT_PRED),
      path(EXTENT_URL), arrayPath(APPLIES_TO_PRED), path(APPLIES_TO_URL), arrayPath(LABEL_PRED));
  }

  private String toContributionRoleProperty(String agentUrl, String property) {
    return String.join(".", toContributionByAgent(agentUrl), arrayPath(ROLE_PRED), path(property));
  }

  private String toContributionAgentProperty(String agentUrl, String property) {
    return String.join(".", toContributionByAgent(agentUrl), arrayPath(AGENT_PRED), path(agentUrl),
      arrayPath(SAME_AS_PRED), path(property));
  }

  private String toDistributionDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(DATE_PRED));
  }

  private String toDistributionPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_URI));
  }

  private String toDistributionPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_LABEL));
  }

  private String toDistributionPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_ID));
  }

  private String toDistributionSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toDistributionSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toDistributionSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED),
      path(DISTRIBUTION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toManufactureDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(DATE_PRED));
  }

  private String toManufacturePlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE2_PRED), path(PROPERTY_URI));
  }

  private String toManufacturePlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE2_PRED), path(PROPERTY_LABEL));
  }

  private String toManufacturePlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(PLACE2_PRED), path(PROPERTY_ID));
  }

  private String toManufactureSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toManufactureSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toManufactureSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 1),
      path(MANUFACTURE_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toProductionDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(DATE_PRED));
  }

  private String toProductionPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_URI));
  }

  private String toProductionPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_LABEL));
  }

  private String toProductionPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_ID));
  }

  private String toProductionSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toProductionSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toProductionSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 2),
      path(PRODUCTION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toPublicationDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(DATE_PRED));
  }

  private String toPublicationPlaceUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_URI));
  }

  private String toPublicationPlaceLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_LABEL));
  }

  private String toPublicationPlaceId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(PLACE2_PRED), path(PROPERTY_ID));
  }

  private String toPublicationSimplePlace() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_PLACE_PRED));
  }

  private String toPublicationSimpleAgent() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_AGENT_PRED));
  }

  private String toPublicationSimpleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(PROVISION_ACTIVITY_PRED, 3),
      path(PUBLICATION_URL), arrayPath(SIMPLE_DATE_PRED));
  }

  private String toInstanceTitlePartName() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(PART_NAME_URL));
  }

  private String toInstanceTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(PART_NUMBER_URL));
  }

  private String toInstanceTitleMain() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toInstanceTitleNonSortNum() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(NON_SORT_NUM_URL));
  }

  private String toInstanceTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED), path(INSTANCE_TITLE_URL),
      arrayPath(SUBTITLE_URL));
  }

  private String toParallelTitlePartName() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(PART_NAME_URL));
  }

  private String toParallelTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(PART_NUMBER_URL));
  }

  private String toParallelTitleMain() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toParallelTitleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(DATE_URL));
  }

  private String toParallelTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(SUBTITLE_URL));
  }

  private String toParallelTitleNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 1), path(PARALLEL_TITLE_URL),
      arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(LABEL_PRED));
  }

  private String toVariantTitlePartName() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(PART_NAME_URL));
  }

  private String toVariantTitlePartNumber() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(PART_NUMBER_URL));
  }

  private String toVariantTitleMain() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(MAIN_TITLE_PRED));
  }

  private String toVariantTitleDate() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(DATE_URL));
  }

  private String toVariantTitleSubtitle() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(SUBTITLE_URL));
  }

  private String toVariantTitleType() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(VARIANT_TYPE_URL));
  }

  private String toVariantTitleNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(INSTANCE_TITLE_2_PRED, 2), path(VARIANT_TITLE_URL),
      arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(LABEL_PRED));
  }

  private String toIdentifiedByEanValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED), path(IDENTIFIERS_EAN_URL),
      arrayPath(VALUE_PRED));
  }

  private String toIdentifiedByEanQualifier() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED), path(IDENTIFIERS_EAN_URL),
      arrayPath(QUALIFIER_URL));
  }

  private String toIdentifiedByIsbnValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(VALUE_PRED));
  }

  private String toIdentifiedByIsbnQualifier() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(QUALIFIER_URL));
  }

  private String toIdentifiedByIsbnStatusId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_ID));
  }

  private String toIdentifiedByIsbnStatusUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_URI));
  }

  private String toIdentifiedByIsbnStatusLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 1), path(IDENTIFIERS_ISBN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_LABEL));
  }

  private String toIdentifiedByLccnValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 2), path(IDENTIFIERS_LCCN_URL),
      arrayPath(VALUE_PRED));
  }

  private String toIdentifiedByLccnStatusId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 2), path(IDENTIFIERS_LCCN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_ID));
  }

  private String toIdentifiedByLccnStatusUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 2), path(IDENTIFIERS_LCCN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_URI));
  }

  private String toIdentifiedByLccnStatusLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 2), path(IDENTIFIERS_LCCN_URL),
      arrayPath(STATUS2_PRED), path(PROPERTY_LABEL));
  }

  private String toIdentifiedByLocalValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 3), path(IDENTIFIERS_LOCAL_URL),
      arrayPath(VALUE_PRED));
  }

  private String toIdentifiedByLocalAssignerId() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 3), path(IDENTIFIERS_LOCAL_URL),
      arrayPath(ASSIGNER_PRED), path(PROPERTY_ID));
  }

  private String toIdentifiedByLocalAssignerLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 3), path(IDENTIFIERS_LOCAL_URL),
      arrayPath(ASSIGNER_PRED), path(PROPERTY_LABEL));
  }

  private String toIdentifiedByLocalAssignerUri() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 3), path(IDENTIFIERS_LOCAL_URL),
      arrayPath(ASSIGNER_PRED), path(PROPERTY_URI));
  }

  private String toIdentifiedByOtherValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 4), path(IDENTIFIERS_OTHER_URL),
      arrayPath(VALUE_PRED));
  }

  private String toIdentifiedByOtherQualifier() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(IDENTIFIED_BY_PRED, 4), path(IDENTIFIERS_OTHER_URL),
      arrayPath(QUALIFIER_URL));
  }

  private String toElectronicLocatorNoteLabel() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ELECTRONIC_LOCATOR_2_PRED), path(URL_URL),
      arrayPath(NOTE_PRED), path(NOTE_URL), arrayPath(LABEL_PRED));
  }

  private String toElectronicLocatorValue() {
    return String.join(".", arrayPath(INSTANCE_URL), arrayPath(ELECTRONIC_LOCATOR_2_PRED), path(URL_URL),
      arrayPath(VALUE_PRED));
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

  private String toId() {
    return path(ID);
  }

  private String toProfile() {
    return path(PROFILE);
  }

  private String toContributionByAgent(String agentTypeUrl) {
    return String.join(".", arrayPath(INSTANCE_URL), path(CONTRIBUTION_PRED) + filterPath(
        path(CONTRIBUTION_URL), arrayPath(AGENT_PRED), path(agentTypeUrl)),
      path(CONTRIBUTION_URL));
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
