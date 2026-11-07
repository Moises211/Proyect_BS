package ues.edu.sv.proyect_bs;

final class BeneficiosController implements AdminViewController {
    private final MainActivity host;

    BeneficiosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        host.findViewById(R.id.benefit_client_one).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios_cliente));
        host.findViewById(R.id.benefit_client_two).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios_cliente));
        host.findViewById(R.id.benefit_client_three).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios_cliente));
    }
}
