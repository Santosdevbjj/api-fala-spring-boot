package br.com.ciandt.budget.infrastructure.persistence;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import br.com.ciandt.budget.domain.TransactionRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapta o contrato de domínio para o Spring Data JPA. */
@Repository
class TransactionRepositoryAdapter implements TransactionRepository {
    private final SpringDataTransactionRepository jpa;
    TransactionRepositoryAdapter(SpringDataTransactionRepository jpa) { this.jpa = jpa; }
    @Override public Transaction save(Transaction tx) { return jpa.save(TransactionEntity.fromDomain(tx)).toDomain(); }
    @Override public List<Transaction> findAll() { return jpa.findAllByOrderByOccurredAtDesc().stream().map(TransactionEntity::toDomain).toList(); }
    @Override public List<Transaction> findByCategory(Category category) { return jpa.findByCategoryOrderByOccurredAtDesc(category).stream().map(TransactionEntity::toDomain).toList(); }
    @Override public Optional<Transaction> findById(UUID id) { return jpa.findById(id).map(TransactionEntity::toDomain); }
}
