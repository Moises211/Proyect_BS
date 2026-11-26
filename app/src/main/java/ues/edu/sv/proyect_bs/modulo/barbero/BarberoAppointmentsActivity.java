package ues.edu.sv.proyect_bs.modulo.barbero;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class BarberoAppointmentsActivity extends AppCompatActivity {

    private static final String ESTADO_PENDIENTE   = "PENDIENTE";
    private static final String ESTADO_EN_ATENCION = "EN_ATENCION";
    private static final String ESTADO_COMPLETADA  = "COMPLETADA";

    private static final int TAB_TODAS       = 0;
    private static final int TAB_PENDIENTES  = 1;
    private static final int TAB_FINALIZADAS = 2;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int tabActual = TAB_TODAS;

    // Datos cargados
    private List<CitaUI> todasLasCitas = new ArrayList<>();

    // Datos de cada cita enriquecidos
    private static class CitaUI {
        CitasCliente cita;
        String nombreCliente;
        String nombreServicio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barbero_appointments);

        // Fecha de hoy
        String fechaFormateada = new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es", "SV"))
                .format(new Date());
        fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
        ((TextView) findViewById(R.id.tv_fecha_hoy)).setText("Hoy · " + fechaFormateada);

        configurarTabs();
        setupNavigation();
        cargarDatos();
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private void configurarTabs() {
        findViewById(R.id.tab_todas).setOnClickListener(v -> cambiarTab(TAB_TODAS));
        findViewById(R.id.tab_pendientes).setOnClickListener(v -> cambiarTab(TAB_PENDIENTES));
        findViewById(R.id.tab_finalizadas).setOnClickListener(v -> cambiarTab(TAB_FINALIZADAS));
    }

    private void cambiarTab(int tab) {
        tabActual = tab;
        actualizarEstiloTabs();
        poblarLista();
    }

    private void actualizarEstiloTabs() {
        int[] ids  = {R.id.tab_todas, R.id.tab_pendientes, R.id.tab_finalizadas};
        for (int i = 0; i < ids.length; i++) {
            TextView tv = findViewById(ids[i]);
            if (i == tabActual) {
                tv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_active));
                tv.setTextColor(0xFFFFFFFF);
            } else {
                tv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive));
                tv.setTextColor(0xFF267F91);
            }
        }
    }

    private void actualizarContadoresTabs(List<CitaUI> todas) {
        long pendientes  = todas.stream()
                .filter(c -> ESTADO_PENDIENTE.equalsIgnoreCase(c.cita.estado)
                          || ESTADO_EN_ATENCION.equalsIgnoreCase(c.cita.estado))
                .count();
        long finalizadas = todas.stream()
                .filter(c -> ESTADO_COMPLETADA.equalsIgnoreCase(c.cita.estado))
                .count();

        ((TextView) findViewById(R.id.tab_todas)).setText("Todas " + todas.size());
        ((TextView) findViewById(R.id.tab_pendientes)).setText("Pendientes " + pendientes);
        ((TextView) findViewById(R.id.tab_finalizadas)).setText("Finalizadas " + finalizadas);
    }

    // ── Carga de datos ───────────────────────────────────────────────────────

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);
        String hoy    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            long barberoId = db.barberosDAO().obtenerTodos().stream()
                    .filter(b -> b.idpersona != null && b.idpersona == personId)
                    .mapToLong(b -> b.idbarberos)
                    .findFirst().orElse(-1L);

            List<CitasCliente> citas = db.citasClienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idbarbero == barberoId && hoy.equals(c.fecha))
                    .sorted((a, b) -> compareHora(a.hora, b.hora))
                    .collect(Collectors.toList());

            List<CitaUI> resultado = new ArrayList<>();
            for (CitasCliente cita : citas) {
                CitaUI ui = new CitaUI();
                ui.cita = cita;

                // Nombre del cliente
                Persona persona = db.clienteDAO().obtenerTodos().stream()
                        .filter(cl -> cl.idcliente == cita.idcliente)
                        .findFirst()
                        .map(cl -> db.personaDAO().obtenerPorId(cl.idpersona))
                        .orElse(null);
                ui.nombreCliente = (persona != null)
                        ? persona.nombres + " " + persona.apellidos : "Cliente";

                // Nombre del servicio
                Servicios srv = db.serviciosDAO().obtenerPorId(cita.idservicio);
                ui.nombreServicio = (srv != null) ? srv.nombre_servicio : "Servicio";

                resultado.add(ui);
            }

            runOnUiThread(() -> {
                todasLasCitas = resultado;
                actualizarContadoresTabs(resultado);
                poblarLista();
            });
        });
    }

    // ── Poblar lista según tab ────────────────────────────────────────────────

    private void poblarLista() {
        List<CitaUI> filtradas;
        String msgVacio;

        switch (tabActual) {
            case TAB_PENDIENTES:
                filtradas = todasLasCitas.stream()
                        .filter(c -> ESTADO_PENDIENTE.equalsIgnoreCase(c.cita.estado)
                                  || ESTADO_EN_ATENCION.equalsIgnoreCase(c.cita.estado))
                        .collect(Collectors.toList());
                msgVacio = "Sin citas pendientes";
                break;
            case TAB_FINALIZADAS:
                filtradas = todasLasCitas.stream()
                        .filter(c -> ESTADO_COMPLETADA.equalsIgnoreCase(c.cita.estado))
                        .collect(Collectors.toList());
                msgVacio = "Sin citas finalizadas";
                break;
            default:
                filtradas = new ArrayList<>(todasLasCitas);
                msgVacio  = "Sin citas para hoy";
                break;
        }

        LinearLayout container  = findViewById(R.id.layout_citas);
        View         sinCitas   = findViewById(R.id.layout_sin_citas);
        TextView     tvSinMsg   = findViewById(R.id.tv_sin_citas_msg);
        container.removeAllViews();

        if (filtradas.isEmpty()) {
            tvSinMsg.setText(msgVacio);
            sinCitas.setVisibility(View.VISIBLE);
            return;
        }

        sinCitas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CitaUI ui : filtradas) {
            View item = inflater.inflate(R.layout.item_cita_barbero, container, false);

            TextView tvCliente = item.findViewById(R.id.tv_cita_cliente);
            TextView tvDetalle = item.findViewById(R.id.tv_cita_detalle);
            TextView tvBadge   = item.findViewById(R.id.tv_cita_badge);

            tvCliente.setText(ui.nombreCliente);

            String hora = trimHora(ui.cita.hora);
            String estado = textoEstado(ui.cita.estado);
            tvDetalle.setText(hora + " · " + ui.nombreServicio + " · " + estado);

            aplicarBadge(tvBadge, ui.cita.estado);

            container.addView(item);
        }
    }

    // ── Badge por estado ──────────────────────────────────────────────────────

    private void aplicarBadge(TextView badge, String estado) {
        if (ESTADO_EN_ATENCION.equalsIgnoreCase(estado)) {
            badge.setText("Actual");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_en_atencion));
            badge.setTextColor(0xFF92400E);
        } else if (ESTADO_COMPLETADA.equalsIgnoreCase(estado)) {
            badge.setText("Lista");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_teal));
            badge.setTextColor(0xFF0F5159);
        } else {
            badge.setText("Pend");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_en_atencion));
            badge.setTextColor(0xFF92400E);
        }
    }

    private String textoEstado(String estado) {
        if (ESTADO_EN_ATENCION.equalsIgnoreCase(estado)) return "En atención";
        if (ESTADO_COMPLETADA.equalsIgnoreCase(estado))  return "Completada";
        return "Pendiente";
    }

    // ── Navegación ───────────────────────────────────────────────────────────

    private void setupNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            startActivity(new Intent(this, BarberoHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_citas).setOnClickListener(v -> cargarDatos());
        findViewById(R.id.nav_servicios).setOnClickListener(v -> {
            startActivity(new Intent(this, BarberoScheduleActivity.class));
            finish();
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            // TODO: BarberoProfileActivity
        });
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private String trimHora(String hora) {
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
