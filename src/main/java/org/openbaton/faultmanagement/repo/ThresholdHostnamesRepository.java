package org.openbaton.faultmanagement.repo;

import org.openbaton.faultmanagement.catalogue.ThresholdHostnames;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 28/06/16.
 */
public interface ThresholdHostnamesRepository extends CrudRepository<ThresholdHostnames, String> {}
