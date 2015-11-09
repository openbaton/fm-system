package org.openbaton.faultmanagement.fc;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mob on 09.11.15.
 */
@Service
public class FaultCorrelatorManager {

    private Map<String, List<Alarm>> alarmRepo;

    @PostConstruct
    public void init(){
        alarmRepo=new HashMap<>();
        alarmRepo.put("VNF Alarm", new ArrayList<Alarm>());
        alarmRepo.put("VIM Alarm", new ArrayList<Alarm>());
    }

    public void manageAlarm(Alarm alarm){

    }

}
