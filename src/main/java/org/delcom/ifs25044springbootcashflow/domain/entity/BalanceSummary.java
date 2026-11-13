package org.delcom.ifs25044springbootcashflow.domain.entity;

/**
 * Ringkasan keuangan (value object): jumlah transaksi, total pemasukan, dan total pengeluaran.
 *
 * @param count        banyak transaksi
 * @param totalIncome  total seluruh pemasukan
 * @param totalExpense total seluruh pengeluaran
 */
public record BalanceSummary(int count, long totalIncome, long totalExpense) {

    /** Saldo = total pemasukan dikurangi total pengeluaran. */
    public long balance() {
        return totalIncome - totalExpense;
    }
}
