package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
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
    private long idservicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_barber);
        idservicio = getIntent().getLongExtra("idservicio", -1);
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

            runOnUiThread(() -> poblarBarberos(barberos, cronogramas, hoy, db));
        });
    }

    private void poblarBarberos(List<Barberos> barberos, List<CronogramaBarbero> cronogramas,
                                String hoy, AppDB db) {
        LinearLayout container = findViewById(R.id.layout_barberos);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Barberos barb : barberos) {
            View item = inflater.inflate(R.layout.item_barbero_cliente, container, false);

            Persona persona = (barb.idpersona != null)
                    ? db.personaDAO().obtenerPorId(barb.idpersona) : null;
            String nombre = (persona != null)
                    ? persona.nombres + " " + persona.apellidos : "Barbero";

            ((TextView) item.findViewById(R.id.tv_barbero_nombre)).setText(nombre);

            // Disponibilidad hoy
            boolean dispHoy = cronogramas.stream()
                    .anyMatch(c -> c.idbarbero == barb.idbarberos && hoy.equals(c.fecha));

            String detalle = dispHoy ? "Disponible hoy" : "Disponible mañana";
            ((TextView) item.findViewById(R.id.tv_barbero_detalle)).setText(detalle);

            TextView tvDisp = item.findViewById(R.id.tv_barbero_disp);
            if (dispHoy) {
                tvDisp.setText("Disp");
            } else {
                tvDisp.setText("Mañ");
            }

            final long idbarbero = barb.idbarberos;
            final long idservicioF = idservicio;
            item.setOnClickListener(v -> {
                Intent i = new Intent(this, ClienteBookingSummaryActivity.class);
                i.putExtra("idservicio", idservicioF);
                i.putExtra("idbarbero", idbarbero);
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
