package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

final class BeneficiosAdminController implements AdminViewController {
    private final MainActivity host;

    BeneficiosAdminController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_benefits).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.configureSpinner(R.id.admin_benefit_status_spinner, Color.WHITE,
                "Todas", "Activas", "Inactivas");
        host.findViewById(R.id.action_edit_base_rate).setOnClickListener(v ->
                host.showBaseRateForm());
        host.findViewById(R.id.action_add_admin_benefit).setOnClickListener(v ->
                host.showBenefitForm());
        host.configureAdminBenefit(R.id.admin_benefit_item_one,
                "Corte Clasico Barba", "45 Pts", true);
        host.configureAdminBenefit(R.id.admin_benefit_item_two,
                "Tinte & Mascarill...", "65 Pts", true);
        host.configureAdminBenefit(R.id.admin_benefit_item_three,
                "Combo VIP (Corte...", "70 Pts", false);
        host.setDisabledCard(R.id.admin_benefit_item_three, R.id.admin_benefit_active_switch);
        View disabledBenefit = host.findViewById(R.id.admin_benefit_item_three);
        ImageView icon = disabledBenefit.findViewById(R.id.benefit_icon);
        icon.setBackgroundResource(R.drawable.bg_benefit_avatar_disabled);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                Color.rgb(170, 170, 170)));
    }
}
