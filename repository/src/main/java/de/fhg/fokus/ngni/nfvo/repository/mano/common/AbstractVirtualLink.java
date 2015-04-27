package de.fhg.fokus.ngni.nfvo.repository.mano.common;

import de.fhg.fokus.ngni.nfvo.repository.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 * The VLD describes the basic topology of the connectivity (e.g. E-LAN, E-Line, E-Tree) between one or more VNFs connected
 * to this VL and other required parameters (e.g. bandwidth and QoS class). The VLD connection parameters are expected to
 * have similar attributes to those used on the ports on VNFs in ETSI GS NFV-SWA 001 [i.8]. Therefore a set of VLs in a
 * Network Service can be mapped to a Network Connectivity Topology (NCT) as defined in ETSI GS NFV-SWA 001 [i.8].
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractVirtualLink {

    /**
     * ID of the VLD
     * */
	@Id
    protected String id = IdGenerator.createUUID();
	@Version
	protected int version = 0;

        /**
     * Throughput of the link (e.g. bandwidth of E-Line, root bandwidth of E-Tree, and aggregate capacity of E-LAN)
     * */
    private String root_requirement;
    /**
     * Throughput of leaf connections to the link (for E-Tree and E-LAN branches)
     * */
    private String leaf_requirement;
    /**
     * QoS options available on the VL, e.g. latency, jitter, etc.
     * */
    @ElementCollection
    private List<String> qos;
    /**
     * Test access facilities available on the VL (e.g. none, passive monitoring, or active (intrusive) loopbacks at endpoints
     * TODO think of using Enum instead of String
     * */
    @ElementCollection
    private List<String> test_access;
    /**
     * Connectivity types, e.g. E-Line, E-LAN, or E-Tree.
     * TODO: think of using Enum instead of String
     * */
    private String connectivity_type;

    public AbstractVirtualLink() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoot_requirement() {
        return root_requirement;
    }

    public void setRoot_requirement(String root_requirement) {
        this.root_requirement = root_requirement;
    }

    public String getLeaf_requirement() {
        return leaf_requirement;
    }

    public void setLeaf_requirement(String leaf_requirement) {
        this.leaf_requirement = leaf_requirement;
    }

    public List<String> getQos() {
        return qos;
    }

    public void setQos(List<String> qos) {
        this.qos = qos;
    }

    public List<String> getTest_access() {
        return test_access;
    }

    public void setTest_access(List<String> test_access) {
        this.test_access = test_access;
    }

    public String getConnectivity_type() {
        return connectivity_type;
    }

    public void setConnectivity_type(String connectivity_type) {
        this.connectivity_type = connectivity_type;
    }
}
