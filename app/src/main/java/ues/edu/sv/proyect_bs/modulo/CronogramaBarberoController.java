package ues.edu.sv.proyect_bs;

final class CronogramaBarberoController implements AdminViewController {
    private final MainActivity host;

    CronogramaBarberoController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_personal_schedule).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cronograma_barberos));
        host.findViewById(R.id.action_base_shift).setOnClickListener(v ->
                host.showBaseShiftForm());
        host.configureDailyShiftActions(R.id.daily_shift_one);
        host.configureDailyShiftActions(R.id.daily_shift_two);
        host.configureDailyShiftActions(R.id.daily_shift_three);
        host.configureDailyShiftActions(R.id.daily_shift_four);
        host.setDisabledCard(R.id.daily_shift_two, R.id.daily_shift_active);
    }
}
