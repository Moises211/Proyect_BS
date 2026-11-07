package ues.edu.sv.proyect_bs;

final class ControlCitasController implements AdminViewController {
    private final MainActivity host;

    ControlCitasController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.getNavigationController().configureToolbar(R.id.toolbar_control_citas);
        host.findViewById(R.id.card_client_directory).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_clientes));
        host.findViewById(R.id.card_benefits).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios));
    }
}
