package org.delcom.ifs25044springbootcashflow.usecase;

import org.delcom.ifs25044springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25044springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.MonthlySummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25044springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER = 1;
    private static final int OTHER = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void addIncomeAndExpenseTrimDescription() throws Exception {
        Transaction income = useCase.addIncome(USER, "  Gaji  ", 5_000_000L);
        Transaction expense = useCase.addExpense(USER, "Kopi", 25_000L);

        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, useCase.getTransactionsByUser(USER).size());
    }

    @Test
    void addTransactionValidation() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, null, 10L));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "   ", 10L));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "Gaji", 0L));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "Kopi", -5L));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(USER, "Gaji", 10L, null));
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());
    }

    @Test
    void parseAmountAcceptsIntegersOnly() throws Exception {
        assertEquals(1_000_000L, useCase.parseAmount(" 1000000 "));
        assertThrows(ValidationException.class, () -> useCase.parseAmount(null));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("   "));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("abc"));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("1.5"));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("99999999999999999999"));
    }

    @Test
    void getAllTransactionsReturnsEveryUsersData() throws Exception {
        useCase.addIncome(USER, "A", 10L);
        useCase.addIncome(OTHER, "B", 20L);

        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(1, useCase.getTransactionsByUser(OTHER).size());
    }

    @Test
    void removeTransactionByIdAndOwner() throws Exception {
        Transaction mine = useCase.addIncome(USER, "A", 10L);
        Transaction theirs = useCase.addIncome(OTHER, "B", 20L);

        useCase.removeTransactionForUser(USER, mine.getId());
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());

        useCase.removeTransaction(theirs.getId());
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeTransactionErrors() throws Exception {
        Transaction theirs = useCase.addIncome(OTHER, "B", 20L);

        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(USER, -1));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER, 99));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(USER, theirs.getId()));
        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction tx = useCase.addIncome(USER, "Gaji", 100L);

        useCase.updateTransactionForUser(USER, tx.getId(), "  Gaji Baru ", 250L,
                TransactionType.EXPENSE, "proofs/a.png", false);
        assertEquals("Gaji Baru", tx.getDescription());
        assertEquals(250L, tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals("proofs/a.png", tx.getProofPath());

        useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, null, false);
        assertEquals("Gaji Baru", tx.getDescription());
        assertEquals(250L, tx.getAmount());
        assertEquals("proofs/a.png", tx.getProofPath());

        useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, null, true);
        assertEquals(null, tx.getProofPath());
    }

    @Test
    void updateValidatesBeforeMutating() throws Exception {
        Transaction tx = useCase.addIncome(USER, "Gaji", 100L);
        Transaction theirs = useCase.addIncome(OTHER, "B", 20L);

        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER, tx.getId(), "  ", null, null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER, tx.getId(), "Baru", 0L, null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER, 0, "Baru", 5L, null, null, false));
        assertThrows(ValidationException.class, () ->
                useCase.updateTransactionForUser(USER, theirs.getId(), "Baru", 5L, null, null, false));
        assertThrows(EntityNotFoundException.class, () ->
                useCase.updateTransactionForUser(USER, 99, "Baru", 5L, null, null, false));

        assertEquals("Gaji", tx.getDescription());
        assertEquals(100L, tx.getAmount());
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws Exception {
        useCase.addIncome(USER, "Gaji Bulanan", 100L);
        useCase.addExpense(USER, "Makan siang", 20L);
        useCase.addIncome(OTHER, "Gaji Orang Lain", 100L);

        assertEquals(1, useCase.searchTransactions(USER, "GAJI").size());
        assertEquals(1, useCase.searchTransactions(USER, " makan ").size());
        assertEquals(2, useCase.searchTransactions(USER, "").size());
        assertEquals(2, useCase.searchTransactions(USER, null).size());
        assertTrue(useCase.searchTransactions(USER, "tidak ada").isEmpty());
    }

    @Test
    void sortUsesSortOptionComparator() throws Exception {
        useCase.addIncome(USER, "Gaji", 500L);
        useCase.addExpense(USER, "Kopi", 20L);
        useCase.addIncome(USER, "Bonus", 100L);

        List<Transaction> asc = useCase.sortTransactions(USER, SortOption.AMOUNT_ASC);
        assertEquals(List.of(20L, 100L, 500L), asc.stream().map(Transaction::getAmount).toList());

        List<Transaction> expenseFirst = useCase.sortTransactions(USER, SortOption.EXPENSE_FIRST);
        assertEquals(TransactionType.EXPENSE, expenseFirst.get(0).getType());

        assertThrows(ValidationException.class, () -> useCase.sortTransactions(USER, null));
    }

    @Test
    void balanceAndSummaryAreConsistent() throws Exception {
        useCase.addIncome(USER, "Gaji", 5_000_000L);
        useCase.addExpense(USER, "Kopi", 25_000L);
        useCase.addIncome(OTHER, "Orang lain", 999L);

        BalanceSummary summary = useCase.summarize(USER);
        assertEquals(2, summary.count());
        assertEquals(5_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());
        assertEquals(4_975_000L, useCase.getBalance(USER));

        assertEquals(0L, useCase.getBalance(99));
        assertEquals(0, useCase.summarize(99).count());
    }

    @Test
    void deletingAnExpenseRaisesBalanceAgain() throws Exception {
        useCase.addIncome(USER, "Gaji", 100L);
        Transaction expense = useCase.addExpense(USER, "Kopi", 30L);
        assertEquals(70L, useCase.getBalance(USER));

        useCase.removeTransactionForUser(USER, expense.getId());
        assertEquals(100L, useCase.getBalance(USER));
    }

    @Test
    void monthlySummaryGroupsByMonthNewestFirst() {
        repository.save(new Transaction(0, USER, "Okt gaji", 100L, TransactionType.INCOME,
                null, LocalDateTime.of(2026, 10, 5, 9, 0)));
        repository.save(new Transaction(0, USER, "Nov gaji", 300L, TransactionType.INCOME,
                null, LocalDateTime.of(2026, 11, 2, 9, 0)));
        repository.save(new Transaction(0, USER, "Nov kopi", 50L, TransactionType.EXPENSE,
                null, LocalDateTime.of(2026, 11, 9, 9, 0)));

        List<MonthlySummary> months = useCase.monthlySummary(USER);

        assertEquals(2, months.size());
        assertEquals(YearMonth.of(2026, 11), months.get(0).month());
        assertEquals(2, months.get(0).summary().count());
        assertEquals(250L, months.get(0).summary().balance());
        assertEquals(YearMonth.of(2026, 10), months.get(1).month());
        assertEquals(100L, months.get(1).summary().balance());
        assertTrue(useCase.monthlySummary(99).isEmpty());
    }
}
