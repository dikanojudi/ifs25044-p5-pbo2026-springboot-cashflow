package org.delcom.ifs25044springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi global untuk setiap halaman: indikator loading untuk tindakan yang
 * memerlukan waktu tunggu, serta atribut bahasa dan meta deskripsi dokumen
 * (dibutuhkan pemeriksaan aksesibilitas dan SEO).
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    static final String PAGE_DESCRIPTION =
            "Cashflow: aplikasi pencatat pemasukan, pengeluaran, dan saldo keuangan pribadi.";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);

            uiEvent.getUI().getPage().executeJs(
                    "document.documentElement.setAttribute('lang', 'id');"
                            + "if (!document.querySelector('meta[name=description]')) {"
                            + "  const meta = document.createElement('meta');"
                            + "  meta.name = 'description';"
                            + "  meta.content = $0;"
                            + "  document.head.appendChild(meta);"
                            + "}",
                    PAGE_DESCRIPTION);
        });
    }
}
