package org.delcom.ifs25044springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceSummaryTest {

    @Test
    void balanceIsIncomeMinusExpense() {
        BalanceSummary summary = new BalanceSummary(3, 5_000_000L, 25_000L);
        assertEquals(3, summary.count());
        assertEquals(5_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());
        assertEquals(4_975_000L, summary.balance());
    }

    @Test
    void balanceCanBeNegative() {
        assertEquals(-50L, new BalanceSummary(1, 0L, 50L).balance());
    }

    @Test
    void monthlySummaryHoldsMonthAndSummary() {
        BalanceSummary summary = new BalanceSummary(1, 10L, 0L);
        MonthlySummary monthly = new MonthlySummary(YearMonth.of(2026, 11), summary);
        assertEquals(YearMonth.of(2026, 11), monthly.month());
        assertEquals(summary, monthly.summary());
    }
}
