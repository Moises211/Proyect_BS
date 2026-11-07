package ues.edu.sv.proyect_bs;

final class PersonalTurnosController implements AdminViewController {
    private final MainActivity host;

    PersonalTurnosController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.getNavigationController().configureToolbar(R.id.toolbar_personal_turnos);
        host.findViewById(R.id.card_staff_barbers).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_barberos));
        host.findViewById(R.id.card_staff_schedule).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cronograma_barberos));
        host.findViewById(R.id.card_staff_specialties).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_especialidades));
    }
}
