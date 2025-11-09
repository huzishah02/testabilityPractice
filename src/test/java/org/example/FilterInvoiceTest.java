package org.example;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
}
