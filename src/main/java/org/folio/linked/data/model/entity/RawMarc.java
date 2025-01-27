package org.folio.linked.data.model.entity;

import static lombok.AccessLevel.PROTECTED;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Data
@NoArgsConstructor(access = PROTECTED)
@Accessors(chain = true)
@Table(name = "raw_marcs")
@EqualsAndHashCode(of = "id")
public class RawMarc {

  @Id
  @Column(name = "resource_hash")
  private Long id;

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private String content;

  @OneToOne
  @MapsId
  @JoinColumn(name = "resource_hash")
  @ToString.Exclude
  private Resource resource;

  public RawMarc(Resource resource) {
    this.resource = resource;
    this.id = resource.getId();
  }
}
