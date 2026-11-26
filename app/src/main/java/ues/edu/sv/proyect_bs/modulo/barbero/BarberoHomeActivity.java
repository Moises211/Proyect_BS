package ues.edu.sv.proyect_bs.modulo.barbero;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class BarberoHomeActivity extends AppCompatActivity {

    // estados de cita usados en la BD
    private static final String ESTADO_EN_ATENCION  = "EN_ATENCION";
    private static final String ESTADO_COMPLETADA   = "COMPLETADA";
    private static final String ESTADO_PENDIENTE    = "PENDIENTE";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // vistas métricas
    private TextView tvSaludo, tvFechaTurno;
    private TextView tvCitasHoy, tvEnAtencion, tvCompletadas;

    // tarjeta atención actual
    private View cardAtencionActual;
    private TextView tvAtencionHoraCliente, tvAtencionServicio;

    // tarjeta siguiente cita
    private View cardSiguienteCita;
    private TextView tvSiguienteHoraCliente;

    // tarjeta horario
    private View cardMiHorario;
    private TextView tvHorarioDetalle;

    // estado vacío
    private View layoutSinCitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barbero_home);
        bindViews();
        setupNavigation();
        cargarDatos();
    }

    // ── Binding ─────────────────────────────────────────────────────────────

    private void bindViews() {
        tvSaludo               = findViewById(R.id.tv_saludo);
        tvFechaTurno           = findViewById(R.id.tv_fecha_turno);
        tvCitasHoy             = findViewById(R.id.tv_citas_hoy);
        tvEnAtencion           = findViewById(R.id.tv_en_atencion);
        tvCompletadas          = findViewById(R.id.tv_completadas);

        cardAtencionActual     = findViewById(R.id.card_atencion_actual);
        tvAtencionHoraCliente  = findViewById(R.id.tv_atencion_hora_cliente);
        tvAtencionServicio     = findViewById(R.id.tv_atencion_servicio);

        cardSiguienteCita      = findViewById(R.id.card_siguiente_cita);
        tvSiguienteHoraCliente = findViewById(R.id.tv_siguiente_hora_cliente);

        cardMiHorario          = findViewById(R.id.card_mi_horario);
        tvHorarioDetalle       = findViewById(R.id.tv_horario_detalle);

        layoutSinCitas         = findViewById(R.id.layout_sin_citas);
    }

    // ── Navegación ──────────────────────────────────────────────────────────

    private void setupNavigation() {
        // nav_inicio ya es la pantalla activa; refresca al tocar
        findViewById(R.id.nav_inicio).setOnClickListener(v -> cargarDatos());

        // Citas del barbero → próxima pantalla (placeholder hasta implementar)
        findViewById(R.id.nav_citas).setOnClickListener(v -> {
            // TODO: ir a BarberoAppointmentsActivity
        });

        // Servicios/horarios → próxima pantalla
        findViewById(R.id.nav_servicios).setOnClickListener(v -> {
            // TODO: ir a BarberoScheduleActivity
        });

        // Perfil
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            // TODO: ir a BarberProfileActivity
        });

        // Menú hamburguesa → cerrar sesión (provisional)
        findViewById(R.id.btn_menu).setOnClickListener(v -> cerrarSesion());
    }

    // ── Carga de datos en hilo de fondo ─────────────────────────────────────

    private void cargarDatos() {
        long personId  = SessionManager.getPersonId(this);
        String hoy     = fechaHoy();

        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            // 1. Nombre del barbero
            Persona persona = db.personaDAO().obtenerPorId(personId);

            // 2. Barbero id (buscar por idpersona)
            long barberoId = db.barberosDAO().obtenerTodos()
                    .stream()
                    .filter(b -> b.idpersona != null && b.idpersona == personId)
                    .mapToLong(b -> b.idbarberos)
                    .findFirst()
                    .orElse(-1L);

            // 3. Citas del día para este barbero
            List<CitasCliente> todasLasCitas = db.citasClienteDAO().obtenerTodos();
            List<CitasCliente> citasHoy = todasLasCitas.stream()
                    .filter(c -> c.idbarbero == barberoId && hoy.equals(c.fecha))
                    .sorted((a, b2) -> compareHora(a.hora, b2.hora))
                    .collect(java.util.stream.Collectors.toList());

            int totalHoy      = citasHoy.size();
            int enAtencion    = (int) citasHoy.stream().filter(c -> ESTADO_EN_ATENCION.equalsIgnoreCase(c.estado)).count();
            int completadas   = (int) citasHoy.stream().filter(c -> ESTADO_COMPLETADA.equalsIgnoreCase(c.estado)).count();

            CitasCliente citaActual    = citasHoy.stream()
                    .filter(c -> ESTADO_EN_ATENCION.equalsIgnoreCase(c.estado))
                    .findFirst().orElse(null);

            CitasCliente siguienteCita = citasHoy.stream()
                    .filter(c -> ESTADO_PENDIENTE.equalsIgnoreCase(c.estado))
                    .findFirst().orElse(null);

            // 4. Servicio de la cita actual
            Servicios servActual = (citaActual != null)
                    ? db.serviciosDAO().obtenerPorId(citaActual.idservicio) : null;

            // 5. Cliente de la cita actual
            Persona clienteActual = resolverClientePersona(db, citaActual);

            // 6. Cliente de la siguiente cita
            Persona clienteSiguiente = resolverClientePersona(db, siguienteCita);

            // 7. Horario de hoy
            HorarioAtencionDiario horario = db.horarioAtencionDiarioDAO().obtenerTodos()
                    .stream()
                    .filter(h -> hoy.equals(h.fecha))
                    .findFirst().orElse(null);

            // Actualizar UI en hilo principal
            final String nombre    = (persona != null) ? persona.nombres.split(" ")[0] : "Barbero";
            final int tot          = totalHoy;
            final int atenc        = enAtencion;
            final int comp         = completadas;
            final CitasCliente ca  = citaActual;
            final Servicios sa     = servActual;
            final Persona pca      = clienteActual;
            final CitasCliente sc  = siguienteCita;
            final Persona pcs      = clienteSiguiente;
            final HorarioAtencionDiario ho = horario;

            runOnUiThread(() -> poblarUI(nombre, tot, atenc, comp, ca, sa, pca, sc, pcs, ho));
        });
    }

    private Persona resolverClientePersona(AppDB db, CitasCliente cita) {
        if (cita == null) return null;
        // buscar el cliente y luego su persona
        return db.clienteDAO().obtenerTodos().stream()
                .filter(cl -> cl.idcliente == cita.idcliente)
                .findFirst()
                .map(cl -> db.personaDAO().obtenerPorId(cl.idpersona))
                .orElse(null);
    }

    // ── Poblar la UI ────────────────────────────────────────────────────────

    private void poblarUI(String nombre, int total, int enAtenc, int comp,
                          CitasCliente citaActual, Servicios servActual, Persona clienteActual,
                          CitasCliente siguienteCita, Persona clienteSiguiente,
                          HorarioAtencionDiario horario) {

        // Saludo y fecha
        tvSaludo.setText("Hola, " + nombre);
        tvFechaTurno.setText(fechaHoyFormateada() + " · Turno Completo");

        // Métricas
        tvCitasHoy.setText(String.valueOf(total));
        tvEnAtencion.setText(String.valueOf(enAtenc));
        tvCompletadas.setText(String.valueOf(comp));

        // Tarjeta Atención actual
        if (citaActual != null) {
            String nombreCliente = (clienteActual != null)
                    ? clienteActual.nombres + " " + clienteActual.apellidos : "Cliente";
            String horaCita      = formatearHora(citaActual.hora);
            tvAtencionHoraCliente.setText(horaCita + " · " + nombreCliente);

            if (servActual != null) {
                tvAtencionServicio.setText(servActual.nombre_servicio
                        + " · " + servActual.duracion + " min");
            }

            cardAtencionActual.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_finalizar_servicio).setOnClickListener(v ->
                    finalizarServicio(citaActual));
        }

        // Tarjeta Siguiente cita
        if (siguienteCita != null) {
            String nombreSig = (clienteSiguiente != null)
                    ? clienteSiguiente.nombres + " " + clienteSiguiente.apellidos : "Cliente";
            tvSiguienteHoraCliente.setText(formatearHora(siguienteCita.hora) + " · " + nombreSig);
            cardSiguienteCita.setVisibility(View.VISIBLE);
        }

        // Tarjeta Mi horario
        if (horario != null) {
            String horarioTexto = horario.apertura + "-" + horario.cierre;
            if (horario.receso && horario.inicio != null && horario.fin != null) {
                horarioTexto += " · descanso " + horario.inicio;
            }
            tvHorarioDetalle.setText(horarioTexto);
            cardMiHorario.setVisibility(View.VISIBLE);
        }

        // Estado vacío
        boolean hayContenido = citaActual != null || siguienteCita != null || horario != null;
        layoutSinCitas.setVisibility(hayContenido ? View.GONE : View.VISIBLE);
    }

    // ── Acción: finalizar servicio ───────────────────────────────────────────

    private void finalizarServicio(CitasCliente cita) {
        executor.execute(() -> {
            cita.estado = ESTADO_COMPLETADA;
            AppDB.getInstance(this).citasClienteDAO().actualizar(cita);
            runOnUiThread(this::cargarDatos);
        });
    }

    // ── Logout ──────────────────────────────────────────────────────────────

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    // ── Utilidades de fecha/hora ─────────────────────────────────────────────

    private String fechaHoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String fechaHoyFormateada() {
        return new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es", "SV"))
                .format(new Date());
    }

    /** HH:mm:ss → HH:mm */
    private String formatearHora(String hora) {
        if (hora == null) return "—";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    private int compareHora(String h1, String h2) {
        if (h1 == null) return 1;
        if (h2 == null) return -1;
        return h1.compareTo(h2);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
