package ues.edu.sv.proyect_bs.modulo.cliente;

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
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class ClienteAppointmentsActivity extends AppCompatActivity {

    private static final int TAB_ACTIVAS = 0;
    private static final int TAB_TODAS   = 1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int tabActual = TAB_ACTIVAS;

    private static class CitaUI {
        CitasCliente cita;
        String nombreServicio;
        String nombreBarbero;
    }

    private List<CitaUI> todasCitas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_appointments);
        configurarTabs();
        setupNavigation();
        cargarDatos();
        findViewById(R.id.btn_menu).setOnClickListener(v -> cerrarSesion());
        findViewById(R.id.btn_nueva_cita).setOnClickListener(v ->
                startActivity(new Intent(this, ClienteServicesActivity.class)));
    }

    private void configurarTabs() {
        findViewById(R.id.tab_citas).setOnClickListener(v -> cambiarTab(TAB_ACTIVAS));
        findViewById(R.id.tab_todas).setOnClickListener(v -> cambiarTab(TAB_TODAS));
    }

    private void cambiarTab(int tab) {
        tabActual = tab;
        actualizarEstiloTabs();
        poblarLista();
    }

    private void actualizarEstiloTabs() {
        TextView t1 = findViewById(R.id.tab_citas);
        TextView t2 = findViewById(R.id.tab_todas);
        if (tabActual == TAB_ACTIVAS) {
            t1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_active));
            t1.setTextColor(0xFFFFFFFF);
            t2.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive));
            t2.setTextColor(0xFF267F91);
        } else {
            t2.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_active));
            t2.setTextColor(0xFFFFFFFF);
            t1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive));
            t1.setTextColor(0xFF267F91);
        }
    }

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);
            long clienteId = db.clienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idpersona != null && c.idpersona == personId)
                    .mapToLong(c -> c.idcliente).findFirst().orElse(-1L);

            List<CitasCliente> citas = db.citasClienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idcliente == clienteId)
                    .sorted((a, b) -> {
                        if (a.fecha == null) return 1;
                        if (b.fecha == null) return -1;
                        int cmp = b.fecha.compareTo(a.fecha);
                        return cmp != 0 ? cmp : (a.hora != null ? a.hora.compareTo(b.hora != null ? b.hora : "") : 1);
                    })
                    .collect(Collectors.toList());

            List<CitaUI> resultado = new ArrayList<>();
            for (CitasCliente c : citas) {
                CitaUI ui = new CitaUI();
                ui.cita = c;
                Servicios srv = db.serviciosDAO().obtenerPorId(c.idservicio);
                ui.nombreServicio = srv != null ? srv.nombre_servicio : "Servicio";
                Barberos b = db.barberosDAO().obtenerTodos().stream()
                        .filter(x -> x.idbarberos == c.idbarbero).findFirst().orElse(null);
                if (b != null && b.idpersona != null) {
                    Persona p = db.personaDAO().obtenerPorId(b.idpersona);
                    ui.nombreBarbero = p != null ? p.nombres + " " + p.apellidos : "Barbero";
                } else {
                    ui.nombreBarbero = "Barbero";
                }
                resultado.add(ui);
            }

            long activas = resultado.stream()
                    .filter(u -> !"COMPLETADA".equalsIgnoreCase(u.cita.estado)).count();
            runOnUiThread(() -> {
                todasCitas = resultado;
                ((TextView) findViewById(R.id.tab_citas)).setText("Citas " + activas);
                ((TextView) findViewById(R.id.tab_todas)).setText("Todas " + resultado.size());
                poblarLista();
            });
        });
    }

    private void poblarLista() {
        List<CitaUI> filtradas = tabActual == TAB_ACTIVAS
                ? todasCitas.stream().filter(u -> !"COMPLETADA".equalsIgnoreCase(u.cita.estado)).collect(Collectors.toList())
                : new ArrayList<>(todasCitas);

        LinearLayout container = findViewById(R.id.layout_citas);
        View sinCitas = findViewById(R.id.layout_sin_citas);
        container.removeAllViews();

        if (filtradas.isEmpty()) {
            sinCitas.setVisibility(View.VISIBLE);
            return;
        }
        sinCitas.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CitaUI ui : filtradas) {
            View item = inflater.inflate(R.layout.item_cita_cliente, container, false);
            ((TextView) item.findViewById(R.id.tv_cita_servicio)).setText(ui.nombreServicio);
            String fecha = ui.cita.fecha != null ? ui.cita.fecha : "—";
            String hora  = ui.cita.hora  != null && ui.cita.hora.length() >= 5 ? ui.cita.hora.substring(0,5) : "—";
            ((TextView) item.findViewById(R.id.tv_cita_detalle)).setText(fecha + " · " + ui.nombreBarbero + " · " + textoEstado(ui.cita.estado));
            aplicarBadge(item.findViewById(R.id.tv_cita_badge), ui.cita.estado);
            container.addView(item);
        }
    }

    private void aplicarBadge(TextView badge, String estado) {
        if ("EN_ATENCION".equalsIgnoreCase(estado)) {
            badge.setText("Activo");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_en_atencion));
            badge.setTextColor(0xFF92400E);
        } else if ("COMPLETADA".equalsIgnoreCase(estado)) {
            badge.setText("Lista");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_teal));
            badge.setTextColor(0xFF0F5159);
        } else {
            badge.setText("Pend");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_en_atencion));
            badge.setTextColor(0xFF92400E);
        }
    }

    private String textoEstado(String e) {
        if ("EN_ATENCION".equalsIgnoreCase(e)) return "En atención";
        if ("COMPLETADA".equalsIgnoreCase(e))  return "Completada";
        return "Pendiente";
    }

    private void setupNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            startActivity(new Intent(this, ClienteHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_citas).setOnClickListener(v -> cargarDatos());
        findViewById(R.id.nav_servicios).setOnClickListener(v ->
                startActivity(new Intent(this, ClientePromocionesActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(this, ClientePerfilActivity.class)));
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
