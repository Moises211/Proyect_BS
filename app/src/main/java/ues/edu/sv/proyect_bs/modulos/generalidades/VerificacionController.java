package ues.edu.sv.proyect_bs;

final class VerificacionController extends GeneralidadesViewController {
    VerificacionController(GeneralidadesActivity host) {
        super(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_verify_back).setOnClickListener(v ->
                host.showScreen(R.layout.general_recuperar));
        host.findViewById(R.id.general_verify_submit).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        host.findViewById(R.id.general_verify_resend).setOnClickListener(v -> {
            host.findViewById(R.id.general_code_three).requestFocus();
        });
        applyTheme(host.findViewById(R.id.general_verify_root));
    }
}
