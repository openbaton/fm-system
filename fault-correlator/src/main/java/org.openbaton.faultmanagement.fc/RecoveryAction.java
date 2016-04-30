/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mob on 22.01.16.
 */
public class RecoveryAction {
    private RecoveryActionType recoveryActionType;
    private String vnfmEndpoint;
    private String vimName;
    private String startingTime;
    private RecoveryActionStatus status;

    public RecoveryAction(RecoveryActionType recoveryActionType, String vnfmEndpoint, String vimName) {
        this.recoveryActionType = recoveryActionType;
        this.vnfmEndpoint = vnfmEndpoint;
        this.vimName = vimName;
        this.startingTime="";
    }

    public RecoveryActionStatus getStatus() {
        return status;
    }

    public void setStatus(RecoveryActionStatus status) {
        this.status = status;
        if(status.ordinal()==RecoveryActionStatus.IN_PROGRESS.ordinal()){
            DateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            this.startingTime = dateFormat.format(date);
        }
    }

    public RecoveryActionType getRecoveryActionType() {
        return recoveryActionType;
    }

    public void setRecoveryActionType(RecoveryActionType recoveryActionType) {
        this.recoveryActionType = recoveryActionType;
    }

    public String getVnfmEndpoint() {
        return vnfmEndpoint;
    }

    public void setVnfmEndpoint(String vnfmEndpoint) {
        this.vnfmEndpoint = vnfmEndpoint;
    }

    public String getVimName() {
        return vimName;
    }

    public void setVimName(String vimName) {
        this.vimName = vimName;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    @Override
    public String toString() {
        return "RecoveryAction{" +
                "recoveryActionType='" + recoveryActionType + '\'' +
                ", vnfmEndpoint='" + vnfmEndpoint + '\'' +
                ", vimName='" + vimName + '\'' +
                ", startingTime='" + startingTime + '\'' +
                ", status=" + status +
                '}';
    }
}
