package org.folio.linked.data.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

  public static final String RESOURCE_TYPE = "Resource type [";
  public static final String PREDICATE = "Predicate [";
  public static final String RIGHT_SQUARE_BRACKET = "]";
  public static final String IS_NOT_SUPPORTED_FOR = "] is not supported for ";
  public static final String IS_NOT_SUPPORTED_FOR_PREDICATE = IS_NOT_SUPPORTED_FOR + PREDICATE;
  public static final String FOLIO_PROFILE = "folio";
  public static final String DTO_UNKNOWN_SUB_ELEMENT = " dto class deserialization error: Unknown sub-element ";
  public static final String AND = " and ";
  public static final String RESOURCE_WITH_GIVEN_ID = "Resource with given id [";
  public static final String RESOURCE_WITH_GIVEN_INVENTORY_ID = "Resource with given inventory id [";
  public static final String IS_NOT_FOUND = "] is not found";
  public static final String SEARCH_WORK_RESOURCE_NAME = "linked-data-work";
  public static final String SEARCH_AUTHORITY_RESOURCE_NAME = "linked-data-authority";
  public static final String TYPE = "type";
  public static final String RELATION_PREDICATE_PREFIX = "http://bibfra.me/vocab/relation/";
  public static final String PROFILE_NOT_FOUND = "Profile not found";
  public static final String MSG_UNKNOWN_TYPES =
    "Unknown type(s) [{}] of [{}] was ignored during Resource [resourceId = {}] conversion to Index message";

}
