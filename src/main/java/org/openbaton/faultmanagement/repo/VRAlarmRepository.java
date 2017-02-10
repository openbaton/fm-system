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

package org.openbaton.faultmanagement.repo;

import java.util.List;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/** Created by mob on 31.01.16. */
public interface VRAlarmRepository
    extends CrudRepository<VRAlarm, String>, VRAlarmRepositoryCustom {
  List<VRAlarm> findByManagedObject(String managedObject);

  List<VRAlarm> findByManagedObjectAndAlarmStateNot(String managedObject, AlarmState alarmState);

  VRAlarm findFirstByThresholdId(String thresholdId);

  @Transactional
  List<VRAlarm> removeByManagedObject(String managedObject);
}
