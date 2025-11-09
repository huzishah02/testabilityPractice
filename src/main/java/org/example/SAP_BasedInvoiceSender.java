package org.example;

import java.util.List;
import java.util.ArrayList;

// Class responsible for sending low-valued invoices to the SAP system
public class SAP_BasedInvoiceSender {

    private final FilterInvoice filter;  // Dependency for filtering invoices
    private final SAP sap;  // Dependency for sending invoices to the SAP system

    // Constructor that uses dependency injection to initialize the filter and sap objects
    public SAP_BasedInvoiceSender(FilterInvoice filter, SAP sap) {
        this.filter = filter;
        this.sap = sap;
    }

    // Modified
    public List<Invoice> sendLowValuedInvoices() {
        List<Invoice> lowValuedInvoices = filter.lowValueInvoices();
        List<Invoice> failedInvoices = new ArrayList<>();

        for (Invoice invoice : lowValuedInvoices) {
            try {
                sap.send(invoice);
            } catch (Exception e) {
                // Record the failed invoice but do NOT stop the process
                failedInvoices.add(invoice);
                System.err.println("Failed to send invoice: " + invoice.getCustomer());
            }
        }

        return failedInvoices;
    }
}