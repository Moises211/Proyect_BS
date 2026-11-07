package ues.edu.sv.proyect_bs;

import com.google.android.material.appbar.MaterialToolbar;

final class EspecialidadesController implements AdminViewController {
    private final MainActivity host;

    EspecialidadesController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        MaterialToolbar toolbar = host.findViewById(R.id.toolbar_admin_back);
        toolbar.setSubtitle("Administración Especialidades");
        toolbar.setNavigationOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
        host.findViewById(R.id.action_add_specialty).setOnClickListener(v ->
                host.showSpecialtyForm());
        host.configureSpecialtyActions(R.id.specialty_item_one);
        host.configureSpecialtyActions(R.id.specialty_item_two);
        host.configureSpecialtyActions(R.id.specialty_item_three);
    }
}
