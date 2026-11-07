package ues.edu.sv.proyect_bs;

import android.graphics.Color;

final class PromocionesController implements AdminViewController {
    private final MainActivity host;

    PromocionesController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_promotions).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.configureSpinner(R.id.promotion_status_spinner, Color.WHITE,
                "Todas", "Activas", "Inactivas");
        host.findViewById(R.id.action_add_promotion).setOnClickListener(v ->
                host.showPromotionForm());
        host.configurePromotionActions(R.id.promotion_item_one,
                "Especial Mes del Padre", "Catálogo", "Combo Papá e Hijo", true);
        host.configurePromotionActions(R.id.promotion_item_two,
                "Descuento Corte Fo...", "Descuento", "10% en cortes", true);
        host.configurePromotionActions(R.id.promotion_item_three,
                "Descuento Corte Fo...", "Descuento", "15% en cortes", false);
        host.setDisabledCard(R.id.promotion_item_three, R.id.promotion_active_switch);
    }
}
