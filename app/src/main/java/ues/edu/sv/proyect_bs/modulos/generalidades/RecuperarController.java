package ues.edu.sv.proyect_bs;

final class RecuperarController extends GeneralidadesViewController {
    RecuperarController(GeneralidadesActivity host) {
        super(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_recover_back).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        host.findViewById(R.id.general_recover_login).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        host.findViewById(R.id.general_recover_submit).setOnClickListener(v ->
                host.showScreen(R.layout.general_verificacion));
        applyTheme(host.findViewById(R.id.general_recover_root));
    }
}
