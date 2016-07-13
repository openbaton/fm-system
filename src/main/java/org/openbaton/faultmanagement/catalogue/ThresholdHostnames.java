package org.openbaton.faultmanagement.catalogue;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by mob on 27/06/16.
 */
@Entity
public class ThresholdHostnames {
  @Id private String id;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> hostnames;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public ThresholdHostnames(List<String> hostnames) {
    this.hostnames = hostnames;
  }

  public ThresholdHostnames() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getHostnames() {
    return hostnames;
  }

  public void setHostnames(List<String> hostnames) {
    this.hostnames = hostnames;
  }

  @Override
  public String toString() {
    return "ThresholdHostnames{" + "id='" + id + '\'' + ", hostnames=" + hostnames + '}';
  }
}
