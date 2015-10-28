package org.openbaton.faultmanagement.repositories;

import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 27.10.15.
 */
public interface SubscriptionRepository extends CrudRepository<AlarmEndpoint, String> {
}
