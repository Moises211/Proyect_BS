package ues.edu.sv.proyect_bs;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;

final class BeneficiosController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ClientBenefitRow> rows = new ArrayList<>();
    private String searchText = "";

    BeneficiosController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        View searchField = host.findViewById(R.id.admin_search_field);
        EditText search = findEditText(searchField);
        if (search != null) {
            search.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchText = s.toString().trim().toLowerCase(Locale.ROOT);
                    render();
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
        loadClients();
    }

    private EditText findEditText(View view) {
        if (view instanceof EditText) return (EditText) view;
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                EditText result = findEditText(group.getChildAt(index));
                if (result != null) return result;
            }
        }
        return null;
    }

    private void loadClients() {
        executor.execute(() -> {
            List<ClientBenefitRow> loaded = new ArrayList<>();
            for (Cliente client : database.clienteDAO().obtenerTodos()) {
                Persona person = client.idpersona == null
                        ? null : database.personaDAO().obtenerPorId(client.idpersona);
                PuntosCliente points = database.puntosClienteDAO().obtenerPorId(client.idcliente);
                loaded.add(new ClientBenefitRow(client, person,
                        points == null || points.cantidadpuntos == null
                                ? 0 : points.cantidadpuntos));
            }
            host.runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                render();
            });
        });
    }

    private void render() {
        LinearLayout list = host.findViewById(R.id.benefits_clients_list);
        list.removeAllViews();
        for (ClientBenefitRow row : rows) {
            String names = row.person == null ? "" : safe(row.person.nombres);
            String lastNames = row.person == null ? "" : safe(row.person.apellidos);
            String fullName = (names + " " + lastNames).trim();
            if (!searchText.isEmpty()
                    && !fullName.toLowerCase(Locale.ROOT).contains(searchText)) {
                continue;
            }
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_benefit_client, list, false);
            list.addView(item);
            TextView avatar = item.findViewById(R.id.benefit_client_avatar);
            TextView name = item.findViewById(R.id.benefit_client_name);
            TextView points = item.findViewById(R.id.benefit_client_points);
            name.setText(fullName.isEmpty() ? "Sin nombre" : fullName);
            avatar.setText(initials(names, lastNames));
            points.setText(row.points + " Pts");
            item.setOnClickListener(v -> {
                host.setSelectedBenefitClientId(row.client.idcliente);
                host.showScreen(R.layout.pantalla_beneficios_cliente);
            });
            host.applyDarkModeToView(item);
        }
    }

    private String initials(String names, String lastNames) {
        String first = names.isEmpty() ? "" : names.substring(0, 1);
        String last = lastNames.isEmpty() ? "" : lastNames.substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class ClientBenefitRow {
        final Cliente client;
        final Persona person;
        final int points;

        ClientBenefitRow(Cliente client, Persona person, int points) {
            this.client = client;
            this.person = person;
            this.points = points;
        }
    }
}
