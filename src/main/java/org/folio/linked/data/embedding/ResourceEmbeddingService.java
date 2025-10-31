package org.folio.linked.data.embedding;

import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NAME_ALTERNATIVE;
import static org.folio.ld.dictionary.PropertyDictionary.NUMERATION;
import static org.folio.ld.dictionary.PropertyDictionary.TITLES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceEmbeddingService {

  private final EmbeddingService embeddingService;

  public List<Double> embedResource(Resource resource) {
    if (resource.isOfType(PERSON)) {
      var name = String.join(", ", getPropertyValues(resource, NAME));
      var numeration = String.join(", ", getPropertyValues(resource, NUMERATION));
      var date = String.join(", ", getPropertyValues(resource, DATE));
      var titles = String.join(", ", getPropertyValues(resource, TITLES));
      var miscInfo = String.join(", ", getPropertyValues(resource, MISC_INFO));
      var nameAlternatives = String.join(", ", getPropertyValues(resource, NAME_ALTERNATIVE));

      StringBuilder paragraphBuilder = new StringBuilder();
      if (!numeration.isBlank() && !name.isBlank()) {
        paragraphBuilder.append("This is the ")
          .append(romanToOrdinal(numeration)).append(" ")
          .append(name).append(" (" + name + " " + numeration + ")");
        if (!titles.isBlank()) paragraphBuilder.append(", ").append(titles);
        paragraphBuilder.append(". The numeration '").append(numeration)
          .append("' means this is the ").append(romanToOrdinal(numeration))
          .append(" person with this name and title. ");
        paragraphBuilder.append("This person is NOT the same as any other person named ")
          .append(name).append(" with a different numeration, such as ")
          .append(name).append(" I, II, III, etc.");
      } else if (!name.isBlank()) {
        paragraphBuilder.append("This is a person with name ").append(name).append(".");
      }
      if (!date.isBlank()) paragraphBuilder.append(" Date of birth/death or activity: ").append(date).append(".");
      if (!miscInfo.isBlank()) paragraphBuilder.append(" Additional info: ").append(miscInfo).append(".");
      if (!nameAlternatives.isBlank()) paragraphBuilder.append(" Alternative names: ").append(nameAlternatives).append(".");

      var paragraph = paragraphBuilder.toString().trim();
      var embedding = embeddingService.generateEmbedding(paragraph);
      System.out.printf("""
        Paragraph: %s
        Vector: %s
        %n""", paragraph, embedding);
      return embedding;
    }
    return List.of();
  }

  // Helper to convert common Roman numerals to ordinal words
  private String romanToOrdinal(String roman) {
    return switch (roman.trim()) {
      case "I" -> "FIRST";
      case "II" -> "SECOND";
      case "III" -> "THIRD";
      case "IV" -> "FOURTH";
      case "V" -> "FIFTH";
      case "VI" -> "SIXTH";
      case "VII" -> "SEVENTH";
      case "VIII" -> "EIGHTH";
      case "IX" -> "ninth";
      case "X" -> "NINTH";
      default -> roman + "th";
    };
  }
}
