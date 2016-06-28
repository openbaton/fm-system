package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mob on 31.01.16.
 */
@Transactional(readOnly = true)
public class VNFAlarmRepositoryImpl implements VNFAlarmRepositoryCustom{

    @Autowired VNFAlarmRepository vnfAlarmRepository;

    @Override
    @Transactional
    public VNFAlarm changeAlarmState(String vnfrId, AlarmState alarmState) {
        VNFAlarm vnfAlarm = vnfAlarmRepository.findFirstByVnfrId(vnfrId);
        if(vnfAlarm!=null){
            vnfAlarm.setAlarmState(alarmState);
        }
        return vnfAlarm;
    }
}
