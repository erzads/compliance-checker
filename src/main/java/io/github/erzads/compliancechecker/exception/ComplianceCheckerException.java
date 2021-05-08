package io.github.erzads.compliancechecker.exception;

/**
 * @author danilo.saita on 08/05/2021.
 */
public class ComplianceCheckerException extends RuntimeException{
    public ComplianceCheckerException(String message) {
        super(message);
    }

    public ComplianceCheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
