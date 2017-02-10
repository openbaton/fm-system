/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.faultmanagement.catalogue;

import java.util.List;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mob on 27/06/16. */
@Entity
public class VduPmJobs {
  @Id private String id;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> pmJobsIds;

  public VduPmJobs() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getPmJobsIds() {
    return pmJobsIds;
  }

  public void setPmJobsIds(List<String> pmJobsIds) {
    this.pmJobsIds = pmJobsIds;
  }

  @Override
  public String toString() {
    return "VduPmJobs{" + "id='" + id + '\'' + ", pmJobsIds=" + pmJobsIds + '}';
  }
}
