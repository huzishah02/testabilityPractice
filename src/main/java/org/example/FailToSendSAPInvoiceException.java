package org.example;


//Custom exception to represent a failure when sending an invoice to SAP.
public class FailToSendSAPInvoiceException extends RuntimeException {

    public FailToSendSAPInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailToSendSAPInvoiceException(String message) {
        super(message);
    }
}
