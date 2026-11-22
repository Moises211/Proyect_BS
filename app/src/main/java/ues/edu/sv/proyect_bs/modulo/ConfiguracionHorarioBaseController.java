package ues.edu.sv.proyect_bs;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.HorarioDiaBase;

final class ConfiguracionHorarioBaseController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HorarioDiaBase[] dayRows = new HorarioDiaBase[7];
    private final HorarioDiaBase[] baselineRows = new HorarioDiaBase[7];
    private final int[] cardIds = {
            R.id.base_day_one, R.id.base_day_two, R.id.base_day_three,
            R.id.base_day_four, R.id.base_day_five, R.id.base_day_six,
            R.id.base_day_seven
    };

    ConfiguracionHorarioBaseController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_base_schedule).setOnClickListener(v -> requestLeave());
        host.findViewById(R.id.action_base_schedule_cancel).setOnClickListener(v -> {
            if (!hasUnsavedChanges()) {
                host.showScreen(R.layout.pantalla_horarios_servicio);
            } else {
                host.createLightDialogBuilder()
                        .setTitle("Cancelar configuración")
                        .setMessage("¿Deseas cancelar los cambios realizados?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí, cancelar", (dialog, which) ->
                                host.showScreen(R.layout.pantalla_horarios_servicio))
                        .show();
            }
        });
        host.findViewById(R.id.action_schedule_save).setOnClickListener(v -> {
            if (!hasUnsavedChanges()) {
                host.showScreen(R.layout.pantalla_horarios_servicio);
                return;
            }
            host.createLightDialogBuilder()
                    .setTitle("Guardar horario base")
                    .setMessage("¿Deseas guardar esta configuración como horario base?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        saveRows();
                        host.showScreen(R.layout.pantalla_horarios_servicio);
                    })
                    .show();
        });
        loadRows();
    }

    private void requestLeave() {
        if (!hasUnsavedChanges()) {
            host.showScreen(R.layout.pantalla_horarios_servicio);
            return;
        }
        host.createLightDialogBuilder()
                .setTitle("Cambios sin guardar")
                .setMessage("Tienes cambios sin guardar. ¿Deseas salir y descartarlos?")
                .setNegativeButton("Seguir editando", null)
                .setPositiveButton("Salir", (dialog, which) ->
                        host.showScreen(R.layout.pantalla_horarios_servicio))
                .show();
    }

    private boolean hasUnsavedChanges() {
        for (int index = 0; index < dayRows.length; index++) {
            if (!sameRow(baselineRows[index], dayRows[index])) return true;
        }
        return false;
    }

    private boolean sameRow(HorarioDiaBase baseline, HorarioDiaBase current) {
        if (baseline == null || current == null) {
            return baseline == current;
        }
        return java.util.Objects.equals(baseline.estado, current.estado)
                && java.util.Objects.equals(baseline.apertura, current.apertura)
                && java.util.Objects.equals(baseline.cierre, current.cierre)
                && baseline.receso == current.receso
                && java.util.Objects.equals(baseline.inicio, current.inicio)
                && java.util.Objects.equals(baseline.fin, current.fin);
    }

    private HorarioDiaBase copyRow(HorarioDiaBase source) {
        if (source == null) return null;
        HorarioDiaBase copy = new HorarioDiaBase();
        copy.idhorariodia = source.idhorariodia;
        copy.estado = source.estado;
        copy.apertura = source.apertura;
        copy.cierre = source.cierre;
        copy.receso = source.receso;
        copy.inicio = source.inicio;
        copy.fin = source.fin;
        copy.dia = source.dia;
        return copy;
    }

    private void loadRows() {
        executor.execute(() -> {
            List<HorarioDiaBase> loaded = database.horarioDiaBaseDAO().obtenerTodos();
            host.runOnUiThread(() -> {
                java.util.Arrays.fill(dayRows, null);
                java.util.Arrays.fill(baselineRows, null);
                for (HorarioDiaBase row : loaded) {
                    int position = dayPosition(row.dia);
                    if (position >= 0) {
                        dayRows[position] = row;
                        baselineRows[position] = copyRow(row);
                    }
                }
                render();
            });
        });
    }

    private void render() {
        String[] names = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        String[] initials = {"L", "M", "X", "J", "V", "S", "D"};
        int restDays = 0;
        int breakDays = 0;

        for (int index = 0; index < dayRows.length; index++) {
            HorarioDiaBase row = dayRows[index];
            View card = host.findViewById(cardIds[index]);
            if (row == null) {
                host.configureBaseScheduleDay(cardIds[index], names[index], initials[index],
                        "Sin configurar", "Sin Receso", null, null);
                continue;
            }

            boolean active = Boolean.TRUE.equals(row.estado);
            if (!active) restDays++;
            if (row.receso) breakDays++;

            String hours = active
                    ? formatTime(row.apertura) + " - " + formatTime(row.cierre)
                    : "Día de Descanso";
            String breakTime = row.receso
                    ? formatTime(row.inicio) + " - " + formatTime(row.fin)
                    : "Sin Receso";
            host.configureBaseScheduleDay(cardIds[index], names[index], initials[index],
                    hours, breakTime, row, () -> render());
            SwitchMaterial switchView = card.findViewById(R.id.daily_shift_active);
            switchView.setOnCheckedChangeListener(null);
            switchView.setChecked(active);
            if (!active) {
                host.setDisabledCard(card, R.id.daily_shift_active);
            } else {
                host.applyDarkModeToView(card);
            }
            switchView.setOnCheckedChangeListener((button, checked) -> {
                row.estado = checked;
                render();
            });
        }

        TextView rest = host.findViewById(R.id.base_schedule_rest_days);
        TextView breaks = host.findViewById(R.id.base_schedule_break_days);
        rest.setText("Descansos " + restDays + "/7");
        breaks.setText("Recesos " + breakDays + "/7");
    }

    private int dayPosition(String dia) {
        if (dia == null) return -1;
        String normalized = removeAccents(dia.trim()).toLowerCase(Locale.ROOT);
        if (normalized.startsWith("lun")) return 0;
        if (normalized.startsWith("mar")) return 1;
        if (normalized.startsWith("mie")) return 2;
        if (normalized.startsWith("jue")) return 3;
        if (normalized.startsWith("vie")) return 4;
        if (normalized.startsWith("sab")) return 5;
        if (normalized.startsWith("dom")) return 6;
        return -1;
    }

    private String removeAccents(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String formatTime(String value) {
        if (value == null) return "";
        String[] parts = value.trim().split(":");
        if (parts.length < 2) return value;
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String suffix = hour >= 12 ? "p.m." : "a.m.";
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            return String.format(Locale.US, "%d:%02d %s",
                    displayHour, minute, suffix);
        } catch (NumberFormatException exception) {
            return value;
        }
    }

    private void saveRows() {
        boolean[] hasError = {false};
        int[] remaining = {0};
        for (HorarioDiaBase row : dayRows) {
            if (row != null) {
                remaining[0]++;
            }
        }
        if (remaining[0] == 0) {
            Toast.makeText(host, "No hay jornadas base cargadas.", Toast.LENGTH_LONG).show();
            return;
        }
        for (int index = 0; index < dayRows.length; index++) {
            HorarioDiaBase row = dayRows[index];
            if (row == null) continue;
            baselineRows[index] = copyRow(row);
            executor.execute(() -> database.horarioDiaBaseDAO().actualizar(row));
            apiClient.saveBaseScheduleDay(SessionManager.getToken(host), row,
                    new AuthApiClient.BaseScheduleCallback() {
                        @Override public void onSuccess(HorarioDiaBase saved) {
                            executor.execute(() -> database.horarioDiaBaseDAO().actualizar(saved));
                            executor.execute(() -> database.operacionPendienteDAO()
                                    .eliminarPorOperacion("horario_dia_base", "PUT",
                                            row.idhorariodia));
                            remaining[0]--;
                            if (remaining[0] == 0) {
                                host.runOnUiThread(() -> {
                                    if (!hasError[0]) {
                                        Toast.makeText(host,
                                                "Horario base guardado correctamente.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                        @Override public void onError(String message) {
                            OperacionesPendientes.guardar(host, "horario_dia_base",
                                    "PUT", row.idhorariodia, row);
                            hasError[0] = true;
                            remaining[0]--;
                            host.runOnUiThread(() -> Toast.makeText(host,
                                    message, Toast.LENGTH_LONG).show());
                        }
                    });
        }
    }
}
