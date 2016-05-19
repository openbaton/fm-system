/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
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

package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;
import org.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 30.10.15.
 */
public interface PolicyManager {
    void manageNSR(NetworkServiceRecord networkServiceRecord) throws SDKException;
    void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException;
    boolean isNSRManaged(String id);
    boolean isAVNFAlarm(String id);
    boolean isAManagedAlarm(String triggerId);
    VRFaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId);
    VirtualNetworkFunctionRecordShort getVNFRShort(String vnfFMPolicyId);
    String getPolicyIdByThresholdId(String triggerId);
    String getVnfrIdByPolicyId(String policyId);
}
