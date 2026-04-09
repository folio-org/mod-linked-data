package org.folio.linked.data.e2e.mappings.work.lightwork;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_EDITION;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_VERSION;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_WORK;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.test.MonographTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class LightWorkIT extends ITBase {

  private static final String RESOURCE_URL = "/linked-data/resource";
  private static final Long IS_PART_OF_ID = 200L;
  private static final Long OTHER_EDITION_ID = 201L;
  private static final Long OTHER_VERSION_ID = 202L;
  private static final Long RELATED_WORK_ID = 203L;
  private static final Long CREATOR_ID = 204L;

  @Test
  @SneakyThrows
  void getWork_withLightWorkEdges_shouldReturnAnalyticalEntryForEachRelation() {
    // given
    var work = buildWorkWithLightWorkEdges();
    resourceTestService.saveGraph(work);
    var getRequest = get(RESOURCE_URL + "/" + work.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var response = mockMvc.perform(getRequest);

    // then
    var analyticalEntryPath = "$.resource['http://bibfra.me/vocab/lite/Work']['_analyticalEntry']";
    response
      .andExpect(status().isOk())
      .andExpect(jsonPath(analyticalEntryPath, hasSize(4)))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['id']", containsInAnyOrder(
        IS_PART_OF_ID.toString(),
        OTHER_EDITION_ID.toString(),
        OTHER_VERSION_ID.toString(),
        RELATED_WORK_ID.toString()
      )))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['_relation']", containsInAnyOrder(
        IS_PART_OF.getUri(),
        OTHER_EDITION.getUri(),
        OTHER_VERSION.getUri(),
        RELATED_WORK.getUri()
      )))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['label']", containsInAnyOrder(
        labelFor(IS_PART_OF_ID) + ". ",
        labelFor(OTHER_EDITION_ID) + ". ",
        labelFor(OTHER_VERSION_ID) + ". ",
        labelFor(RELATED_WORK_ID) + ". "
      )));
  }

  @Test
  @SneakyThrows
  void getWork_withLightWorkEdgeAndCreator_shouldConstructLabelFromWorkAndCreator() {
    // given
    var work = MonographTestUtil.getWork("work", hashService);
    var creator = new Resource()
      .addTypes(LIGHT_RESOURCE)
      .setDoc(TEST_JSON_MAPPER.readTree("""
        {"%s": ["%s"]}""".formatted(LABEL.getValue(), "Creator Name")))
      .setLabel("Creator Name")
      .setIdAndRefreshEdges(CREATOR_ID);
    var lightWork = new Resource()
      .addTypes(LIGHT_RESOURCE, WORK)
      .setDoc(TEST_JSON_MAPPER.readTree("""
        {"%s": ["%s"]}""".formatted(LABEL.getValue(), labelFor(IS_PART_OF_ID))))
      .setLabel(labelFor(IS_PART_OF_ID))
      .setIdAndRefreshEdges(IS_PART_OF_ID);
    var creatorEdge = new ResourceEdge(lightWork, creator, CREATOR);
    lightWork.addOutgoingEdge(creatorEdge);
    creator.addIncomingEdge(creatorEdge);
    work.addOutgoingEdge(new ResourceEdge(work, lightWork, IS_PART_OF));
    resourceTestService.saveGraph(work);
    var getRequest = get(RESOURCE_URL + "/" + work.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var response = mockMvc.perform(getRequest);

    // then
    var analyticalEntryPath = "$.resource['http://bibfra.me/vocab/lite/Work']['_analyticalEntry']";
    response
      .andExpect(status().isOk())
      .andExpect(jsonPath(analyticalEntryPath, hasSize(1)))
      .andExpect(jsonPath(analyticalEntryPath + "[0]['label']")
        .value(labelFor(IS_PART_OF_ID) + ". " + "Creator Name"));
  }

  @Test
  @SneakyThrows
  void getWork_withLightWorkEdgesAndPartOfSeries_shouldReturnCorrectAnalyticalEntries() {
    // given
    var work = buildWorkWithLightWorkEdges();
    var series = new Resource().setLabel("Series").addTypes(WORK, SERIES, LIGHT_RESOURCE).setIdAndRefreshEdges(333L);
    work.addOutgoingEdge(new ResourceEdge(work, series, IS_PART_OF));
    resourceTestService.saveGraph(work);
    var getRequest = get(RESOURCE_URL + "/" + work.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var response = mockMvc.perform(getRequest);

    // then
    var analyticalEntryPath = "$.resource['http://bibfra.me/vocab/lite/Work']['_analyticalEntry']";
    response
      .andExpect(status().isOk())
      .andExpect(jsonPath(analyticalEntryPath, hasSize(4)))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['id']", containsInAnyOrder(
        IS_PART_OF_ID.toString(),
        OTHER_EDITION_ID.toString(),
        OTHER_VERSION_ID.toString(),
        RELATED_WORK_ID.toString()
      )))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['_relation']", containsInAnyOrder(
        IS_PART_OF.getUri(),
        OTHER_EDITION.getUri(),
        OTHER_VERSION.getUri(),
        RELATED_WORK.getUri()
      )))
      .andExpect(jsonPath(analyticalEntryPath + "[*]['label']", containsInAnyOrder(
        labelFor(IS_PART_OF_ID) + ". ",
        labelFor(OTHER_EDITION_ID) + ". ",
        labelFor(OTHER_VERSION_ID) + ". ",
        labelFor(RELATED_WORK_ID) + ". "
      )));
  }

  @SneakyThrows
  private Resource buildWorkWithLightWorkEdges() {
    var work = MonographTestUtil.getWork("work", hashService);
    addLightWorkEdge(work, IS_PART_OF_ID, IS_PART_OF);
    addLightWorkEdge(work, OTHER_EDITION_ID, OTHER_EDITION);
    addLightWorkEdge(work, OTHER_VERSION_ID, OTHER_VERSION);
    addLightWorkEdge(work, RELATED_WORK_ID, RELATED_WORK);
    return work;
  }

  @SneakyThrows
  private void addLightWorkEdge(Resource work, Long lightWorkId, PredicateDictionary predicate) {
    var doc = TEST_JSON_MAPPER.readTree("""
      {"%s": ["%s"]}""".formatted(LABEL.getValue(), labelFor(lightWorkId)));
    var lightWork = new Resource()
      .addTypes(LIGHT_RESOURCE, WORK)
      .setDoc(doc)
      .setLabel(labelFor(lightWorkId))
      .setIdAndRefreshEdges(lightWorkId);
    work.addOutgoingEdge(new ResourceEdge(work, lightWork, predicate));
  }

  private String labelFor(Long id) {
    return "light work label " + id;
  }
}

