package org.delcom.ifs25044springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void shortConstructorSetsDefaults() {
        Transaction transaction = new Transaction(1, 7, "Gaji", 5_000_000L, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals(7, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNull(transaction.getProofPath());
        assertTrue(transaction.getCreatedAt() != null);
    }

    @Test
    void fullConstructorKeepsProofAndCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 1, 8, 30);
        Transaction transaction = new Transaction(
                2, 7, "Makan", 25_000L, TransactionType.EXPENSE, "proofs/a.png", createdAt);

        assertEquals("proofs/a.png", transaction.getProofPath());
        assertEquals(createdAt, transaction.getCreatedAt());
    }

    @Test
    void signedAmountFollowsType() {
        Transaction income = new Transaction(1, 1, "Gaji", 100L, TransactionType.INCOME);
        Transaction expense = new Transaction(2, 1, "Kopi", 40L, TransactionType.EXPENSE);

        assertTrue(income.isIncome());
        assertFalse(expense.isIncome());
        assertEquals(100L, income.signedAmount());
        assertEquals(-40L, expense.signedAmount());
    }

    @Test
    void mutatorsChangeFields() {
        Transaction transaction = new Transaction(1, 1, "Lama", 10L, TransactionType.INCOME);

        transaction.changeDescription("Baru");
        transaction.changeAmount(99L);
        transaction.changeType(TransactionType.EXPENSE);
        transaction.changeProofPath("proofs/b.png");

        assertEquals("Baru", transaction.getDescription());
        assertEquals(99L, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("proofs/b.png", transaction.getProofPath());

        transaction.changeProofPath(null);
        assertNull(transaction.getProofPath());
    }
}
