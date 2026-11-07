package ues.edu.sv.proyect_bs;

final class CitasClienteController implements AdminViewController {
    private final MainActivity host;

    CitasClienteController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_clientes));
        host.findViewById(R.id.filter_active_appointments).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_citas_cliente));
        host.findViewById(R.id.filter_history_appointments).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_historial_citas_cliente));
        host.findViewById(R.id.action_change_appointment).setOnClickListener(v ->
                host.showAppointmentForm());
    }
}
