package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
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

    private static class BeneficioUI {
        String nombre;
        int costepuntos;
    }

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

            // Resolver nombres de servicio en background
            List<BeneficioUI> items = new ArrayList<>();
            for (Beneficios b : beneficios) {
                BeneficioUI ui = new BeneficioUI();
                ui.costepuntos = b.costepuntos != null ? b.costepuntos : 0;
                ui.nombre = "Beneficio";
                if (b.servicio != null) {
                    Servicios srv = db.serviciosDAO().obtenerPorId(b.servicio);
                    if (srv != null && srv.nombre_servicio != null) ui.nombre = srv.nombre_servicio;
                }
                items.add(ui);
            }

            final int puntosF = puntos;
            runOnUiThread(() -> poblarUI(puntosF, items));
        });
    }

    private void poblarUI(int puntos, List<BeneficioUI> items) {
        puntosDisponibles = puntos;
        ((TextView) findViewById(R.id.tv_puntos_disponibles)).setText(String.valueOf(puntos));

        LinearLayout container = findViewById(R.id.layout_beneficios);
        container.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        for (BeneficioUI b : items) {
            View item = inf.inflate(R.layout.item_beneficio_cliente, container, false);

            ((TextView) item.findViewById(R.id.tv_ben_nombre)).setText(b.nombre);
            ((TextView) item.findViewById(R.id.tv_ben_puntos)).setText(b.costepuntos + " puntos requeridos");

            TextView badge = item.findViewById(R.id.tv_ben_badge);
            if (puntos >= b.costepuntos) {
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

        if (items.isEmpty()) {
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
