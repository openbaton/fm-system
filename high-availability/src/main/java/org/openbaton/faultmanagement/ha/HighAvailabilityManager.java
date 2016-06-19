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

package org.openbaton.faultmanagement.ha;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;

/**
 * Created by mob on 11.01.16.
 */
public interface HighAvailabilityManager {
    void switchToRedundantVNFC(String projectId, VNFCInstance failedVnfcInstance, String nsrId, String vnfrId, String vduId, String vnfcInstanceId) throws HighAvailabilityException;
    void configureRedundancy(VirtualNetworkFunctionRecord nsr) throws HighAvailabilityException, UnirestException;
    void createStandByVNFC(VNFComponent vnfComponent, VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu) throws HighAvailabilityException;
    void switchToRedundantVNFC(VNFCInstance failedVnfcInstance, VirtualNetworkFunctionRecord vnfr,VirtualDeploymentUnit vdu)throws HighAvailabilityException;
    void executeHeal(String cause, String nsrId, String vnfrId, String vduId, String vnfcInstanceId);
    String cleanFailedInstances(VirtualNetworkFunctionRecord vnfr) throws UnirestException;
}
