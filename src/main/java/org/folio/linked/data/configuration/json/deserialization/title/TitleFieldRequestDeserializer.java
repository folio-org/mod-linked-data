package org.folio.linked.data.configuration.json.deserialization.title;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import java.util.Map;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.util.DtoDeserializer;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class TitleFieldRequestDeserializer extends ValueDeserializer<TitleFieldRequestTitleInner> {

  private static final Map<String, Class<? extends TitleFieldRequestTitleInner>> IDENDTITY_MAP = Map.of(
    TITLE.getUri(), PrimaryTitleField.class,
    PARALLEL_TITLE.getUri(), ParallelTitleField.class,
    VARIANT_TITLE.getUri(), VariantTitleField.class
  );
  private final DtoDeserializer<TitleFieldRequestTitleInner> dtoDeserializer;

  public TitleFieldRequestDeserializer() {
    dtoDeserializer = new DtoDeserializer<>(TitleFieldRequestTitleInner.class, IDENDTITY_MAP);
  }

  @Override
  public TitleFieldRequestTitleInner deserialize(JsonParser jp, DeserializationContext dc) {
    return dtoDeserializer.deserialize(jp, dc);
  }

}
