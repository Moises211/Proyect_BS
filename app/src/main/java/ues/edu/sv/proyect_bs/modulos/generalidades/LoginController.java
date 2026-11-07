package ues.edu.sv.proyect_bs;

final class LoginController extends GeneralidadesViewController {
    LoginController(GeneralidadesActivity host) {
        super(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_login_settings).setOnClickListener(v ->
                host.showScreen(R.layout.general_configuracion));
        host.findViewById(R.id.general_login_forgot).setOnClickListener(v ->
                host.showScreen(R.layout.general_recuperar));
        host.findViewById(R.id.general_login_register).setOnClickListener(v ->
                host.showScreen(R.layout.general_registro));
        host.findViewById(R.id.general_login_submit).setOnClickListener(v ->
                host.openAdminModule());
        configurePasswordField(R.id.general_login_password);
        applyTheme(host.findViewById(R.id.general_login_root));
    }
}
