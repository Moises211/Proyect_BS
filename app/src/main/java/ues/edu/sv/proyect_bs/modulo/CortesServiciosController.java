package ues.edu.sv.proyect_bs;

import com.google.android.material.appbar.MaterialToolbar;

final class CortesServiciosController implements AdminViewController {
    private final MainActivity host;

    CortesServiciosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        MaterialToolbar toolbar = host.findViewById(R.id.toolbar_admin_menu);
        toolbar.setSubtitle("Cortes y Servicios");
        host.getNavigationController().configureToolbar(R.id.toolbar_admin_menu);
        host.findViewById(R.id.card_admin_services).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_administracion_servicios));
        host.findViewById(R.id.card_service_hours).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_horarios_servicio));
        host.findViewById(R.id.card_promotions).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_promociones));
        host.findViewById(R.id.card_admin_benefits).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios_admin));
    }
}
