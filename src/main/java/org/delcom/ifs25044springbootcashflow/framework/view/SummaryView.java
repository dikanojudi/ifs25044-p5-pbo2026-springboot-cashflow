package org.delcom.ifs25044springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25044springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25044springbootcashflow.domain.entity.MonthlySummary;
import org.delcom.ifs25044springbootcashflow.domain.entity.User;
import org.delcom.ifs25044springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25044springbootcashflow.usecase.FinanceUseCase;

import java.util.List;

/**
 * Ringkasan keuangan per bulan milik pengguna yang sedang login.
 */
@Route(value = "summary", layout = MainLayout.class)
@PageTitle("Ringkasan Bulanan")
public class SummaryView extends VerticalLayout {

    public SummaryView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        setSizeFull();
        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        User user = AuthSession.currentUser().orElseThrow();

        H1 title = new H1("Ringkasan Bulanan");
        title.getStyle().set("margin", "0");

        Paragraph overall = new Paragraph(presenter.formatSummary(financeUseCase.summarize(user.getId())));
        overall.getStyle().set("margin", "0").set("color", "#1F2937").set("font-weight", "600");

        List<MonthlySummary> months = financeUseCase.monthlySummary(user.getId());
        Paragraph empty = new Paragraph("Belum ada transaksi untuk diringkas.");
        empty.getStyle().set("margin", "0").set("color", "#4B5563");
        empty.setVisible(months.isEmpty());

        Grid<MonthlySummary> grid = new Grid<>();
        grid.addColumn(item -> presenter.formatMonth(item.month())).setHeader("Bulan").setAutoWidth(true);
        grid.addColumn(item -> item.summary().count()).setHeader("Transaksi").setAutoWidth(true);
        grid.addColumn(item -> presenter.formatMoney(item.summary().totalIncome()))
                .setHeader("Pemasukan").setFlexGrow(1);
        grid.addColumn(item -> presenter.formatMoney(item.summary().totalExpense()))
                .setHeader("Pengeluaran").setFlexGrow(1);
        grid.addColumn(item -> presenter.formatMoney(item.summary().balance()))
                .setHeader("Saldo").setFlexGrow(1);
        grid.setItems(months);
        grid.setVisible(!months.isEmpty());
        grid.setSizeFull();
        grid.getElement().setAttribute("aria-label", "Ringkasan keuangan per bulan");
        GridStyles.apply(grid);

        Div card = PageCard.wrap(title, overall, empty, grid);
        add(card);
        expand(card);
    }
}
