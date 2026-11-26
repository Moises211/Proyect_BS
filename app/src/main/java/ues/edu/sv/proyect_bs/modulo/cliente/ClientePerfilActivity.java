package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Persona;

public class ClientePerfilActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_perfil);
        setupNavigation();
        cargarDatos();
    }

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);
            Persona persona = db.personaDAO().obtenerPorId(personId);

            String nombre  = persona != null ? persona.nombres + " " + persona.apellidos : "Cliente";
            String iniciales = obtenerIniciales(nombre);
            String correo  = persona != null && persona.correo_electronico != null
                    ? persona.correo_electronico : "—";

            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.tv_avatar_iniciales)).setText(iniciales);
                ((TextView) findViewById(R.id.tv_perfil_nombre)).setText(nombre);
                ((TextView) findViewById(R.id.tv_perfil_correo)).setText(correo);
            });
        });
    }

    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "?";
        String[] partes = nombre.trim().split(" ");
        if (partes.length >= 2)
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        return String.valueOf(nombre.charAt(0)).toUpperCase();
    }

    private void setupNavigation() {
        // Cerrar sesión
        findViewById(R.id.btn_cerrar_sesion).setOnClickListener(v -> cerrarSesion());

        // Nav
        if (findViewById(R.id.nav_inicio) != null)
            findViewById(R.id.nav_inicio).setOnClickListener(v -> {
                startActivity(new Intent(this, ClienteHomeActivity.class));
                finish();
            });
        if (findViewById(R.id.nav_citas) != null)
            findViewById(R.id.nav_citas).setOnClickListener(v -> {
                startActivity(new Intent(this, ClienteAppointmentsActivity.class));
                finish();
            });
        if (findViewById(R.id.nav_servicios) != null)
            findViewById(R.id.nav_servicios).setOnClickListener(v -> {
                startActivity(new Intent(this, ClientePromocionesActivity.class));
                finish();
            });
        if (findViewById(R.id.nav_perfil) != null)
            findViewById(R.id.nav_perfil).setOnClickListener(v -> {});
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
