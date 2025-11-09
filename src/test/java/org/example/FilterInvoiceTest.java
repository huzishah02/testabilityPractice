package org.example;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for FilterInvoice.lowValueInvoices().
 * Uses the real Database and DAO — no stubs or mocks.
 */
class FilterInvoiceTest {

    private Database db;
    private FilterInvoice filter;

    @BeforeEach
    void setUp() throws SQLException {
        db = new Database();
        db.resetDatabase();

        // Insert test data directly into the in-memory DB
        try (var ps = db.getConnection().prepareStatement("INSERT INTO invoice (name, value) VALUES (?, ?)")) {
            ps.setString(1, "A"); ps.setDouble(2, 50); ps.executeUpdate();
            ps.setString(1, "B"); ps.setDouble(2, 150); ps.executeUpdate();
            ps.setString(1, "C"); ps.setDouble(2, 99); ps.executeUpdate();
            db.getConnection().commit();
        }

        // Pass the same Database instance into FilterInvoice
        filter = new FilterInvoice(db);
    }

    @AfterEach
    void tearDown() {
        db.close();   // close connection after test
    }

    @Test
    void filterInvoiceTest() {
        // Call the real method under test
        List<Invoice> lowValued = filter.lowValueInvoices();

        // Expect invoices A (50) and C (99) only
        assertEquals(2, lowValued.size(),
                "Should return only invoices with value < 100");

        assertTrue(lowValued.stream()
                        .allMatch(inv -> inv.getValue() < 100),
                "All returned invoices must be below 100");

        // Optionally verify names to be certain
        assertTrue(
                lowValued.stream().anyMatch(i -> i.getCustomer().equals("A")) &&
                        lowValued.stream().anyMatch(i -> i.getCustomer().equals("C")),
                "Expected invoices A and C only");
    }

    @Test
    void filterInvoiceStubbedTest() {
        // Create a mock DAO ---
        QueryInvoicesDAO mockDao = Mockito.mock(QueryInvoicesDAO.class);

        //Create a dummy database (won’t actually be used) ---
        Database fakeDb = Mockito.mock(Database.class);

        // Prepare fake invoice data ---
        List<Invoice> fakeInvoices = List.of(
                new Invoice("X", 50),   // low value
                new Invoice("Y", 120),  // high value
                new Invoice("Z", 75)    // low value
        );

        // Stub the DAO's .all() method to return the fake data ---
        Mockito.when(mockDao.all()).thenReturn(fakeInvoices);

        // Create a FilterInvoice with stubbed DAO ---
        // To inject it, we temporarily create a subclass (or you could refactor FilterInvoice to accept DAO directly)
        FilterInvoice filter = new FilterInvoice(fakeDb);
        filter.dao = mockDao; // direct injection for testability

        // Call the method under test ---
        List<Invoice> result = filter.lowValueInvoices();

        // Verify filtering logic (no DB involved) ---
        assertThat(result)
                .extracting(Invoice::getCustomer)
                .containsExactlyInAnyOrder("X", "Z");

        assertThat(result)
                .allMatch(inv -> inv.getValue() < 100);

        // Verify that DAO’s all() method was called exactly once ---
        Mockito.verify(mockDao, Mockito.times(1)).all();
    }
}
