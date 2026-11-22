package br.com.ciandt.budget.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Registro financeiro imutável. O valor é expresso em centavos para evitar imprecisão decimal. */
public record Transaction(UUID id, String description, long amountCents, Category category, Instant occurredAt) {
    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (description.isBlank()) throw new IllegalArgumentException("A descrição não pode ficar vazia.");
        if (amountCents <= 0) throw new IllegalArgumentException("O valor deve ser maior que zero.");
    }

    public static Transaction create(String description, long amountCents, Category category) {
        return new Transaction(UUID.randomUUID(), description.trim(), amountCents, category, Instant.now());
    }
}
