package ues.edu.sv.proyect_bs;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

final class LoginController extends GeneralidadesViewController {
    private final AuthApiClient authApiClient = new AuthApiClient();
    private final SincronizacionLocal sincronizacionLocal;

    LoginController(GeneralidadesActivity host) {
        super(host);
        sincronizacionLocal = new SincronizacionLocal(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_login_settings).setOnClickListener(v ->
                host.showScreen(R.layout.general_configuracion));
        host.findViewById(R.id.general_login_forgot).setOnClickListener(v ->
                host.showScreen(R.layout.general_recuperar));
        host.findViewById(R.id.general_login_register).setOnClickListener(v ->
                host.showScreen(R.layout.general_registro));
        host.findViewById(R.id.general_login_submit).setOnClickListener(v -> login());
        configurePasswordField(R.id.general_login_password);
        loadSavedCredentials();
        applyTheme(host.findViewById(R.id.general_login_root));
    }

    private void loadSavedCredentials() {
        if (!SessionManager.shouldRememberLogin(host)) {
            return;
        }
        EditText email = host.findViewById(R.id.general_login_username);
        EditText password = host.findViewById(R.id.general_login_password);
        email.setText(SessionManager.getSavedEmail(host));
        password.setText(SessionManager.getSavedPassword(host));
    }

    private void login() {
        EditText email = host.findViewById(R.id.general_login_username);
        EditText password = host.findViewById(R.id.general_login_password);
        String correo = email.getText().toString().trim();
        String contrasena = password.getText().toString();
        if (correo.isEmpty()) {
            email.setError("Ingresa tu correo electrónico.");
            return;
        }
        if (contrasena.isEmpty()) {
            password.setError("Ingresa tu contraseña.");
            return;
        }

        View submit = host.findViewById(R.id.general_login_submit);
        submit.setEnabled(false);
        ((TextView) submit).setText("Conectando...");
        authApiClient.login(correo, contrasena, new AuthApiClient.LoginCallback() {
            @Override
            public void onSuccess(AuthApiClient.LoginResponse response) {
                submit.setEnabled(true);
                ((TextView) submit).setText("Iniciar sesión");
                SessionManager.save(host, response.token, response.tokenType, response.profile,
                        response.userId, response.personId);
                if (SessionManager.shouldRememberLogin(host)) {
                    SessionManager.saveLoginCredentials(host, correo, contrasena);
                } else {
                    SessionManager.clearSavedLogin(host);
                }
                sincronizacionLocal.hasLocalProfile(response.userId, response.personId,
                hasLocalProfile -> {
                    if (hasLocalProfile) {
                        openAuthenticatedModule(response);
                        sincronizacionLocal.sincronizar(response.token, (ok, failed) -> {
                            if (failed > 0) {
                                android.widget.Toast.makeText(host,
                                        "Sincronización incompleta: " + failed
                                                + " tablas pendientes.",
                                        android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        ((TextView) submit).setText("Preparando datos locales...");
                        sincronizacionLocal.sincronizar(response.token, (ok, failed) -> {
                            submit.setEnabled(true);
                            ((TextView) submit).setText("Iniciar sesión");
                            openAuthenticatedModule(response);
                            if (failed > 0) {
                                host.showLoginError("Sesión iniciada. "
                                        + failed + " tablas no se pudieron sincronizar.");
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                submit.setEnabled(true);
                ((TextView) submit).setText("Iniciar sesión");
                host.showLoginError(message);
            }
        });
    }

    private void openAuthenticatedModule(AuthApiClient.LoginResponse response) {
        if ("ADMIN".equalsIgnoreCase(response.profile)) {
            host.openAdminModule();
        } else if ("BARBERO".equalsIgnoreCase(response.profile)
                || "CLIENTE".equalsIgnoreCase(response.profile)) {
            host.openRoleModule(response.profile);
        } else {
            SessionManager.clear(host);
            host.showLoginError("El perfil recibido no tiene acceso habilitado.");
        }
    }
}
