package br.com.ciandt.budget.infrastructure.persistence;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Modelo de persistência. Mantido separado do registro de domínio. */
@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 240)
    private String description;
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Category category;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TransactionEntity() { }
    private TransactionEntity(Transaction tx) {
        id = tx.id(); description = tx.description(); amountCents = tx.amountCents();
        category = tx.category(); occurredAt = tx.occurredAt();
    }
    static TransactionEntity fromDomain(Transaction tx) { return new TransactionEntity(tx); }
    Transaction toDomain() { return new Transaction(id, description, amountCents, category, occurredAt); }
}
