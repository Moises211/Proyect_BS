package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class BarberosController implements AdminViewController {
    private final MainActivity host;

    BarberosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_barbers).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
        host.configureSpinner(R.id.barbers_status_spinner, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        host.findViewById(R.id.action_add_barber).setOnClickListener(v ->
                host.showBarberForm(false));
        host.configureBarberActions(R.id.barber_item_one);
        host.configureBarberActions(R.id.barber_item_two);
        host.configureBarberActions(R.id.barber_item_three);
        host.setDisabledCard(R.id.barber_item_two, R.id.barber_active_switch);
    }
}
