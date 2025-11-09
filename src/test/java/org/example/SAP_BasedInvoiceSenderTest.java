package org.example;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests for the SAP_BasedInvoiceSender class.
 * These tests verify that SAP integration behaves correctly
 * when there are invoices to send and when there are none.
 */
public class SAP_BasedInvoiceSenderTest {

    @Test
    void testWhenLowInvoicesSent() {
        // This test verifies that sap.send() is called for each low-valued invoice.

        FilterInvoice filterMock = mock(FilterInvoice.class);
        SAP sapMock = mock(SAP.class);

        // Stub filter.lowValueInvoices() to return a list of fake invoices
        List<Invoice> fakeInvoices = List.of(
                new Invoice("A", 50),
                new Invoice("C", 75)
        );
        when(filterMock.lowValueInvoices()).thenReturn(fakeInvoices);

        SAP_BasedInvoiceSender sender = new SAP_BasedInvoiceSender(filterMock, sapMock);

        sender.sendLowValuedInvoices();

        verify(sapMock, times(2)).send(any(Invoice.class));

        // Optional: verify specific invoices were sent
        verify(sapMock).send(new Invoice("A", 50));
        verify(sapMock).send(new Invoice("C", 75));
    }

    @Test
    void testWhenNoInvoices() {
        // This test verifies that sap.send() is NOT called when there are no low-valued invoices.

        FilterInvoice filterMock = mock(FilterInvoice.class);
        SAP sapMock = mock(SAP.class);

        when(filterMock.lowValueInvoices()).thenReturn(List.of());

        SAP_BasedInvoiceSender sender = new SAP_BasedInvoiceSender(filterMock, sapMock);

        sender.sendLowValuedInvoices();

        verify(sapMock, never()).send(any(Invoice.class));
    }
}
