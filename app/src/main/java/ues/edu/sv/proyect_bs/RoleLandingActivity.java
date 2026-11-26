package ues.edu.sv.proyect_bs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ues.edu.sv.proyect_bs.modulo.barbero.BarberoHomeActivity;

public class RoleLandingActivity extends AppCompatActivity {
    private final AuthApiClient authApiClient = new AuthApiClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String profile = getIntent().getStringExtra("profile");

        // Redirigir al módulo real cuando esté disponible
        if ("BARBERO".equalsIgnoreCase(profile)) {
            startActivity(new Intent(this, BarberoHomeActivity.class));
            finish();
            return;
        }

        // Pantalla provisional para los demás roles
        setContentView(R.layout.pantalla_cliente_provisional);
        TextView profileText = findViewById(R.id.role_landing_profile);
        profileText.setText("Perfil: " + profile);
        MaterialButton logout = findViewById(R.id.role_landing_logout);
        logout.setOnClickListener(v -> logout());
    }

    private void logout() {
        String token = SessionManager.getToken(this);
        SessionManager.clear(this);
        authApiClient.logout(token, () -> {
            startActivity(new Intent(this, GeneralidadesActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        authApiClient.shutdown();
        super.onDestroy();
    }
}
