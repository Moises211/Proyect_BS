package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class ClienteBeneficiosActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int puntosDisponibles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_beneficios);
        setupNavigation();
        cargarDatos();
        if (findViewById(R.id.btn_back) != null)
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);
            long clienteId = db.clienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idpersona != null && c.idpersona == personId)
                    .mapToLong(c -> c.idcliente).findFirst().orElse(-1L);

            PuntosCliente pts = db.puntosClienteDAO().obtenerPorId(clienteId);
            int puntos = (pts != null && pts.cantidadpuntos != null) ? pts.cantidadpuntos : 0;

            List<Beneficios> beneficios = db.beneficiosDAO().obtenerTodos().stream()
                    .filter(b -> Boolean.TRUE.equals(b.estado))
                    .collect(java.util.stream.Collectors.toList());

            final int puntosF = puntos;
            runOnUiThread(() -> poblarUI(puntosF, beneficios, db));
        });
    }

    private void poblarUI(int puntos, List<Beneficios> beneficios, AppDB db) {
        puntosDisponibles = puntos;
        ((TextView) findViewById(R.id.tv_puntos_disponibles)).setText(String.valueOf(puntos));

        LinearLayout container = findViewById(R.id.layout_beneficios);
        container.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        for (Beneficios b : beneficios) {
            View item = inf.inflate(R.layout.item_beneficio_cliente, container, false);

            // Nombre desde el servicio asociado
            String nombre = "Beneficio";
            if (b.servicio != null) {
                Servicios srv = db.serviciosDAO().obtenerPorId(b.servicio);
                if (srv != null) nombre = srv.nombre_servicio;
            }
            ((TextView) item.findViewById(R.id.tv_ben_nombre)).setText(nombre);

            int costo = b.costepuntos != null ? b.costepuntos : 0;
            ((TextView) item.findViewById(R.id.tv_ben_puntos)).setText(costo + " puntos requeridos");

            TextView badge = item.findViewById(R.id.tv_ben_badge);
            if (puntos >= costo) {
                badge.setText("Canj");
                badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_teal));
                badge.setTextColor(0xFF0F5159);
            } else {
                badge.setText("Falta");
                badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_en_atencion));
                badge.setTextColor(0xFF92400E);
            }
            container.addView(item);
        }

        if (beneficios.isEmpty()) {
            findViewById(R.id.layout_sin_beneficios).setVisibility(View.VISIBLE);
        }
    }

    private void setupNavigation() {
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
            findViewById(R.id.nav_perfil).setOnClickListener(v -> {
                startActivity(new Intent(this, ClientePerfilActivity.class));
                finish();
            });
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
