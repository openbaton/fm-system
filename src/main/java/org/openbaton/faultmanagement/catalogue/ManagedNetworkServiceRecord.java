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

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.*;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class ManagedNetworkServiceRecord {
  @Id private String id;
  private String nsrId;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> thresholdIdFmPolicyMap;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, ThresholdHostnames> hostnames;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> unSubscriptionIds;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, VduPmJobs> vduIdPmJobIdMap;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> vnfTriggerId;

  public ManagedNetworkServiceRecord() {
    thresholdIdFmPolicyMap = new HashMap<>();
    hostnames = new HashMap<>();
    vduIdPmJobIdMap = new HashMap<>();
    vnfTriggerId = new HashSet<>();
  }

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

  public String getNsrId() {
    return nsrId;
  }

  public void setNsrId(String nsrId) {
    this.nsrId = nsrId;
  }

  public Map<String, String> getThresholdIdFmPolicyMap() {
    return thresholdIdFmPolicyMap;
  }

  public void setThresholdIdFmPolicyMap(Map<String, String> thresholdIdFmPolicyMap) {
    this.thresholdIdFmPolicyMap = thresholdIdFmPolicyMap;
  }

  public Set<String> getUnSubscriptionIds() {
    return unSubscriptionIds;
  }

  public void setUnSubscriptionIds(Set<String> unSubscriptionIds) {
    this.unSubscriptionIds = unSubscriptionIds;
  }

  public Map<String, VduPmJobs> getVduIdPmJobIdMap() {
    return vduIdPmJobIdMap;
  }

  public void setVduIdPmJobIdMap(Map<String, VduPmJobs> vduIdPmJobIdMap) {
    this.vduIdPmJobIdMap = vduIdPmJobIdMap;
  }

  public Set<String> getVnfTriggerId() {
    return vnfTriggerId;
  }

  public void setVnfTriggerId(Set<String> vnfTriggerId) {
    this.vnfTriggerId = vnfTriggerId;
  }

  public Map<String, ThresholdHostnames> getHostnames() {
    return hostnames;
  }

  public void setHostnames(Map<String, ThresholdHostnames> hostnames) {
    this.hostnames = hostnames;
  }

  @Override
  public String toString() {
    return "ManagedNetworkServiceRecord{"
        + "id='"
        + id
        + '\''
        + ", nsrId='"
        + nsrId
        + '\''
        + ", thresholdIdFmPolicyMap="
        + thresholdIdFmPolicyMap
        + ", hostnames="
        + hostnames
        + ", unSubscriptionIds="
        + unSubscriptionIds
        + ", vduIdPmJobIdMap="
        + vduIdPmJobIdMap
        + ", vnfTriggerId="
        + vnfTriggerId
        + '}';
  }
}
