package de.fhg.fokus.ngni.nfvo.repository.mano.common;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */

import de.fhg.fokus.ngni.nfvo.repository.mano.descriptor.VirtualNetworkFunctionDescriptor;
import de.fhg.fokus.ngni.nfvo.repository.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Describe dependencies between VNF. Defined in terms of
 * source and target VNF i.e. target VNF "depends on" source
 * VNF. In other words a source VNF shall exist and connect to
 * the service before target VNF can be initiated/deployed and
 * connected. This element would be used, for example, to define
 * the sequence in which various numbered network nodes and
 * links within a VNF FG should be instantiated by the NFV
 * Orchestrator.*/
@Entity
public class VNFDependency implements Serializable{
	@Id
	private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;
	
	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private VirtualNetworkFunctionDescriptor source;
	
	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private VirtualNetworkFunctionDescriptor target;

    public VNFDependency() {
    }

    public VirtualNetworkFunctionDescriptor getSource() {

        return source;
    }

    public void setSource(VirtualNetworkFunctionDescriptor source) {
        this.source = source;
    }

    public VirtualNetworkFunctionDescriptor getTarget() {
        return target;
    }

    public void setTarget(VirtualNetworkFunctionDescriptor target) {
        this.target = target;
    }
}
