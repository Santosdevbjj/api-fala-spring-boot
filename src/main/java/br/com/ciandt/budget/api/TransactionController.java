package br.com.ciandt.budget.api;

import br.com.ciandt.budget.application.CreateTransactionUseCase;
import br.com.ciandt.budget.application.ListTransactionsUseCase;
import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final CreateTransactionUseCase create;
    private final ListTransactionsUseCase list;
    public TransactionController(CreateTransactionUseCase create, ListTransactionsUseCase list) {
        this.create = create; this.list = list;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest request) {
        return TransactionResponse.from(create.execute(request.description(), request.amountCents(), request.category()));
    }

    @GetMapping
    public List<TransactionResponse> list(@RequestParam(required = false) Category category) {
        return list.execute(category).stream().map(TransactionResponse::from).toList();
    }

    public record CreateTransactionRequest(@NotBlank String description, @Positive long amountCents, @NotNull Category category) { }
    public record TransactionResponse(UUID id, String description, long amountCents, Category category, Instant occurredAt) {
        static TransactionResponse from(Transaction tx) {
            return new TransactionResponse(tx.id(), tx.description(), tx.amountCents(), tx.category(), tx.occurredAt());
        }
    }
}
