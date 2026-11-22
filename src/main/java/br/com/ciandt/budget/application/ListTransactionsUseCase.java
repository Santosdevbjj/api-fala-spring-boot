package br.com.ciandt.budget.application;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/** Consulta transações, opcionalmente filtrando por categoria. */
@Service
public class ListTransactionsUseCase {
    private final TransactionRepository repository;
    public ListTransactionsUseCase(TransactionRepository repository) { this.repository = repository; }
    public List<Transaction> execute(Category category) {
        return category == null ? repository.findAll() : repository.findByCategory(category);
    }
}
