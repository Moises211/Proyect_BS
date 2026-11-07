package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class BeneficiosClienteController implements AdminViewController {
    private final MainActivity host;

    BeneficiosClienteController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_benefits_client).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios));
        host.configureSpinner(R.id.dropdown_benefit_status, Color.WHITE,
                "Todas", "Activas", "Canjeadas");
        host.configureBenefitActions(R.id.benefit_available_one);
        host.configureBenefitActions(R.id.benefit_available_two);
    }
}
