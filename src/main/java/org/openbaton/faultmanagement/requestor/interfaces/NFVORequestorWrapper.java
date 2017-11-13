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

package org.openbaton.faultmanagement.requestor.interfaces;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.sdk.api.exception.SDKException;

/** Created by mob on 11.01.16. */
public interface NFVORequestorWrapper {
  NetworkServiceRecord getNsr(String nsrId)
      throws ClassNotFoundException, SDKException, FileNotFoundException;

  List<NetworkServiceRecord> getNsrs()
      throws ClassNotFoundException, SDKException, FileNotFoundException;

  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId, String vnfrId)
      throws NotFoundException, SDKException, ClassNotFoundException, FileNotFoundException;

  VNFCInstance getVNFCInstance(String hostname)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  VNFCInstance getVNFCInstanceById(String VnfcId)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);

  VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);

  String subscribe(String projectId, EventEndpoint eventEndpoint)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  String subscribe(EventEndpoint eventEndpoint)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  void deleteVnfcInstance(String nsrId, String vnfrId, String vduId, String vnfcInstanceId)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  void createStandbyVNFCInstance(
      String nsrId,
      String vnfrId,
      String vduId,
      VNFComponent vnfComponent,
      ArrayList<String> vimInstanceNames)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  void switchToStandby(
      String nsrId,
      String vnfrId,
      String vduId,
      String standbyVnfcId,
      VNFCInstance failedVnfcInstance)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  void executeHeal(
      String nsrId, String vnfrId, String vduId, String failedVnfcInstanceId, String cause)
      throws SDKException, ClassNotFoundException, FileNotFoundException;

  void unSubscribe(String id) throws SDKException, ClassNotFoundException, FileNotFoundException;
}
