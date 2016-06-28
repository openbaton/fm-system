package org.openbaton.faultmanagement.repo;

import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 24/06/16.
 */
public interface ManagedNetworkServiceRecordRepository extends CrudRepository<ManagedNetworkServiceRecord, String>,ManagedNetworkServiceRecordRepositoryCustom {
    ManagedNetworkServiceRecord findByNsrId(String nsrId);
}
