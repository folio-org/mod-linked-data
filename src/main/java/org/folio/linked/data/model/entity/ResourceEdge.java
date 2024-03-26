package org.folio.linked.data.model.entity;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resource_edges")
public class ResourceEdge {

  @EmbeddedId
  private ResourceEdgePk id;

  @ManyToOne(optional = false)
  @MapsId("sourceHash")
  @JoinColumn(name = "source_hash", nullable = false)
  private Resource source;

  @ManyToOne(optional = false)
  @MapsId("targetHash")
  @JoinColumn(name = "target_hash", nullable = false)
  private Resource target;

  @ManyToOne(optional = false)
  @MapsId("predicateHash")
  @JoinColumn(name = "predicate_hash", nullable = false)
  private PredicateEntity predicate;

  public ResourceEdge(@NonNull Resource source, @NonNull Resource target, @NonNull Predicate predicate) {
    this.source = source;
    this.target = target;
    this.predicate = new PredicateEntity(predicate);
  }

  public ResourceEdge(@NonNull ResourceEdge that) {
    if (nonNull(that.id)) {
      this.id = new ResourceEdgePk(that.id);
    }
    this.source = that.source;
    this.target = that.target;
    this.predicate = that.predicate;
  }

  public void computeId() {
    if (nonNull(source) && nonNull(target) && nonNull(predicate)) {
      id = new ResourceEdgePk(source.getResourceHash(), target.getResourceHash(), predicate.getHash());
    }
  }

  @Override
  public String toString() {
    return "ResourceEdge{"
      + "source=" + (nonNull(source) ? source.getTypes().iterator().next().getUri() : null)
      + ", target=" + (nonNull(target) ? target.getTypes().iterator().next().getUri() : null)
      + ", predicate=" + (nonNull(predicate) ? predicate.getUri() : null)
      + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceEdge that = (ResourceEdge) o;
    return Objects.equals(getSourceHash(), that.getSourceHash())
      && Objects.equals(getTargetHash(), that.getTargetHash())
      && Objects.equals(getPredicateHash(), that.getPredicateHash());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSourceHash(), getTargetHash(), getPredicateHash());
  }

  private Long getSourceHash() {
    return ofNullable(id)
      .map(ResourceEdgePk::getSourceHash)
      .or(() -> ofNullable(source).map(Resource::getResourceHash))
      .orElse(null);
  }

  private Long getTargetHash() {
    return ofNullable(id)
      .map(ResourceEdgePk::getTargetHash)
      .or(() -> ofNullable(target).map(Resource::getResourceHash))
      .orElse(null);
  }

  private Long getPredicateHash() {
    return ofNullable(id)
      .map(ResourceEdgePk::getPredicateHash)
      .or(() -> ofNullable(predicate).map(Predicate::getHash))
      .orElse(null);
  }
}
