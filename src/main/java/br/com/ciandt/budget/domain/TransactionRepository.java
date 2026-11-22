package br.com.ciandt.budget.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saída do domínio; não conhece JPA nem qualquer fornecedor de banco. */
public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAll();
    List<Transaction> findByCategory(Category category);
    Optional<Transaction> findById(UUID id);
}
