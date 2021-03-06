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

package org.openbaton.faultmanagement.core.ham.interfaces;

import java.util.ArrayList;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.core.ham.exceptions.HighAvailabilityException;

/** Created by mob on 11.01.16. */
public interface HighAvailabilityManager {
  void configureRedundancy(NetworkServiceRecord nsr) throws HighAvailabilityException;

  void createStandByVNFC(
      VNFComponent vnfComponent,
      VirtualNetworkFunctionRecord vnfr,
      VirtualDeploymentUnit vdu,
      ArrayList<String> vimInstanceNames)
      throws HighAvailabilityException;

  void switchToStandby(String vnfrId, String failedVnfcInstanceId) throws HighAvailabilityException;

  void executeHeal(String failedVnfcInstanceId, String cause) throws HighAvailabilityException;

  boolean hasFailedVnfcInstances(String vnfrId) throws HighAvailabilityException;

  void cleanFailedInstances(String vnfrId) throws HighAvailabilityException;

  void stopConfigureRedundancy(String nsrId);
}
