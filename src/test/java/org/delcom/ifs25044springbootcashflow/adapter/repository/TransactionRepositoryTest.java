package org.delcom.ifs25044springbootcashflow.adapter.repository;

import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveAssignsIncrementingIds() {
        Transaction first = repository.save(1, "Gaji", 100L, TransactionType.INCOME);
        Transaction second = repository.save(1, "Kopi", 20L, TransactionType.EXPENSE);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByUserIdIsScopedToOwner() {
        repository.save(1, "A", 10L, TransactionType.INCOME);
        repository.save(2, "B", 20L, TransactionType.INCOME);
        repository.save(1, "C", 30L, TransactionType.EXPENSE);

        assertEquals(2, repository.findByUserId(1).size());
        assertEquals(1, repository.findByUserId(2).size());
        assertTrue(repository.findByUserId(99).isEmpty());
    }

    @Test
    void findByIdUpdateAndDelete() {
        Transaction saved = repository.save(1, "Gaji", 100L, TransactionType.INCOME);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertTrue(repository.findById(99).isEmpty());

        saved.changeDescription("Gaji Baru");
        repository.update(saved);
        assertEquals("Gaji Baru", repository.findById(saved.getId()).orElseThrow().getDescription());

        assertEquals(1, repository.findBy(t -> t.getAmount() == 100L).size());

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
    }
}
