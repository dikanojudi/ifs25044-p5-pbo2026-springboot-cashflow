package org.delcom.ifs25044springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;

/**
 * Halaman 404 kustom: memiliki judul, heading level satu, dan landmark utama
 * sehingga lolos pemeriksaan aksesibilitas.
 */
@PageTitle("Halaman Tidak Ditemukan")
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        removeAll();
        setSizeFull();
        getElement().setAttribute("role", "main");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Halaman tidak ditemukan");
        title.getStyle().set("margin", "0");

        Paragraph message = new Paragraph("Alamat /" + event.getLocation().getPath() + " tidak tersedia.");
        message.getStyle().set("margin", "0.75rem 0 0 0").set("color", "#374151");

        RouterLink home = new RouterLink("Kembali ke beranda", FinanceView.class);
        home.getStyle()
                .set("color", "#1D4ED8")
                .set("font-weight", "600")
                .set("display", "inline-block")
                .set("margin-top", "1rem")
                .set("cursor", "pointer");

        Div card = new Div(title, message, home);
        card.getStyle()
                .set("width", "100%")
                .set("max-width", "560px")
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-sizing", "border-box");
        add(card);
        return 404;
    }
}
