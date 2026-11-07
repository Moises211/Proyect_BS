package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class HistorialCitasClienteController implements AdminViewController {
    private final MainActivity host;

    HistorialCitasClienteController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_history_appointments).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_citas_cliente));
        host.configureSpinner(R.id.history_all_dropdown, Color.DKGRAY,
                "Todas", "Activas", "Canceladas");
    }
}
