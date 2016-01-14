package org.openbaton.faultmanagement.ha.exceptions;

/**
 * Created by mob on 11.01.16.
 */
public class HighAvailabilityException extends Exception{
    public HighAvailabilityException() {
    }
    public HighAvailabilityException(Throwable cause) {
        super(cause);
    }

    public HighAvailabilityException(String message) {
        super(message);
    }

    public HighAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
