package br.com.ciandt.budget.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ciandt.budget.application.CreateTransactionUseCase;
import br.com.ciandt.budget.application.ListTransactionsUseCase;
import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TransactionControllerTest {

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {
        Transaction created = new Transaction(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Estacionamento",
                6000L,
                Category.AUTO,
                Instant.parse("2026-11-22T12:00:00Z")
        );

        CreateTransactionUseCase createUseCase = new CreateTransactionUseCase(new StubRepository(List.of(created)));
        ListTransactionsUseCase listUseCase = new ListTransactionsUseCase(new StubRepository(List.of(created)));
        TransactionController controller = new TransactionController(createUseCase, listUseCase);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        String body = """
                {
                  "description": "Estacionamento",
                  "amountCents": 6000,
                  "category": "AUTO"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Estacionamento"))
                .andExpect(jsonPath("$.amountCents").value(6000))
                .andExpect(jsonPath("$.category").value("AUTO"));
    }

    @Test
    void shouldListAndFilterTransactionsByCategory() throws Exception {
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
                4500L,
                Category.FARMA,
                Instant.now()
        );

        List<Transaction> all = List.of(autoTx, farmaTx);
        CreateTransactionUseCase createUseCase = new CreateTransactionUseCase(new StubRepository(all));
        ListTransactionsUseCase listUseCase = new ListTransactionsUseCase(new StubRepository(all));
        TransactionController controller = new TransactionController(createUseCase, listUseCase);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/transactions").param("category", "AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("AUTO"))
                .andExpect(jsonPath("$[0].description").value("Estacionamento"));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        CreateTransactionUseCase createUseCase = new CreateTransactionUseCase(new StubRepository(List.of()));
        ListTransactionsUseCase listUseCase = new ListTransactionsUseCase(new StubRepository(List.of()));
        TransactionController controller = new TransactionController(createUseCase, listUseCase);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        String invalidBody = """
                {
                  "description": "",
                  "amountCents": 0,
                  "category": "AUTO"
                }
                """;

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    private record StubRepository(List<Transaction> items) implements TransactionRepository {
        @Override
        public Transaction save(Transaction transaction) {
            return transaction;
        }

        @Override
        public List<Transaction> findAll() {
            return items;
        }

        @Override
        public List<Transaction> findByCategory(Category category) {
            return items.stream().filter(tx -> tx.category() == category).toList();
        }

        @Override
        public Optional<Transaction> findById(UUID id) {
            return items.stream().filter(tx -> tx.id().equals(id)).findFirst();
        }
    }
}
