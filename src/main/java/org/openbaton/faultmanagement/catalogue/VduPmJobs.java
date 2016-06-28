package org.openbaton.faultmanagement.catalogue;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by mob on 27/06/16.
 */
@Entity
public class VduPmJobs {
    @Id
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> pmJobsIds;

    public VduPmJobs(){

    }
    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPmJobsIds() {
        return pmJobsIds;
    }

    public void setPmJobsIds(List<String> pmJobsIds) {
        this.pmJobsIds = pmJobsIds;
    }

    @Override
    public String toString() {
        return "VduPmJobs{" +
                "id='" + id + '\'' +
                ", pmJobsIds=" + pmJobsIds +
                '}';
    }
}
