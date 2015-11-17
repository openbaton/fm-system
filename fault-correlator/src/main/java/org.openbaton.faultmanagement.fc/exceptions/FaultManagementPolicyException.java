package org.openbaton.faultmanagement.fc.exceptions;

/**
 * Created by mob on 29.10.15.
 */
public class FaultManagementPolicyException extends Exception{
    public FaultManagementPolicyException() {
    }
    public FaultManagementPolicyException(Throwable cause) {
        super(cause);
    }

    public FaultManagementPolicyException(String message) {
        super(message);
    }

    public FaultManagementPolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
