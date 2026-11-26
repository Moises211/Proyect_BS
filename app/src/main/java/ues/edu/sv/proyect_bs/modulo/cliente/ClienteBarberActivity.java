package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import ues.edu.sv.proyect_bs.modelos.Persona;

public class ClienteBarberActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long   idservicio;
    private String nombreServicio;
    private int    duracion;
    private double precio;

    private static class BarberoUI {
        long idbarbero;
        String nombre;
        boolean dispHoy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_barber);
        idservicio     = getIntent().getLongExtra("idservicio", -1);
        nombreServicio = getIntent().getStringExtra("nombreServicio");
        duracion       = getIntent().getIntExtra("duracion", 0);
        precio         = getIntent().getDoubleExtra("precio", 0.0);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupNavigation();
        cargarBarberos();
    }

    private void cargarBarberos() {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);
            List<Barberos> barberos = db.barberosDAO().obtenerTodos().stream()
                    .filter(b -> Boolean.TRUE.equals(b.estado))
                    .collect(Collectors.toList());
            List<CronogramaBarbero> cronogramas = db.cronogramaBarberoDAO().obtenerTodos();

            List<BarberoUI> items = new ArrayList<>();
            for (Barberos barb : barberos) {
                BarberoUI ui = new BarberoUI();
                ui.idbarbero = barb.idbarberos;
                Persona persona = (barb.idpersona != null)
                        ? db.personaDAO().obtenerPorId(barb.idpersona) : null;
                ui.nombre = (persona != null)
                        ? persona.nombres + " " + persona.apellidos : "Barbero";
                ui.dispHoy = cronogramas.stream()
                        .anyMatch(c -> c.idbarbero == barb.idbarberos && hoy.equals(c.fecha));
                items.add(ui);
            }

            runOnUiThread(() -> poblarBarberos(items));
        });
    }

    private void poblarBarberos(List<BarberoUI> items) {
        LinearLayout container = findViewById(R.id.layout_barberos);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (BarberoUI barb : items) {
            View item = inflater.inflate(R.layout.item_barbero_cliente, container, false);

            ((TextView) item.findViewById(R.id.tv_barbero_nombre)).setText(barb.nombre);

            String detalle = barb.dispHoy ? "Disponible hoy" : "Disponible mañana";
            ((TextView) item.findViewById(R.id.tv_barbero_detalle)).setText(detalle);

            TextView tvDisp = item.findViewById(R.id.tv_barbero_disp);
            tvDisp.setText(barb.dispHoy ? "Disp" : "Mañ");

            final long idbarbero = barb.idbarbero;
            final long idservicioF = idservicio;
            final String nombSrv = nombreServicio;
            final String nombBarb = barb.nombre;
            final int dur = duracion;
            final double prec = precio;
            item.setOnClickListener(v -> {
                Intent i = new Intent(this, ClienteBookingSummaryActivity.class);
                i.putExtra("idservicio", idservicioF);
                i.putExtra("idbarbero", idbarbero);
                i.putExtra("nombreServicio", nombSrv);
                i.putExtra("nombreBarbero", nombBarb);
                i.putExtra("duracion", dur);
                i.putExtra("precio", prec);
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
