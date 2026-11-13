package org.delcom.ifs25044springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    private final Transaction salary = new Transaction(1, 1, "Gaji", 500L, TransactionType.INCOME);
    private final Transaction coffee = new Transaction(2, 1, "Kopi", 20L, TransactionType.EXPENSE);
    private final Transaction bonus = new Transaction(3, 1, "Bonus", 100L, TransactionType.INCOME);
    private final Transaction rent = new Transaction(4, 1, "Sewa", 300L, TransactionType.EXPENSE);

    private List<Integer> sortedIds(SortOption option) {
        List<Transaction> list = new ArrayList<>(List.of(salary, coffee, bonus, rent));
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void hasAtLeastFourOptions() {
        assertTrue(SortOption.values().length >= 4);
    }

    @Test
    void amountAscending() {
        assertEquals(List.of(2, 3, 4, 1), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescending() {
        assertEquals(List.of(1, 4, 3, 2), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstKeepsIncomeBeforeExpense() {
        assertEquals(List.of(1, 3, 2, 4), sortedIds(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstKeepsExpenseBeforeIncome() {
        assertEquals(List.of(2, 4, 1, 3), sortedIds(SortOption.EXPENSE_FIRST));
    }

    @Test
    void valueOfResolvesByName() {
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }
}
