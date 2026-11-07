package ues.edu.sv.proyect_bs;

final class HorariosServicioController implements AdminViewController {
    private final MainActivity host;

    HorariosServicioController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_service_hours).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.findViewById(R.id.action_previous_week).setOnClickListener(v ->
                host.showWeekChangeDialog(-1));
        host.findViewById(R.id.action_select_week).setOnClickListener(v ->
                host.showWeekCalendar());
        host.findViewById(R.id.week_days_summary).setOnClickListener(v ->
                host.showWeekCalendar());
        host.findViewById(R.id.action_next_week).setOnClickListener(v ->
                host.showWeekChangeDialog(1));
        host.findViewById(R.id.action_service_shifts).setOnClickListener(v ->
                host.showBaseShiftForm());
        host.findViewById(R.id.action_base_schedule).setOnClickListener(v ->
                host.showBaseScheduleForm());
        host.configureDailyShiftActions(R.id.service_daily_shift_one);
        host.configureDailyShiftActions(R.id.service_daily_shift_two);
        host.configureDailyShiftActions(R.id.service_daily_shift_three);
        host.configureDailyShiftActions(R.id.service_daily_shift_four);
        host.configureDailyShiftActions(R.id.service_daily_shift_five);
        host.configureDailyShiftActions(R.id.service_daily_shift_six);
        host.setDisabledCard(R.id.service_daily_shift_two, R.id.daily_shift_active);
    }
}
