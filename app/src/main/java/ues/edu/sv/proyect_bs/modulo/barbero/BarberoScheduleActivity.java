package ues.edu.sv.proyect_bs.modulo.barbero;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionBase;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class BarberoScheduleActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Semana: lunes a viernes
    private final List<Calendar> diasSemana = new ArrayList<>();
    private int diaSeleccionadoIndex = 0; // índice en diasSemana

    // Chips
    private final int[] chipIds = {
        R.id.chip_lun, R.id.chip_mar, R.id.chip_mie, R.id.chip_jue, R.id.chip_vie
    };

    // Datos cargados
    private long barberoId = -1;
    private List<CronogramaBarbero> cronogramas;
    private List<HorarioAtencionBase> horariosBase;
    private List<Servicios> serviciosEspecialidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barbero_schedule);

        inicializarSemana();
        configurarChips();
        setupNavigation();
        cargarDatos();

        findViewById(R.id.btn_menu).setOnClickListener(v -> cerrarSesion());
    }

    // ── Semana ──────────────────────────────────────────────────────────────

    private void inicializarSemana() {
        Calendar hoy = Calendar.getInstance();
        // Mover al lunes de la semana actual
        int dow = hoy.get(Calendar.DAY_OF_WEEK);
        int offset = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
        hoy.add(Calendar.DAY_OF_MONTH, offset);

        for (int i = 0; i < 5; i++) {
            Calendar dia = (Calendar) hoy.clone();
            dia.add(Calendar.DAY_OF_MONTH, i);
            diasSemana.add(dia);
        }

        // Día seleccionado = hoy (si es laborable), sino lunes
        Calendar ahora = Calendar.getInstance();
        int dowAhora = ahora.get(Calendar.DAY_OF_WEEK);
        if (dowAhora >= Calendar.MONDAY && dowAhora <= Calendar.FRIDAY) {
            diaSeleccionadoIndex = dowAhora - Calendar.MONDAY;
        } else {
            diaSeleccionadoIndex = 0;
        }

        // Rango de semana
        String rango = formatearRangoSemana(diasSemana.get(0), diasSemana.get(4));
        ((TextView) findViewById(R.id.tv_semana_rango)).setText(rango);
    }

    private String formatearRangoSemana(Calendar lunes, Calendar viernes) {
        SimpleDateFormat dfDia = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat dfMes = new SimpleDateFormat("'de' MMMM", new Locale("es", "SV"));
        return "Semana del " + dfDia.format(lunes.getTime())
                + " al " + dfDia.format(viernes.getTime())
                + " " + dfMes.format(viernes.getTime());
    }

    // ── Chips de día ────────────────────────────────────────────────────────

    private final String[] PREFIJOS = {"L", "M", "M", "J", "V"};

    private void configurarChips() {
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            TextView chip = findViewById(chipIds[i]);
            int dia = diasSemana.get(i).get(Calendar.DAY_OF_MONTH);
            chip.setText(PREFIJOS[i] + dia);
            chip.setOnClickListener(v -> seleccionarDia(idx));
        }
        actualizarEstiloChips();
    }

    private void seleccionarDia(int idx) {
        diaSeleccionadoIndex = idx;
        actualizarEstiloChips();
        poblarDetalleDia();
    }

    private void actualizarEstiloChips() {
        for (int i = 0; i < 5; i++) {
            TextView chip = findViewById(chipIds[i]);
            if (i == diaSeleccionadoIndex) {
                chip.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_day_chip_active));
                chip.setTextColor(0xFFFFFFFF);
                chip.setTypeface(null, Typeface.BOLD);
            } else {
                chip.setBackground(null);
                chip.setTextColor(0xFF6B7280);
                chip.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    // ── Carga de datos ───────────────────────────────────────────────────────

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);

        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            // Barbero id
            Barberos barbero = db.barberosDAO().obtenerTodos().stream()
                    .filter(b -> b.idpersona != null && b.idpersona == personId)
                    .findFirst().orElse(null);

            if (barbero == null) {
                runOnUiThread(this::mostrarSinDatos);
                return;
            }

            barberoId = barbero.idbarberos;

            // Cronograma del barbero
            cronogramas = db.cronogramaBarberoDAO().obtenerTodos().stream()
                    .filter(c -> c.idbarbero == barberoId)
                    .collect(java.util.stream.Collectors.toList());

            // Horarios base (apertura/cierre)
            horariosBase = db.horarioAtencionBaseDAO().obtenerTodos();

            // Servicios de la especialidad del barbero
            if (barbero.especialidad != null) {
                serviciosEspecialidad = db.serviciosDAO().obtenerTodos().stream()
                        .filter(s -> barbero.especialidad.equals(s.idcategoria_servicio)
                                && Boolean.TRUE.equals(s.estado))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                serviciosEspecialidad = new ArrayList<>();
            }

            runOnUiThread(() -> {
                poblarDetalleDia();
                poblarServicios();
            });
        });
    }

    // ── Detalle del día seleccionado ─────────────────────────────────────────

    private void poblarDetalleDia() {
        if (cronogramas == null) return;

        Calendar diaCalendar = diasSemana.get(diaSeleccionadoIndex);
        String fechaDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(diaCalendar.getTime());

        // Nombre largo del día
        String nombreDia = new SimpleDateFormat("EEEE d", new Locale("es", "SV"))
                .format(diaCalendar.getTime());
        nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1);

        TextView tvNombre   = findViewById(R.id.tv_dia_nombre);
        TextView tvHorario  = findViewById(R.id.tv_horario_servicio);
        TextView tvDescanso = findViewById(R.id.tv_descanso);
        TextView tvSin      = findViewById(R.id.tv_sin_horario);

        tvNombre.setText(nombreDia);

        // Buscar cronograma de ese día
        CronogramaBarbero crono = cronogramas.stream()
                .filter(c -> fechaDia.equals(c.fecha))
                .findFirst().orElse(null);

        if (crono != null) {
            // Buscar horario base para apertura/cierre
            final long idHorario = crono.idhorario;
            HorarioAtencionBase base = horariosBase.stream()
                    .filter(h -> h.idhorarioatencionbase == idHorario)
                    .findFirst().orElse(null);

            if (base != null) {
                tvHorario.setText("Horario de Servicio "
                        + trim(base.apertura) + "-" + trim(base.cierre));
                tvHorario.setVisibility(View.VISIBLE);
            } else {
                tvHorario.setVisibility(View.GONE);
            }

            if (crono.receso && crono.horainicioreceso != null) {
                tvDescanso.setText("Descanso " + trim(crono.horainicioreceso)
                        + "-" + trim(crono.horafinreceso));
                tvDescanso.setVisibility(View.VISIBLE);
            } else {
                tvDescanso.setVisibility(View.GONE);
            }

            tvSin.setVisibility(View.GONE);
        } else {
            tvHorario.setVisibility(View.GONE);
            tvDescanso.setVisibility(View.GONE);
            tvSin.setVisibility(View.VISIBLE);
        }
    }

    // ── Servicios ────────────────────────────────────────────────────────────

    private void poblarServicios() {
        LinearLayout container = findViewById(R.id.layout_servicios);
        View sinServicios = findViewById(R.id.layout_sin_servicios);
        container.removeAllViews();

        if (serviciosEspecialidad == null || serviciosEspecialidad.isEmpty()) {
            sinServicios.setVisibility(View.VISIBLE);
            return;
        }

        sinServicios.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Servicios srv : serviciosEspecialidad) {
            View item = inflater.inflate(R.layout.item_servicio_barbero, container, false);

            TextView tvNombre    = item.findViewById(R.id.tv_srv_nombre);
            TextView tvDetalle   = item.findViewById(R.id.tv_srv_detalle);

            tvNombre.setText(srv.nombre_servicio != null ? srv.nombre_servicio : "—");

            String duracion = srv.duracion != null ? srv.duracion + " min" : "—";
            String precio   = srv.precio   != null ? " · $" + String.format(Locale.getDefault(), "%.0f", srv.precio) : "";
            tvDetalle.setText(duracion + precio);

            container.addView(item);
        }
    }

    private void mostrarSinDatos() {
        ((TextView) findViewById(R.id.tv_dia_nombre)).setText("Sin datos");
        findViewById(R.id.layout_sin_servicios).setVisibility(View.VISIBLE);
    }

    // ── Navegación ───────────────────────────────────────────────────────────

    private void setupNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            startActivity(new Intent(this, BarberoHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_citas).setOnClickListener(v -> {
            startActivity(new Intent(this, BarberoAppointmentsActivity.class));
            finish();
        });
        findViewById(R.id.nav_servicios).setOnClickListener(v -> {
            // Ya estamos aquí, refrescar
            cargarDatos();
        });
        findViewById(R.id.nav_perfil).setOnClickListener(v -> {
            // TODO: ir a BarberoProfileActivity
        });
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private String trim(String hora) {
        if (hora == null) return "—";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
