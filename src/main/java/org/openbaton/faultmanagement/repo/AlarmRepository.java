package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 26.10.15.
 */
public interface AlarmRepository extends CrudRepository<Alarm, String> {
}
