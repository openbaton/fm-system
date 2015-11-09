package org.openbaton.faultmanagement.repositories;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.catalogue.mano.common.faultmanagement.PerceivedSeverity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
public interface AlarmRepository extends CrudRepository<Alarm, String> {
    List<Alarm> findByVnfrIdAndPerceivedSeverity(String vnfrId,PerceivedSeverity perceivedSeverity);
}
