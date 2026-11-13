package org.delcom.ifs25044springbootcashflow.adapter.presenter;

import org.delcom.ifs25044springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Format uang dan teks sengaja dipisah dari entity, use case, dan view
 * sehingga format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MM/yyyy");

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah: kecil ke besar",
            SortOption.AMOUNT_DESC, "Jumlah: besar ke kecil",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    );

    /** Memformat uang, contoh: {@code Rp 1,000,000.00} (negatif: {@code -Rp 25,000.00}). */
    public String formatMoney(long amount) {
        BigDecimal value = BigDecimal.valueOf(amount).abs().setScale(2);
        String prefix = amount < 0 ? "-Rp " : "Rp ";
        return prefix + String.format(Locale.US, "%,.2f", value);
    }

    /** Label jenis transaksi untuk ditampilkan. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label opsi pengurutan untuk ditampilkan. */
    public String sortLabel(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Memformat satu transaksi menjadi baris teks: {@code [id] keterangan | Rp ... | [Jenis]}. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat tanggal transaksi dibuat. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat bulan, contoh: {@code 11/2026}. */
    public String formatMonth(YearMonth month) {
        return month.format(MONTH_FORMAT);
    }

    /** Memformat saldo, contoh: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Memformat ringkasan keuangan menjadi satu baris teks. */
    public String formatSummary(BalanceSummary summary) {
        return String.format(
                "Transaksi: %d | Pemasukan: %s | Pengeluaran: %s | %s",
                summary.count(),
                formatMoney(summary.totalIncome()),
                formatMoney(summary.totalExpense()),
                formatBalance(summary.balance())
        );
    }

    /**
     * Memformat daftar transaksi menjadi teks multi-baris.
     * Mengembalikan pesan kosong jika list tidak berisi data.
     */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan sukses setelah mengubah transaksi. */
    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    /** Pesan gagal saat mengubah transaksi. */
    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
