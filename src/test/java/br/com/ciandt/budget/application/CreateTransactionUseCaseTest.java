package br.com.ciandt.budget.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateTransactionUseCaseTest {

    @Test
    void shouldPersistTransactionThroughRepository() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        CreateTransactionUseCase useCase = new CreateTransactionUseCase(repository);

        Transaction saved = useCase.execute("Supermercado", 12550L, Category.GROCERIES);

        assertNotNull(saved.id());
        assertEquals("Supermercado", saved.description());
        assertEquals(12550L, saved.amountCents());
        assertEquals(Category.GROCERIES, saved.category());
        assertEquals(1, repository.savedItems.size());
    }

    @Test
    void shouldNotSaveWhenDomainValidationFails() {
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        CreateTransactionUseCase useCase = new CreateTransactionUseCase(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("Compra invalida", 0L, Category.OTHER)
        );
        assertEquals(0, repository.savedItems.size());
    }

    private static final class InMemoryTransactionRepository implements TransactionRepository {
        private final List<Transaction> savedItems = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            savedItems.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(savedItems);
        }

        @Override
        public List<Transaction> findByCategory(Category category) {
            return savedItems.stream()
                    .filter(tx -> tx.category() == category)
                    .toList();
        }

        @Override
        public Optional<Transaction> findById(UUID id) {
            return savedItems.stream()
                    .filter(tx -> tx.id().equals(id))
                    .findFirst();
        }
    }
}
