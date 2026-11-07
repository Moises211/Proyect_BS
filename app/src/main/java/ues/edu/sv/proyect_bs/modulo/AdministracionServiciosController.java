package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class AdministracionServiciosController implements AdminViewController {
    private final MainActivity host;

    AdministracionServiciosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_services).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.findViewById(R.id.action_add_service).setOnClickListener(v ->
                host.showServiceForm());
        host.configureSpinner(R.id.service_status_spinner, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        host.configureServiceActions(R.id.service_item_one);
        host.configureServiceActions(R.id.service_item_two);
        host.configureServiceActions(R.id.service_item_three);
        host.setDisabledCard(R.id.service_item_three, R.id.service_active_switch);
    }
}
