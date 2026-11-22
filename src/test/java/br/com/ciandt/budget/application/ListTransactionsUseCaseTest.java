package br.com.ciandt.budget.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListTransactionsUseCaseTest {

    @Test
    void shouldReturnAllTransactionsWhenCategoryIsNull() {
        Transaction first = new Transaction(
                UUID.randomUUID(),
                "Supermercado",
                12550L,
                Category.GROCERIES,
                Instant.now()
        );
        Transaction second = new Transaction(
                UUID.randomUUID(),
                "Estacionamento",
                6000L,
                Category.AUTO,
                Instant.now()
        );

        TransactionRepository repository = new StubTransactionRepository(List.of(first, second));
        ListTransactionsUseCase useCase = new ListTransactionsUseCase(repository);

        List<Transaction> result = useCase.execute(null);

        assertEquals(2, result.size());
        assertEquals(List.of(first, second), result);
    }

    @Test
    void shouldFilterTransactionsByCategoryWhenProvided() {
        Transaction autoTx = new Transaction(
                UUID.randomUUID(),
                "Estacionamento",
                6000L,
                Category.AUTO,
                Instant.now()
        );
        Transaction farmaTx = new Transaction(
                UUID.randomUUID(),
                "Farmacia",
                4200L,
                Category.FARMA,
                Instant.now()
        );

        TransactionRepository repository = new StubTransactionRepository(List.of(autoTx, farmaTx));
        ListTransactionsUseCase useCase = new ListTransactionsUseCase(repository);

        List<Transaction> result = useCase.execute(Category.AUTO);

        assertEquals(1, result.size());
        assertEquals(Category.AUTO, result.getFirst().category());
        assertEquals("Estacionamento", result.getFirst().description());
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionMatchesCategory() {
        Transaction otherTx = new Transaction(
                UUID.randomUUID(),
                "Livro",
                5500L,
                Category.OTHER,
                Instant.now()
        );

        TransactionRepository repository = new StubTransactionRepository(List.of(otherTx));
        ListTransactionsUseCase useCase = new ListTransactionsUseCase(repository);

        List<Transaction> result = useCase.execute(Category.FARMA);

        assertTrue(result.isEmpty());
    }

    private record StubTransactionRepository(List<Transaction> items) implements TransactionRepository {

        @Override
        public Transaction save(Transaction transaction) {
            throw new UnsupportedOperationException("Nao utilizado neste teste");
        }

        @Override
        public List<Transaction> findAll() {
            return items;
        }

        @Override
        public List<Transaction> findByCategory(Category category) {
            return items.stream()
                    .filter(tx -> tx.category() == category)
                    .toList();
        }

        @Override
        public Optional<Transaction> findById(UUID id) {
            return items.stream()
                    .filter(tx -> tx.id().equals(id))
                    .findFirst();
        }
    }
}
