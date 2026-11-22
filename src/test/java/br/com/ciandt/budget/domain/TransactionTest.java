package br.com.ciandt.budget.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void shouldCreateTransactionWithTrimmedDescriptionAndGeneratedId() {
        Transaction tx = Transaction.create("  Estacionamento  ", 6000L, Category.AUTO);

        assertNotNull(tx.id());
        assertEquals("Estacionamento", tx.description());
        assertEquals(6000L, tx.amountCents());
        assertEquals(Category.AUTO, tx.category());
        assertNotNull(tx.occurredAt());
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(UUID.randomUUID(), "   ", 2500L, Category.GROCERIES, Instant.now())
        );
    }

    @Test
    void shouldRejectZeroOrNegativeAmountCents() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(UUID.randomUUID(), "Farmacia", 0L, Category.FARMA, Instant.now())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(UUID.randomUUID(), "Farmacia", -1500L, Category.FARMA, Instant.now())
        );
    }
}
