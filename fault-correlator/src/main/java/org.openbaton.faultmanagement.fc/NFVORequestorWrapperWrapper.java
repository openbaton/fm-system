/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.openbaton.faultmanagement.fc;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.security.Project;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;
import org.openbaton.faultmanagement.fc.interfaces.NFVORequestorWrapper;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
@Service
public class NFVORequestorWrapperWrapper implements NFVORequestorWrapper {
    private static final Logger log = LoggerFactory.getLogger(NFVORequestorWrapperWrapper.class);
    private List<NetworkServiceRecord> nsrList;
    private NFVORequestor nfvoRequestor;
    @Value("${nfvo-usr:}")
    private String nfvoUsr;
    @Value("${nfvo-pwd:}")
    private String nfvoPwd;
    @Value("${nfvo.ip:}")
    private String nfvoIp;
    @Value("${nfvo.port:8080}")
    private String nfvoPort;
    private String projectId;

    @PostConstruct
    public void init() throws IOException {
        this.nfvoRequestor = new NFVORequestor(nfvoUsr,nfvoPwd, null,false,nfvoIp,nfvoPort,"1");
        try {
            log.debug("executing get all projects");

            List<Project> projects = nfvoRequestor.getProjectAgent().findAll();
            log.debug("found " + projects.size() + " projects");
            for (Project project : projects) {
                if (project.getName().equals("default")) {
                    projectId = project.getId();
                }
            }
        } catch (ClassNotFoundException|SDKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws ClassNotFoundException, SDKException {
        nfvoRequestor.setProjectId(projectId);
        return nfvoRequestor.getNetworkServiceRecordAgent().findById(nsrId);
    }


    @Override
    public List<NetworkServiceRecord> getNetworkServiceRecords() throws ClassNotFoundException, SDKException {
        nfvoRequestor.setProjectId(projectId);
        return nfvoRequestor.getNetworkServiceRecordAgent().findAll();
    }


    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws SDKException {
        nfvoRequestor.setProjectId(projectId);
        return nfvoRequestor.getNetworkServiceRecordAgent().getVirtualNetworkFunctionRecord(nsrId,vnfrId);
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId) throws SDKException, ClassNotFoundException {
        for(NetworkServiceRecord nsr : getNetworkServiceRecords()){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                if(vnfr.getId().equals(vnfrId))
                    return vnfr;
            }
        }
        return null;
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname) throws SDKException, ClassNotFoundException {
        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getHostname().equals(hostname))
                            return vnfr;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId) {
        for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                if(vnfcInstance.getId().equals(vnfcInstaceId))
                    return vnfcInstance;
            }
        }
        return null;
    }

    public VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId) {

        for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                if(vnfcInstance.getId().equals(vnfcInstaceId))
                    return vdu;
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstance(String hostname) throws SDKException, ClassNotFoundException {

        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getHostname().equals(hostname))
                            return vnfcInstance;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstanceById(String VnfcId) throws SDKException, ClassNotFoundException {

        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getId().equals(VnfcId))
                            return vnfcInstance;
                    }
                }
            }
        }
        return null;
    }
}
