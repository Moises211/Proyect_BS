package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class CitasController implements AdminViewController {
    private final MainActivity host;

    CitasController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        host.findViewById(R.id.action_add_appointment).setOnClickListener(v ->
                host.showAppointmentForm());
        host.configureSpinner(R.id.dropdown_appointment_status, Color.DKGRAY,
                "Historial", "Activas", "Canceladas");
        host.findViewById(R.id.appointment_item_one).setOnClickListener(v ->
                host.showAppointmentForm());
    }
}
