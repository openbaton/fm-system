package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 09.02.16.
 */
public interface RecoveryActionRepository extends CrudRepository<VNFAlarm, String> {

}
