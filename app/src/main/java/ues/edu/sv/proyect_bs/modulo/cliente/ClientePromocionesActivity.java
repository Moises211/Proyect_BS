package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Promociones;

public class ClientePromocionesActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_promociones);
        setupNavigation();
        cargarDatos();
        if (findViewById(R.id.btn_menu) != null)
            findViewById(R.id.btn_menu).setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatos() {
        executor.execute(() -> {
            List<Promociones> promos = AppDB.getInstance(this).promocionesDAO().obtenerTodos();
            runOnUiThread(() -> poblarUI(promos));
        });
    }

    private void poblarUI(List<Promociones> promos) {
        // Promo destacada: primera de la lista
        View cardDestacada = findViewById(R.id.card_promo_destacada);
        TextView tvDestNom = findViewById(R.id.tv_promo_destacada_nombre);
        TextView tvDestDesc = findViewById(R.id.tv_promo_destacada_desc);
        TextView tvDestAhorra = findViewById(R.id.tv_promo_destacada_ahorra);

        if (!promos.isEmpty()) {
            Promociones p = promos.get(0);
            tvDestNom.setText(p.producto_descuento != null ? p.producto_descuento : "Promoción especial");
            tvDestDesc.setText(p.descripcion != null ? p.descripcion : "");
            tvDestAhorra.setText("Ahorra $" + String.format("%.2f", 3.0));
            cardDestacada.setVisibility(View.VISIBLE);
        } else {
            cardDestacada.setVisibility(View.GONE);
        }

        // Lista del resto
        LinearLayout container = findViewById(R.id.layout_promos);
        container.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        int start = promos.isEmpty() ? 0 : 1;
        for (int i = start; i < promos.size(); i++) {
            Promociones p = promos.get(i);
            View item = inf.inflate(R.layout.item_promo_cliente, container, false);
            ((TextView) item.findViewById(R.id.tv_promo_nombre)).setText(
                    p.producto_descuento != null ? p.producto_descuento : "Promoción");
            ((TextView) item.findViewById(R.id.tv_promo_desc)).setText(
                    p.descripcion != null ? p.descripcion : "");
            TextView badge = item.findViewById(R.id.tv_promo_badge);
            badge.setText("-15%");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_teal));
            badge.setTextColor(0xFF0F5159);
            container.addView(item);
        }

        // Si sólo hay una promo o ninguna, mostrar placeholder
        if (promos.size() <= 1) {
            View item = inf.inflate(R.layout.item_promo_cliente, container, false);
            ((TextView) item.findViewById(R.id.tv_promo_nombre)).setText("Canje de puntos");
            ((TextView) item.findViewById(R.id.tv_promo_desc)).setText("100 puntos por perfilado");
            TextView badge = item.findViewById(R.id.tv_promo_badge);
            badge.setText("Info");
            badge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_active));
            badge.setTextColor(0xFF065F46);
            container.addView(item);
        }
    }

    private void setupNavigation() {
        if (findViewById(R.id.nav_inicio) != null)
            findViewById(R.id.nav_inicio).setOnClickListener(v -> {
                startActivity(new Intent(this, ClienteHomeActivity.class));
                finish();
            });
        if (findViewById(R.id.nav_citas) != null)
            findViewById(R.id.nav_citas).setOnClickListener(v -> {
                startActivity(new Intent(this, ClienteAppointmentsActivity.class));
                finish();
            });
        if (findViewById(R.id.nav_servicios) != null)
            findViewById(R.id.nav_servicios).setOnClickListener(v -> {});
        if (findViewById(R.id.nav_perfil) != null)
            findViewById(R.id.nav_perfil).setOnClickListener(v -> {
                startActivity(new Intent(this, ClientePerfilActivity.class));
                finish();
            });
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override protected void onDestroy() { executor.shutdown(); super.onDestroy(); }
}
