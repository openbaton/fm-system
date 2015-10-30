package org.openbaton.faultmanagement.exceptions;

/**
 * Created by mob on 30.10.15.
 */
public class ZabbixMetricParserException extends Exception {
    public ZabbixMetricParserException() {
    }

    public ZabbixMetricParserException(Throwable cause) {
        super(cause);
    }

    public ZabbixMetricParserException(String message) {
        super(message);
    }

    public ZabbixMetricParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
