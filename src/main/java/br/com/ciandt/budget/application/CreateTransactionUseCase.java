package br.com.ciandt.budget.application;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import org.springframework.stereotype.Service;

/** Regra de aplicação para registrar uma despesa. */
@Service
public class CreateTransactionUseCase {
    private final TransactionRepository repository;
    public CreateTransactionUseCase(TransactionRepository repository) { this.repository = repository; }
    public Transaction execute(String description, long amountCents, Category category) {
        return repository.save(Transaction.create(description, amountCents, category));
    }
}
