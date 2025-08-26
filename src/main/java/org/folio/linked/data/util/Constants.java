package org.folio.linked.data.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

  public static final String RESOURCE_TYPE = "Resource type [";
  public static final String PREDICATE = "Predicate [";
  public static final String RIGHT_SQUARE_BRACKET = "]";
  public static final String IS_NOT_SUPPORTED_FOR = "] is not supported for ";
  public static final String IS_NOT_SUPPORTED_FOR_PREDICATE = IS_NOT_SUPPORTED_FOR + PREDICATE;
  public static final String STANDALONE_PROFILE = "standalone";
  public static final String AND = " and ";
  public static final String RESOURCE_WITH_GIVEN_ID = "Resource with given id [";
  public static final String IS_NOT_FOUND = "] is not found";
  public static final String SEARCH_WORK_RESOURCE_NAME = "linked-data-work";
  public static final String RELATION_PREDICATE_PREFIX = "http://bibfra.me/vocab/relation/";
  public static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [resourceId = {}] conversion to Index message";
  public static final String MSG_NOT_FOUND_IN = "{} with {}: {} was not found in {}";
  public static final String LINKED_DATA_STORAGE = "Linked Data storage";
  public static final String EMPTY_CACHE_MSG = "Emptying {} cache";

  @UtilityClass
  public static class Cache {
    public static final String SPEC_RULES = "spec-rules";
    public static final String SETTINGS_ENTRIES = "settings-entries";
    public static final String MODULE_STATE = "module-state";
    public static final String PROFILES = "profiles";
  }
}
