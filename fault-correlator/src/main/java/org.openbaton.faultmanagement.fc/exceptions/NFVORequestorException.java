package org.openbaton.faultmanagement.fc.exceptions;

/**
 * Created by mob on 08.03.16.
 */
public class NFVORequestorException extends Exception {
    public NFVORequestorException() {
    }

    public NFVORequestorException(Throwable cause) {
        super(cause);
    }

    public NFVORequestorException(String message) {
        super(message);
    }

    public NFVORequestorException(String message, Throwable cause) {
        super(message, cause);
    }
}
