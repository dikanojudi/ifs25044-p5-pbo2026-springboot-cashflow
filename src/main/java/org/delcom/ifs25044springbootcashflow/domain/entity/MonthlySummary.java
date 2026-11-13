package org.delcom.ifs25044springbootcashflow.domain.entity;

import java.time.YearMonth;

/**
 * Ringkasan keuangan untuk satu bulan.
 *
 * @param month   bulan dan tahun
 * @param summary ringkasan transaksi pada bulan tersebut
 */
public record MonthlySummary(YearMonth month, BalanceSummary summary) {
}
