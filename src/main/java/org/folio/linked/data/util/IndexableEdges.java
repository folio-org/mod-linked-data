package org.folio.linked.data.util;

import static java.util.Collections.unmodifiableSet;
import static java.util.EnumSet.of;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;

import java.util.Set;
import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;

@UtilityClass
public final class IndexableEdges {

  // Re-exported single predicates — use these instead of importing PredicateDictionary directly.
  // The purpose is a connection between SearchMessageMappers and IndexableResourceQueryBuilder.
  public static final PredicateDictionary INDEXABLE_CLASSIFICATION = CLASSIFICATION;
  public static final PredicateDictionary INDEXABLE_CONTRIBUTOR = CONTRIBUTOR;
  public static final PredicateDictionary INDEXABLE_CREATOR = CREATOR;
  public static final PredicateDictionary INDEXABLE_INSTANTIATES = INSTANTIATES;
  public static final PredicateDictionary INDEXABLE_LANGUAGE = LANGUAGE;
  public static final PredicateDictionary INDEXABLE_MAP = MAP;
  public static final PredicateDictionary INDEXABLE_PE_PUBLICATION = PE_PUBLICATION;
  public static final PredicateDictionary INDEXABLE_SUBJECT = SUBJECT;
  public static final PredicateDictionary INDEXABLE_TITLE = TITLE;

  public static final Set<PredicateDictionary> OUTGOING = unmodifiableSet(of(
    INDEXABLE_CLASSIFICATION,
    INDEXABLE_CONTRIBUTOR,
    INDEXABLE_CREATOR,
    INDEXABLE_LANGUAGE,
    INDEXABLE_MAP,
    INDEXABLE_PE_PUBLICATION,
    INDEXABLE_SUBJECT,
    INDEXABLE_TITLE
  ));

  public static final Set<PredicateDictionary> INCOMING = unmodifiableSet(of(
    INDEXABLE_INSTANTIATES
  ));
}
