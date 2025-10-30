package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "resource_embeddings")
public class ResourceEmbedding {
    @Id
    @Column(name = "resource_hash", nullable = false)
    private Long resourceHash;

    // The 'vector' type is supported by pgvector. Use Object for now, or float[] if you have a converter.
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private List<Double> embedding; // For full support, use float[] and a converter, or a custom type handler.

    public Long getResourceHash() {
        return resourceHash;
    }

    public void setResourceHash(Long resourceHash) {
        this.resourceHash = resourceHash;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceEmbedding)) return false;
        ResourceEmbedding that = (ResourceEmbedding) o;
        return Objects.equals(resourceHash, that.resourceHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceHash);
    }
}



