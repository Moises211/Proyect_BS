package ues.edu.sv.proyect_bs;

final class PantallaInicialController implements AdminViewController {
    private final MainActivity host;

    PantallaInicialController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.getNavigationController().configureToolbar(R.id.admin_top_app_bar);
        host.findViewById(R.id.module_cuts_services).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.findViewById(R.id.module_client_control).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        host.findViewById(R.id.module_user_profile).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_perfil_usuario));
        host.findViewById(R.id.module_staff_shifts).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
    }
}
