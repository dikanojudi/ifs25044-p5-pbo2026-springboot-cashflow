package org.delcom.ifs25044springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Halaman publik yang menjelaskan aplikasi Cashflow (dapat dibuka tanpa login).
 */
@Route("about")
@PageTitle("Tentang Cashflow")
public class AboutView extends VerticalLayout {

    public AboutView() {
        setSizeFull();
        getElement().setAttribute("role", "main");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Tentang Cashflow");
        title.getStyle().set("margin", "0");

        Paragraph intro = new Paragraph(
                "Cashflow adalah aplikasi pencatat keuangan pribadi. Catat setiap pemasukan "
                        + "dan pengeluaran, lampirkan bukti transaksi, lalu pantau saldo Anda.");
        intro.getStyle().set("margin", "0.75rem 0 0 0").set("color", "#374151");

        Paragraph features = new Paragraph(
                "Fitur utama: pencarian dan pengurutan transaksi, ringkasan saldo, "
                        + "ringkasan bulanan, serta pengelolaan profil.");
        features.getStyle().set("margin", "0.5rem 0 0 0").set("color", "#374151");

        RouterLink login = new RouterLink("Masuk", LoginView.class);
        RouterLink register = new RouterLink("Daftar akun baru", RegisterView.class);
        for (RouterLink link : new RouterLink[]{login, register}) {
            link.getStyle()
                    .set("color", "#1D4ED8")
                    .set("font-weight", "600")
                    .set("cursor", "pointer");
        }
        HorizontalLayout links = new HorizontalLayout(login, register);
        links.setSpacing(true);
        links.getStyle().set("margin-top", "1rem");

        Div card = new Div(title, intro, features, links);
        card.getStyle()
                .set("width", "100%")
                .set("max-width", "560px")
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 8px 24px rgba(15, 23, 42, 0.08)")
                .set("box-sizing", "border-box");
        add(card);
    }
}
