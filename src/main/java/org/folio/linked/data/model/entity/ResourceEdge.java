package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.DETACH;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.model.entity.pk.ResourceEdgePk;
import org.springframework.data.domain.Persistable;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resource_edges")
@SuppressWarnings("javaarchitecture:S7027")
public class ResourceEdge implements Persistable<ResourceEdgePk>  {

  @EmbeddedId
  private ResourceEdgePk id;

  @MapsId("sourceHash")
  @ManyToOne(cascade = DETACH, optional = false)
  @JoinColumn(name = "source_hash", nullable = false)
  private Resource source;

  @MapsId("targetHash")
  @ManyToOne(cascade = DETACH, optional = false)
  @JoinColumn(name = "target_hash", nullable = false)
  private Resource target;

  @MapsId("predicateHash")
  @ManyToOne(cascade = DETACH, optional = false)
  @JoinColumn(name = "predicate_hash", nullable = false)
  private PredicateEntity predicate;

  @Transient
  private boolean managed;

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
      id = new ResourceEdgePk(source.getId(), target.getId(), predicate.getHash());
    }
  }

  @Override
  public boolean isNew() {
    return !managed && isNull(id);
  }

  @PostLoad
  @PrePersist
  void markManaged() {
    this.managed = true;
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
      .or(() -> ofNullable(source).map(Resource::getId))
      .orElse(null);
  }

  private Long getTargetHash() {
    return ofNullable(id)
      .map(ResourceEdgePk::getTargetHash)
      .or(() -> ofNullable(target).map(Resource::getId))
      .orElse(null);
  }

  private Long getPredicateHash() {
    return ofNullable(id)
      .map(ResourceEdgePk::getPredicateHash)
      .or(() -> ofNullable(predicate).map(Predicate::getHash))
      .orElse(null);
  }
}
