package org.openbaton.faultmanagement.fc.exceptions;

/**
 * Created by mob on 11.01.16.
 */
public class FaultCorrelatorException extends Exception{

    public FaultCorrelatorException() {
    }
    public FaultCorrelatorException(Throwable cause) {
        super(cause);
    }

    public FaultCorrelatorException(String message) {
        super(message);
    }

    public FaultCorrelatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
