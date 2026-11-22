package br.com.ciandt.budget.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransactionRepositoryAdapterTest {

    @Autowired
    private SpringDataTransactionRepository springDataRepository;

    private TransactionRepository repositoryAdapter;

    @BeforeEach
    void setUp() {
        springDataRepository.deleteAll();
        repositoryAdapter = new TransactionRepositoryAdapter(springDataRepository);
    }

    @Test
    void shouldSaveAndFindTransactionById() {
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                "Supermercado do bairro",
                14890L,
                Category.GROCERIES,
                Instant.parse("2026-11-22T10:15:30Z")
        );

        Transaction saved = repositoryAdapter.save(transaction);
        Optional<Transaction> found = repositoryAdapter.findById(saved.id());

        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.get().id());
        assertEquals("Supermercado do bairro", found.get().description());
        assertEquals(14890L, found.get().amountCents());
        assertEquals(Category.GROCERIES, found.get().category());
        assertEquals(Instant.parse("2026-11-22T10:15:30Z"), found.get().occurredAt());
    }

    @Test
    void shouldReturnAllTransactionsOrderedByOccurredAtDesc() {
        Transaction older = new Transaction(
                UUID.randomUUID(),
                "Estacionamento manha",
                2500L,
                Category.AUTO,
                Instant.parse("2026-11-22T08:00:00Z")
        );
        Transaction newer = new Transaction(
                UUID.randomUUID(),
                "Farmacia tarde",
                5400L,
                Category.FARMA,
                Instant.parse("2026-11-22T16:30:00Z")
        );

        repositoryAdapter.save(older);
        repositoryAdapter.save(newer);

        List<Transaction> all = repositoryAdapter.findAll();

        assertEquals(2, all.size());
        assertEquals(newer.id(), all.get(0).id());
        assertEquals(older.id(), all.get(1).id());
    }

    @Test
    void shouldFilterTransactionsByCategoryOrderedByOccurredAtDesc() {
        Transaction olderAuto = new Transaction(
                UUID.randomUUID(),
                "Combustivel",
                12000L,
                Category.AUTO,
                Instant.parse("2026-11-20T09:00:00Z")
        );
        Transaction newerAuto = new Transaction(
                UUID.randomUUID(),
                "Pedagio",
                1850L,
                Category.AUTO,
                Instant.parse("2026-11-22T11:20:00Z")
        );
        Transaction otherTx = new Transaction(
                UUID.randomUUID(),
                "Assinatura",
                3990L,
                Category.OTHER,
                Instant.parse("2026-11-21T14:00:00Z")
        );

        repositoryAdapter.save(olderAuto);
        repositoryAdapter.save(newerAuto);
        repositoryAdapter.save(otherTx);

        List<Transaction> autoTransactions = repositoryAdapter.findByCategory(Category.AUTO);

        assertEquals(2, autoTransactions.size());
        assertEquals(newerAuto.id(), autoTransactions.get(0).id());
        assertEquals(olderAuto.id(), autoTransactions.get(1).id());
    }

    @Test
    void shouldReturnEmptyWhenTransactionIdDoesNotExist() {
        Optional<Transaction> result = repositoryAdapter.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
    }
}
