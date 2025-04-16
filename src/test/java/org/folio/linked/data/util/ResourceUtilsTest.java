package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceUtilsTest {

  @Test
  void getFirstValue_shouldReturnEmptyString_ifGivenSupplierIsNull() {
    // given

    // when
    var result = ResourceUtils.getFirstValue(null);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getFirstValue_shouldReturnFirstValue() {
    // given
    var first = UUID.randomUUID().toString();
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = ResourceUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEqualTo(first);
  }

  @Test
  void getFirstValue_shouldReturnSecondValue_ifFirstValueIsEmpty() {
    // given
    var first = "";
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = ResourceUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEqualTo(second);
  }

  @Test
  void getFirstValue_shouldReturnEmptyString_ifAllValuesAreNull() {
    // given
    String first = null;
    String second = null;
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = ResourceUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void cleanDate_shouldCleanAnyUnexpectedSymbolAndReturnDateIfParsed() {
    // given
    var given = List.of(
      "2021-11-08T13:00:00.000+00:00",
      "2022-12-22T13:00:00",
      "2020-01-01",
      "[2010]",
      "sdvsdvindfinvdfnbv±!@#$%^&*()_=2000",
      "2023",
      "abc",
      "25",
      "200"
    );

    // when
    var result = given.stream().map(ResourceUtils::cleanDate).toList();

    // then
    assertThat(result).containsExactly(
      "2021-11-08T13:00:00.000+00:00",
      "2022-12-22T13:00:00",
      "2020-01-01",
      "2010",
      "2000",
      "2023",
      null,
      null,
      null
    );
  }

  @Test
  void extractInstancesFromWork_shouldReturnInstance_ifGivenResourceIsinstance() {
    // given
    var instance = new Resource().setId(1L).addTypes(INSTANCE);

    // when
    var result = ResourceUtils.extractInstancesFromWork(instance);

    // then
    assertThat(result).containsOnly(instance);
  }

  @Test
  void extractInstancesFromWork_shouldReturnEmptyList_ifGivenResourceIsNotInstanceOrWork() {
    // given
    var resource = new Resource().setId(1L).addTypes(JURISDICTION);
    var instance = new Resource().setId(2L).addTypes(INSTANCE);
    resource.addOutgoingEdge(new ResourceEdge(resource, instance, INSTANTIATES));
    resource.addIncomingEdge(new ResourceEdge(instance, resource, INSTANTIATES));

    // when
    var result = ResourceUtils.extractInstancesFromWork(resource);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void extractInstancesFromWork_shouldReturnInstancesListWithEdgesToWork_ifGivenResourceIsWork() {
    // given
    var work = new Resource().setId(0L).addTypes(WORK);
    var instance1 = new Resource().setId(1L).addTypes(INSTANCE);
    work.addIncomingEdge(new ResourceEdge(instance1, work, INSTANTIATES));
    var instance2 = new Resource().setId(2L).addTypes(INSTANCE);
    work.addIncomingEdge(new ResourceEdge(instance2, work, INSTANTIATES));

    // when
    var result = ResourceUtils.extractInstancesFromWork(work);

    // then
    assertThat(result).containsOnly(instance1, instance2);
    assertThat(instance1.getOutgoingEdges()).containsOnly(new ResourceEdge(instance1, work, INSTANTIATES));
    assertThat(instance2.getOutgoingEdges()).containsOnly(new ResourceEdge(instance2, work, INSTANTIATES));
  }

  @Test
  void test_isPreferred_when_preferred_flag_is_not_set() {
    // given
    Resource resource = new Resource();

    // then
    assertThat(ResourceUtils.isPreferred(resource)).isFalse();
  }

  @Test
  void test_isPreferred_when_preferred_flag_is_set_as_true() {
    // given
    Resource resource = new Resource();
    ResourceUtils.setPreferred(resource, true);

    // then
    assertThat(ResourceUtils.isPreferred(resource)).isTrue();
  }

  @Test
  void test_isPreferred_when_preferred_flag_is_set_as_false() {
    // given
    Resource resource = new Resource();
    ResourceUtils.setPreferred(resource, false);

    // then
    assertThat(ResourceUtils.isPreferred(resource)).isFalse();
  }

  @Test
  void copyWithoutPreferred_removesPreferredProperty_whenDocContainsPreferred() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.set("http://library.link/vocab/resourcePreferred", JsonNodeFactory.instance.arrayNode().add("true"));
    resource.setDoc(doc);

    // when
    var result = ResourceUtils.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("http://library.link/vocab/resourcePreferred")).isFalse();
  }

  @Test
  void copyWithoutPreferred_returnsNull_whenDocIsNull() {
    // given
    var resource = new Resource();
    resource.setDoc(null);

    // when
    var result = ResourceUtils.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNull();
  }

  @Test
  void copyWithoutPreferred_returnsUnchangedCopy_whenDocDoesNotContainPreferred() {
    // given
    var resource = new Resource();
    var doc = JsonNodeFactory.instance.objectNode();
    doc.put("otherProperty", "value");
    resource.setDoc(doc);

    // when
    var result = ResourceUtils.copyWithoutPreferred(resource);

    // then
    assertThat(result).isNotNull();
    assertThat(result.has("otherProperty")).isTrue();
    assertThat(result.get("otherProperty").asText()).isEqualTo("value");
  }
}
