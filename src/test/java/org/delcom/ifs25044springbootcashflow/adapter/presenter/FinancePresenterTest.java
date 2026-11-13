package org.delcom.ifs25044springbootcashflow.adapter.presenter;

import org.delcom.ifs25044springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private Transaction income() {
        return new Transaction(1, 1, "Gaji", 1_000_000L, TransactionType.INCOME,
                null, LocalDateTime.of(2026, 11, 13, 9, 5));
    }

    private Transaction expense() {
        return new Transaction(2, 1, "Kopi", 25_000L, TransactionType.EXPENSE,
                null, LocalDateTime.of(2026, 11, 13, 10, 0));
    }

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatMoney(0L));
        assertEquals("Rp 4,975,000.00", presenter.formatMoney(4_975_000L));
        assertEquals("-Rp 25,000.00", presenter.formatMoney(-25_000L));
    }

    @Test
    void formatTransactionMatchesReferenceLayout() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income()));
        assertEquals("[2] Kopi | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense()));
    }

    @Test
    void formatTypeAndSortLabels() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));
        for (SortOption option : SortOption.values()) {
            assertNotNull(presenter.sortLabel(option));
        }
    }

    @Test
    void formatDatesAndBalance() {
        assertEquals("13/11/2026 09:05", presenter.formatCreatedAt(income()));
        assertEquals("11/2026", presenter.formatMonth(YearMonth.of(2026, 11)));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000L));
    }

    @Test
    void formatSummaryCombinesAllTotals() {
        String text = presenter.formatSummary(new BalanceSummary(2, 5_000_000L, 25_000L));
        assertEquals(
                "Transaksi: 2 | Pemasukan: Rp 5,000,000.00 | Pengeluaran: Rp 25,000.00 | Saldo: Rp 4,975,000.00",
                text);
    }

    @Test
    void listsHandleEmptyAndFilledData() {
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!",
                presenter.showTransactions(List.of()));
        assertEquals("Daftar Transaksi:\n[1] Gaji | Rp 1,000,000.00 | [Pemasukan]\n"
                        + "[2] Kopi | Rp 25,000.00 | [Pengeluaran]",
                presenter.showTransactions(List.of(income(), expense())));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "x"));
        assertEquals("Daftar Transaksi (Terurut):\n- Data transaksi belum tersedia!",
                presenter.showSortedTransactions(List.of()));
    }

    @Test
    void messages() {
        assertEquals("Berhasil menambah transaksi: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income()));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 5.", presenter.showUpdateFailed(5));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] salah", presenter.showValidationError("salah"));
    }
}
