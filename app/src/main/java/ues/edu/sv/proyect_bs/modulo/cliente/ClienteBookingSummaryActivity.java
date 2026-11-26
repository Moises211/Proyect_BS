package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionBase;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class ClienteBookingSummaryActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long idservicio;
    private long idbarbero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_booking_summary);

        idservicio = getIntent().getLongExtra("idservicio", -1);
        idbarbero  = getIntent().getLongExtra("idbarbero", -1);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupNavigation();
        cargarResumen();

        findViewById(R.id.btn_confirmar).setOnClickListener(v -> confirmarReserva());
    }

    private void cargarResumen() {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            Servicios srv = db.serviciosDAO().obtenerPorId(idservicio);
            Barberos barb = db.barberosDAO().obtenerTodos().stream()
                    .filter(b -> b.idbarberos == idbarbero)
                    .findFirst().orElse(null);

            String nombreSrv  = (srv != null) ? srv.nombre_servicio : "Servicio";
            String detalleSrv = "";
            if (srv != null) {
                String dur    = srv.duracion != null ? srv.duracion + " min" : "";
                String precio = srv.precio   != null
                        ? " · $" + String.format(Locale.getDefault(), "%.0f", srv.precio) : "";
                detalleSrv = dur + precio;
            }

            String nombreBarb   = "Barbero";
            String detalleBarb  = "";
            if (barb != null && barb.idpersona != null) {
                Persona pb = db.personaDAO().obtenerPorId(barb.idpersona);
                nombreBarb = (pb != null) ? pb.nombres + " " + pb.apellidos : "Barbero";
            }

            // Horario disponible: buscar apertura del horario base del barbero para hoy o próximo día
            CronogramaBarbero crono = db.cronogramaBarberoDAO().obtenerTodos().stream()
                    .filter(c -> c.idbarbero == idbarbero && c.fecha != null
                            && c.fecha.compareTo(hoy) >= 0)
                    .min((a, b) -> a.fecha.compareTo(b.fecha))
                    .orElse(null);

            String horarioTexto = "—";
            if (crono != null) {
                HorarioAtencionBase base = db.horarioAtencionBaseDAO().obtenerTodos().stream()
                        .filter(h -> h.idhorarioatencionbase == crono.idhorario)
                        .findFirst().orElse(null);
                String apertura = (base != null) ? trimHora(base.apertura) : "09:00";
                try {
                    Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(crono.fecha);
                    String fechaFmt = new SimpleDateFormat("d 'Sep' yyyy", new Locale("es", "SV")).format(d);
                    horarioTexto = fechaFmt + " - " + apertura + " a.m.";
                } catch (Exception e) {
                    horarioTexto = crono.fecha + " - " + apertura;
                }
            }

            final String sN = nombreSrv, sD = detalleSrv, bN = nombreBarb, bD = detalleBarb, h = horarioTexto;
            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.tv_resumen_servicio)).setText(sN);
                ((TextView) findViewById(R.id.tv_resumen_srv_detalle)).setText(sD);
                ((TextView) findViewById(R.id.tv_resumen_barbero)).setText(bN);
                ((TextView) findViewById(R.id.tv_resumen_barb_detalle)).setText(bD);
                ((TextView) findViewById(R.id.tv_resumen_horario)).setText(h);
            });
        });
    }

    private void confirmarReserva() {
        long personId = SessionManager.getPersonId(this);
        String hoy    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            Cliente cliente = db.clienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idpersona != null && c.idpersona == personId)
                    .findFirst().orElse(null);

            if (cliente == null) {
                runOnUiThread(() -> Toast.makeText(this,
                        "No se encontró perfil de cliente.", Toast.LENGTH_LONG).show());
                return;
            }

            // Hora de apertura
            CronogramaBarbero crono = db.cronogramaBarberoDAO().obtenerTodos().stream()
                    .filter(c -> c.idbarbero == idbarbero && c.fecha != null
                            && c.fecha.compareTo(hoy) >= 0)
                    .min((a, b) -> a.fecha.compareTo(b.fecha))
                    .orElse(null);

            String fecha = (crono != null) ? crono.fecha : hoy;
            String hora  = "09:00:00";
            if (crono != null) {
                HorarioAtencionBase base = db.horarioAtencionBaseDAO().obtenerTodos().stream()
                        .filter(h2 -> h2.idhorarioatencionbase == crono.idhorario)
                        .findFirst().orElse(null);
                if (base != null && base.apertura != null) {
                    hora = base.apertura.length() >= 8 ? base.apertura : base.apertura + ":00";
                }
            }

            CitasCliente nueva = new CitasCliente();
            nueva.idcitas    = System.currentTimeMillis();
            nueva.fecha      = fecha;
            nueva.hora       = hora;
            nueva.estado     = "PENDIENTE";
            nueva.idcliente  = cliente.idcliente;
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
            startActivity(new Intent(this, ClienteServicesActivity.class));
            finish();
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> { /* TODO */ });
    }

    private String trimHora(String hora) {
        if (hora == null) return "09:00";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
