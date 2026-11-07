package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class ClientesController implements AdminViewController {
    private final MainActivity host;

    ClientesController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        host.findViewById(R.id.action_add_client).setOnClickListener(v ->
                host.showClientForm(false));
        host.configureSpinner(R.id.dropdown_client_status, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        host.configureClientActions(R.id.client_item_one);
        host.configureClientActions(R.id.client_item_two);
        host.configureClientActions(R.id.client_item_three);
    }
}
