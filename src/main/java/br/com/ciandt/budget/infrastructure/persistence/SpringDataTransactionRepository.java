package br.com.ciandt.budget.infrastructure.persistence;

import br.com.ciandt.budget.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findAllByOrderByOccurredAtDesc();
    List<TransactionEntity> findByCategoryOrderByOccurredAtDesc(Category category);
}
