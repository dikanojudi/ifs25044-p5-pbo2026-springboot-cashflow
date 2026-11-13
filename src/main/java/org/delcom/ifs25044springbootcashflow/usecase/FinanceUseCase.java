package org.delcom.ifs25044springbootcashflow.usecase;

import org.delcom.ifs25044springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.MonthlySummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25044springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25044springbootcashflow.domain.repository.ITransactionRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O dan tidak mengenal komponen UI: hanya memproses data
 * dan melempar checked exception domain jika operasi tidak valid.
 * Semua operasi dibatasi per pengguna (scoped per user).
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi yang tersedia (khusus admin). */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Mengubah teks jumlah dari input menjadi angka bulat.
     * Gagal jika kosong atau bukan angka.
     */
    public long parseAmount(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
    }

    /** Menambahkan pemasukan baru. */
    public Transaction addIncome(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.INCOME);
    }

    /** Menambahkan pengeluaran baru. */
    public Transaction addExpense(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.EXPENSE);
    }

    /** Menambahkan transaksi baru setelah validasi. */
    public Transaction addTransaction(int userId, String description, long amount, TransactionType type)
            throws ValidationException {
        requireDescription(description);
        requirePositiveAmount(amount);
        if (type == null) {
            throw new ValidationException("Jenis transaksi tidak boleh kosong");
        }
        return transactionRepository.save(userId, description.trim(), amount, type);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException("Transaksi", id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /**
     * Mengubah keterangan, jumlah, jenis, dan/atau bukti transaksi milik pengguna.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     * Seluruh input divalidasi lebih dulu sehingga tidak ada perubahan sebagian.
     */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);

        if (description != null) {
            requireDescription(description);
        }
        if (amount != null) {
            requirePositiveAmount(amount);
        }

        if (description != null) {
            transaction.changeDescription(description.trim());
        }
        if (amount != null) {
            transaction.changeAmount(amount);
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(int userId, SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("Opsi pengurutan tidak valid");
        }
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Menghitung ringkasan keuangan (jumlah, pemasukan, pengeluaran) milik satu pengguna. */
    public BalanceSummary summarize(int userId) {
        return summarizeList(transactionRepository.findByUserId(userId));
    }

    /** Menghitung saldo pengguna: total pemasukan dikurangi total pengeluaran. */
    public long getBalance(int userId) {
        return summarize(userId).balance();
    }

    /** Ringkasan per bulan milik satu pengguna, bulan terbaru lebih dulu. */
    public List<MonthlySummary> monthlySummary(int userId) {
        Map<YearMonth, List<Transaction>> grouped = transactionRepository.findByUserId(userId).stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getCreatedAt())));
        return grouped.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<Transaction>>comparingByKey().reversed())
                .map(entry -> new MonthlySummary(entry.getKey(), summarizeList(entry.getValue())))
                .toList();
    }

    private static BalanceSummary summarizeList(List<Transaction> transactions) {
        long income = transactions.stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
        long expense = transactions.stream()
                .filter(transaction -> !transaction.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
        return new BalanceSummary(transactions.size(), income, expense);
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("Transaksi", id);
        }
        Transaction transaction = found.get();
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private static void requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
    }

    private static void requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
    }

    private static void requirePositiveAmount(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
    }
}
