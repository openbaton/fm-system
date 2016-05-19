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

package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;

import java.util.List;

/**
 * Created by mob on 11.01.16.
 */
public interface NFVORequestorWrapper {
    NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws NotFoundException, NFVORequestorException;
    List<NetworkServiceRecord> getNetworkServiceRecords() throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws NotFoundException, NFVORequestorException;
    VNFCInstance getVNFCInstance(String hostname) throws NFVORequestorException;
    VNFCInstance getVNFCInstanceById(String VnfcId) throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId) throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname) throws NFVORequestorException;
    VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId);
    VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);
}
