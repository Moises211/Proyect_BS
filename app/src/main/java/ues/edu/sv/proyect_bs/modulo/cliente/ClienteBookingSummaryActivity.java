package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionBase;

public class ClienteBookingSummaryActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long   idservicio;
    private long   idbarbero;
    private String nombreServicio;
    private String nombreBarbero;
    private int    duracion;
    private double precio;

    // Horario cargado de la BD
    private String fechaReserva = "";
    private String horaReserva  = "09:00:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_booking_summary);

        idservicio     = getIntent().getLongExtra("idservicio", -1);
        idbarbero      = getIntent().getLongExtra("idbarbero", -1);
        nombreServicio = getIntent().getStringExtra("nombreServicio");
        nombreBarbero  = getIntent().getStringExtra("nombreBarbero");
        duracion       = getIntent().getIntExtra("duracion", 0);
        precio         = getIntent().getDoubleExtra("precio", 0.0);

        if (findViewById(R.id.btn_back) != null)
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        poblarUI();
        cargarHorario();

        findViewById(R.id.btn_confirmar).setOnClickListener(v -> confirmarReserva());
    }

    private void poblarUI() {
        String detSrv = duracion + " min · $" + String.format(Locale.getDefault(), "%.0f", precio);
        ((TextView) findViewById(R.id.tv_srv_nombre)).setText(nombreServicio != null ? nombreServicio : "—");
        ((TextView) findViewById(R.id.tv_srv_detalle)).setText(detSrv);
        ((TextView) findViewById(R.id.tv_barbero_nombre)).setText(nombreBarbero != null ? nombreBarbero : "—");
    }

    private void cargarHorario() {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            CronogramaBarbero crono = db.cronogramaBarberoDAO().obtenerTodos().stream()
                    .filter(c -> c.idbarbero == idbarbero && c.fecha != null && c.fecha.compareTo(hoy) >= 0)
                    .min((a, b) -> a.fecha.compareTo(b.fecha))
                    .orElse(null);

            String apertura = "09:00";
            String fecha    = hoy;

            if (crono != null) {
                fecha = crono.fecha;
                HorarioAtencionBase base = db.horarioAtencionBaseDAO().obtenerTodos().stream()
                        .filter(h -> h.idhorarioatencionbase == crono.idhorario)
                        .findFirst().orElse(null);
                if (base != null && base.apertura != null)
                    apertura = base.apertura.length() >= 5 ? base.apertura.substring(0, 5) : base.apertura;
            }

            fechaReserva = fecha;
            horaReserva  = apertura + ":00";

            final String texto = fecha + " - " + apertura + " a.m.";
            runOnUiThread(() -> ((TextView) findViewById(R.id.tv_horario)).setText(texto));
        });
    }

    private void confirmarReserva() {
        long personId = SessionManager.getPersonId(this);
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            long clienteId = db.clienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idpersona != null && c.idpersona == personId)
                    .mapToLong(c -> c.idcliente).findFirst().orElse(-1L);

            if (clienteId == -1) {
                runOnUiThread(() -> Toast.makeText(this,
                        "No se encontró tu perfil de cliente.", Toast.LENGTH_SHORT).show());
                return;
            }

            long maxId = db.citasClienteDAO().obtenerTodos().stream()
                    .mapToLong(c -> c.idcitas).max().orElse(0L);

            CitasCliente nueva = new CitasCliente();
            nueva.idcitas    = maxId + 1;
            nueva.fecha      = fechaReserva.isEmpty()
                    ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
                    : fechaReserva;
            nueva.hora       = horaReserva;
            nueva.estado     = "PENDIENTE";
            nueva.idcliente  = clienteId;
            nueva.idbarbero  = idbarbero;
            nueva.idservicio = idservicio;

            db.citasClienteDAO().insertar(nueva);

            runOnUiThread(() -> {
                Toast.makeText(this, "¡Cita confirmada!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, ClienteAppointmentsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
