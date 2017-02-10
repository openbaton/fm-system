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

import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.springframework.data.repository.CrudRepository;

/** Created by mob on 26.10.15. */
public interface VNFAlarmRepository
    extends CrudRepository<VNFAlarm, String>, VNFAlarmRepositoryCustom {
  //List<Alarm> findByThresholdIdAndPerceivedSeverity(String thresholdId, PerceivedSeverity perceivedSeverity);
  //List<Alarm> findByThresholdIdAndAlarmStateNot(String threshold, AlarmState alarmState);
  VNFAlarm findFirstByThresholdId(String threshold);

  VNFAlarm findFirstByVnfrId(String vnfrId);
  //List<Alarm> findByResourceIdAndAlarmStateNotAndAlarmType(String resourceId, AlarmState alarmState, AlarmType alarmType);
}
