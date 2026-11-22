package br.com.ciandt.budget.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionToolsTest {

    @Test
    void shouldRegisterExpenseWithInformedCategory() {
        InMemoryRepository repository = new InMemoryRepository();
        TransactionTools tools = newTools(repository);

        Transaction created = tools.registerExpense("Estacionamento", 6000L, Category.AUTO);

        assertNotNull(created.id());
        assertEquals("Estacionamento", created.description());
        assertEquals(6000L, created.amountCents());
        assertEquals(Category.AUTO, created.category());
    }

    @Test
    void shouldDefaultToOtherCategoryWhenCategoryIsNull() {
        InMemoryRepository repository = new InMemoryRepository();
        TransactionTools tools = newTools(repository);

        Transaction created = tools.registerExpense("Presente", 8000L, null);

        assertEquals(Category.OTHER, created.category());
    }

    @Test
    void shouldListAllOrFilteredExpensesThroughTool() {
        InMemoryRepository repository = new InMemoryRepository();
        TransactionTools tools = newTools(repository);

        tools.registerExpense("Mercado", 15000L, Category.GROCERIES);
        tools.registerExpense("Remedio", 3500L, Category.FARMA);

        List<Transaction> all = tools.listExpenses(null);
        List<Transaction> farmaOnly = tools.listExpenses(Category.FARMA);

        assertEquals(2, all.size());
        assertEquals(1, farmaOnly.size());
        assertEquals("Remedio", farmaOnly.getFirst().description());
    }

    private TransactionTools newTools(InMemoryRepository repository) {
        CreateTransactionUseCase createUseCase = new CreateTransactionUseCase(repository);
        ListTransactionsUseCase listUseCase = new ListTransactionsUseCase(repository);
        return new TransactionTools(createUseCase, listUseCase);
    }

    private static final class InMemoryRepository implements TransactionRepository {
        private final List<Transaction> storage = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            storage.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public List<Transaction> findByCategory(Category category) {
            return storage.stream()
                    .filter(tx -> tx.category() == category)
                    .toList();
        }

        @Override
        public Optional<Transaction> findById(UUID id) {
            return storage.stream()
                    .filter(tx -> tx.id().equals(id))
                    .findFirst();
        }
    }
}
