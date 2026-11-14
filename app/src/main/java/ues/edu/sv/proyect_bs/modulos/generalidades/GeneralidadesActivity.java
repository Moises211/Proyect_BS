package ues.edu.sv.proyect_bs;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GeneralidadesActivity extends AppCompatActivity {
    private boolean darkMode;
    private int currentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        darkMode = getSharedPreferences("app_preferences", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        setTheme(darkMode ? R.style.Theme_GeneralidadesDark : R.style.Theme_GeneralidadesLight);
        super.onCreate(savedInstanceState);
        int initialLayout = savedInstanceState == null
                ? R.layout.general_login
                : savedInstanceState.getInt("general_current_layout", R.layout.general_login);
        showScreen(initialLayout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("general_current_layout", currentLayout);
        super.onSaveInstanceState(outState);
    }

    void showScreen(int layout) {
        currentLayout = layout;
        setContentView(layout);
        createController(layout).configure();
    }

    void setDarkMode(boolean enabled) {
        darkMode = enabled;
        getSharedPreferences("app_preferences", MODE_PRIVATE).edit()
                .putBoolean("dark_mode", enabled)
                .apply();
        recreate();
    }

    void openAdminModule() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    void openRoleModule(String profile) {
        Intent intent = new Intent(this, RoleLandingActivity.class);
        intent.putExtra("profile", profile);
        startActivity(intent);
        finish();
    }

    void showLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    void showRegistrationError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    boolean isDarkMode() {
        return darkMode;
    }

    int getCurrentLayout() {
        return currentLayout;
    }

    private GeneralidadesViewController createController(int layout) {
        if (layout == R.layout.general_login) {
            return new LoginController(this);
        }
        if (layout == R.layout.general_registro) {
            return new RegistroController(this);
        }
        if (layout == R.layout.general_recuperar) {
            return new RecuperarController(this);
        }
        if (layout == R.layout.general_verificacion) {
            return new VerificacionController(this);
        }
        if (layout == R.layout.general_configuracion) {
            return new ConfiguracionController(this);
        }
        throw new IllegalArgumentException("Layout de Generalidades no registrado: " + layout);
    }
}
