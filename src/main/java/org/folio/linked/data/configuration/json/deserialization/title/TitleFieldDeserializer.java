package org.folio.linked.data.configuration.json.deserialization.title;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleField;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.util.DtoDeserializer;

public class TitleFieldDeserializer extends JsonDeserializer<TitleField> {

  private static final Map<String, Class<? extends TitleField>> IDENDTITY_MAP = Map.of(
    TITLE.getUri(), PrimaryTitleField.class,
    PARALLEL_TITLE.getUri(), ParallelTitleField.class,
    VARIANT_TITLE.getUri(), VariantTitleField.class
  );
  private final DtoDeserializer<TitleField> dtoDeserializer =
    new DtoDeserializer<>(IDENDTITY_MAP, TitleField.class);

  @Override
  public TitleField deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }

}
