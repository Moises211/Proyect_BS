package ues.edu.sv.proyect_bs;

final class ConfiguracionHorarioBaseController implements AdminViewController {
    private final MainActivity host;

    ConfiguracionHorarioBaseController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_base_schedule).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_horarios_servicio));
        host.findViewById(R.id.action_base_schedule_cancel).setOnClickListener(v ->
                host.createLightDialogBuilder()
                        .setTitle("Cancelar configuración")
                        .setMessage("¿Deseas cancelar la configuración del horario base?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí, cancelar", (dialog, which) ->
                                host.showScreen(R.layout.pantalla_horarios_servicio))
                        .show());
        host.findViewById(R.id.action_schedule_save).setOnClickListener(v ->
                host.createLightDialogBuilder()
                        .setTitle("Guardar horario base")
                        .setMessage("¿Deseas guardar esta configuración como horario base?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Guardar", null)
                        .show());
        host.configureBaseScheduleDay(R.id.base_day_one, "Lunes", "L",
                "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
        host.configureBaseScheduleDay(R.id.base_day_two, "Martes", "M",
                "Día de Descanso", "Sin Receso");
        host.configureBaseScheduleDay(R.id.base_day_three, "Miércoles", "X",
                "9:00 a.m. - 7:00 p.m.", "Sin Receso");
        host.configureBaseScheduleDay(R.id.base_day_four, "Jueves", "J",
                "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
        host.configureBaseScheduleDay(R.id.base_day_five, "Viernes", "V",
                "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
        host.configureBaseScheduleDay(R.id.base_day_six, "Sábado", "S",
                "9:00 a.m. - 7:00 p.m.", "Sin Receso");
        host.configureBaseScheduleDay(R.id.base_day_seven, "Domingo", "D",
                "9:00 a.m. - 7:00 p.m.", "Sin Receso");
        host.setDisabledCard(R.id.base_day_two, R.id.daily_shift_active);
    }
}
