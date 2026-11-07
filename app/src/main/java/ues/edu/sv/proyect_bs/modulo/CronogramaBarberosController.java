package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class CronogramaBarberosController implements AdminViewController {
    private final MainActivity host;

    CronogramaBarberosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_schedule).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
        host.configureSpinner(R.id.schedule_status_spinner, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        host.findViewById(R.id.schedule_barber_one).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cronograma_barbero));
        host.findViewById(R.id.schedule_barber_two).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cronograma_barbero));
        host.findViewById(R.id.schedule_barber_three).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cronograma_barbero));
        host.setDisabledCard(R.id.schedule_barber_two, -1);
    }
}
