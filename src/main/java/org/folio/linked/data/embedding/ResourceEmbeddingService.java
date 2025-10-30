package org.folio.linked.data.embedding;

import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
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
      var date = String.join(", ", getPropertyValues(resource, DATE));
      var toEmbed = "This is a Person with name %s and date of birth/death: %s".formatted(name, date);
      var embedding = embeddingService.generateEmbedding(toEmbed);
      System.out.printf("""
        Text: %s
        Vector: %s
        %n""", toEmbed, embedding);
      return embedding;
    }
    return List.of();
  }
}

