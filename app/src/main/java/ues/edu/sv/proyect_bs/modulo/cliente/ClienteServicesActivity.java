package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class ClienteServicesActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_services);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupNavigation();
        cargarServicios();
    }

    private void cargarServicios() {
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);
            List<Servicios> servicios = db.serviciosDAO().obtenerTodos().stream()
                    .filter(s -> Boolean.TRUE.equals(s.estado))
                    .collect(Collectors.toList());

            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.tab_servicios))
                        .setText("Servicios " + servicios.size());
                poblarServicios(servicios);
            });
        });
    }

    private void poblarServicios(List<Servicios> servicios) {
        LinearLayout container = findViewById(R.id.layout_servicios);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Servicios srv : servicios) {
            View item = inflater.inflate(R.layout.item_servicio_barbero, container, false);
            ((TextView) item.findViewById(R.id.tv_srv_nombre))
                    .setText(srv.nombre_servicio != null ? srv.nombre_servicio : "—");
            String dur    = srv.duracion != null ? srv.duracion + " min" : "—";
            String precio = srv.precio != null
                    ? " · $" + String.format(Locale.getDefault(), "%.0f", srv.precio) : "";
            ((TextView) item.findViewById(R.id.tv_srv_detalle)).setText(dur + precio);

            final long idservicio = srv.idservicio;
            item.setOnClickListener(v -> {
                Intent i = new Intent(this, ClienteBarberActivity.class);
                i.putExtra("idservicio", idservicio);
                startActivity(i);
            });
            container.addView(item);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            startActivity(new Intent(this, ClienteHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_citas).setOnClickListener(v -> {
            startActivity(new Intent(this, ClienteAppointmentsActivity.class));
            finish();
        });
        findViewById(R.id.nav_servicios).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientePromocionesActivity.class));
            finish();
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientePerfilActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
