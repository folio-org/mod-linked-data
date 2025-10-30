package org.folio.linked.data.embedding;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String EMBEDDING_API_URL = "http://localhost:11434/api/embeddings";

    public List<Double> generateEmbedding(String input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"model\":\"nomic-embed-text\",\"prompt\":\"%s\"}", input.replace("\"", "\\\""));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        var response = restTemplate.postForObject(EMBEDDING_API_URL, entity, Map.class);
        return response != null ? (List<Double>) response.get("embedding") : null;
    }
}

