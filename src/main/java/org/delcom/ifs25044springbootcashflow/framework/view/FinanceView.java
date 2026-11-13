package org.delcom.ifs25044springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25044springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25044springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25044springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25044springbootcashflow.domain.entity.User;
import org.delcom.ifs25044springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25044springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25044springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tampilan web Vaadin pencatatan keuangan milik pengguna yang sedang login.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow Saya")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_MESSAGE = "- Data transaksi belum tersedia!";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>("Urutkan");
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_MESSAGE);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceTotal = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter, FileStorage fileStorage) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Cashflow Saya");
        title.getStyle().set("margin", "0");

        configureGrid();
        configureToolbar();

        Div card = PageCard.wrap(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser().orElseThrow();
    }

    // ------------------------------------------------------------------
    // Kartu ringkasan: total, pemasukan, pengeluaran, saldo
    // ------------------------------------------------------------------

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E3A8A"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#14532D"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#7F1D1D"),
                createSummaryCard("Saldo", balanceTotal, "#FEF3C7", "#D97706", "#78350F")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String textColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("color", textColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", textColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        BalanceSummary summary = financeUseCase.summarize(requireUser().getId());
        totalCount.setText(String.valueOf(summary.count()));
        incomeTotal.setText(presenter.formatMoney(summary.totalIncome()));
        expenseTotal.setText(presenter.formatMoney(summary.totalExpense()));
        balanceTotal.setText(presenter.formatMoney(summary.balance()));
    }

    // ------------------------------------------------------------------
    // Grid
    // ------------------------------------------------------------------

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addComponentColumn(this::createProofPreview)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        grid.getElement().setAttribute("aria-label", "Daftar transaksi keuangan");
        GridStyles.apply(grid);
    }

    private Component createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? "#14532D" : "#7F1D1D");
        return badge;
    }

    private Component createProofPreview(Transaction transaction) {
        if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi " + transaction.getId(),
                "48px"
        );
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");
        deleteButton.getElement().setAttribute("aria-label", "Hapus transaksi " + transaction.getId());
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------
    // Toolbar
    // ------------------------------------------------------------------

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.getElement().setAttribute("aria-label", "Cari keterangan transaksi");

        sortBox.setItems(List.of(SortOption.values()));
        sortBox.setItemLabelGenerator(presenter::sortLabel);
        sortBox.setPlaceholder("Pilih urutan");
        sortBox.setClearButtonVisible(true);
        sortBox.addValueChangeListener(event -> applyFilters());
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Pemasukan", VaadinIcon.PLUS.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        incomeButton.getStyle().set("cursor", "pointer");

        Button expenseButton = new Button("Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        expenseButton.getStyle().set("cursor", "pointer");

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> applyFilters());
        searchButton.getStyle().set("cursor", "pointer");

        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());
        refreshButton.getStyle().set("cursor", "pointer");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                sortBox,
                searchButton,
                refreshButton,
                incomeButton,
                expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        return toolbar;
    }

    // ------------------------------------------------------------------
    // Dialog tambah, ubah, hapus
    // ------------------------------------------------------------------

    private void openAddDialog(TransactionType type) {
        String typeLabel = presenter.formatType(type);

        TextField descriptionField = new TextField("Keterangan");
        TextField amountField = amountField();
        descriptionField.setWidthFull();

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = new Span("Bukti (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                long amount = financeUseCase.parseAmount(amountField.getValue());
                Transaction created = financeUseCase.addTransaction(
                        user.getId(), descriptionField.getValue(), amount, type);
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    String path = fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
                    financeUseCase.updateTransactionForUser(
                            user.getId(), created.getId(), null, null, null, path, false);
                }
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Tambah " + typeLabel, loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setWidthFull();

        TextField amountField = amountField();
        amountField.setValue(String.valueOf(transaction.getAmount()));

        ComboBox<TransactionType> typeBox = new ComboBox<>("Tipe");
        typeBox.setItems(List.of(TransactionType.values()));
        typeBox.setItemLabelGenerator(presenter::formatType);
        typeBox.setValue(transaction.getType());
        typeBox.setAllowCustomValue(false);
        typeBox.setWidthFull();

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = new Span("Ganti bukti (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                long amount = financeUseCase.parseAmount(amountField.getValue());
                String proofPath = null;
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    proofPath = fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
                }
                financeUseCase.updateTransactionForUser(
                        user.getId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        amount,
                        typeBox.getValue(),
                        proofPath,
                        false
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, typeBox, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        buildDialog(dialog, "Mengubah Transaksi #" + transaction.getId(), loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (" + presenter.formatMoney(transaction.getAmount()) + ", ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", loadingBar, message, cancelButton, confirmButton);
        dialog.open();
    }

    private TextField amountField() {
        TextField field = new TextField("Jumlah (Rp)");
        field.setWidthFull();
        field.setPrefixComponent(new Span("Rp"));
        field.setHelperText("Angka bulat tanpa titik atau koma, contoh: 1000000");
        field.setAllowedCharPattern("[0-9]");
        return field;
    }

    /** Semua dialog memakai header primer (#2563EB) dan footer abu-abu (#F3F4F6) dengan tombol di kanan. */
    private void buildDialog(
            Dialog dialog,
            String title,
            ProgressBar loadingBar,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("finance-form-dialog");

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", "#2563EB")
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        loadingBar.setIndeterminate(true);
        loadingBar.getStyle().set("margin", "0").set("height", "4px");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        for (Button btn : footerButtons) {
            btn.getStyle().set("cursor", "pointer");
        }
        footer.add(footerButtons);

        Div shell = new Div(header, loadingBar, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }

    // ------------------------------------------------------------------
    // Aksi dan pemuatan data
    // ------------------------------------------------------------------

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransactionForUser(requireUser().getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    /** Menerapkan kata kunci pencarian dan opsi pengurutan yang sedang dipilih. */
    private void applyFilters() {
        int userId = requireUser().getId();
        String keyword = searchField.getValue();
        boolean searching = keyword != null && !keyword.isBlank();

        List<Transaction> result = searching
                ? financeUseCase.searchTransactions(userId, keyword)
                : financeUseCase.getTransactionsByUser(userId);

        SortOption option = sortBox.getValue();
        if (option != null) {
            result = result.stream().sorted(option.comparator()).toList();
        }

        emptyState.setText(searching ? "- Transaksi tidak ditemukan!" : EMPTY_MESSAGE);
        refreshGrid(result, searching ? "Hasil Pencarian: \"" + keyword + "\"" : "Daftar Transaksi");
    }

    private void refreshAll() {
        searchField.clear();
        applyFilters();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        return upload;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
