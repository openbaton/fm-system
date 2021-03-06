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

package org.openbaton.faultmanagement.core.pm.interfaces;

import java.io.FileNotFoundException;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.core.ham.exceptions.HighAvailabilityException;
import org.openbaton.faultmanagement.core.pm.exceptions.FaultManagementPolicyException;
import org.openbaton.sdk.api.exception.SDKException;

/** Created by mob on 30.10.15. */
public interface PolicyManager {
  void manageNSR(NetworkServiceRecord networkServiceRecord)
      throws SDKException, FaultManagementPolicyException, HighAvailabilityException,
          ClassNotFoundException, FileNotFoundException;

  void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException;

  boolean isNSRManaged(String id);

  void cleanUnmanagedNsrs() throws FileNotFoundException, SDKException;

  boolean isAVNFAlarm(String id);

  boolean isAManagedAlarm(String triggerId);

  FaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  String getPolicyIdByThresholdId(String triggerId);

  String getVnfrIdByPolicyId(String policyId)
      throws SDKException, ClassNotFoundException, FileNotFoundException;
}
