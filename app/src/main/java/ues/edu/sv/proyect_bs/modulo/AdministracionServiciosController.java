package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Servicios;

final class AdministracionServiciosController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Servicios> rows = new ArrayList<>();
    private String searchText = "";
    private int statusFilter;

    AdministracionServiciosController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        MaterialToolbar toolbar = host.findViewById(R.id.toolbar_services);
        toolbar.setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.findViewById(R.id.action_add_service).setOnClickListener(v ->
                openServiceForm(null));
        host.configureSpinner(R.id.service_status_spinner, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        ((android.widget.Spinner) host.findViewById(R.id.service_status_spinner))
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                            int position, long id) {
                        statusFilter = position;
                        render();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        EditText search = findSearchInput();
        if (search != null) {
            search.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchText = s.toString().trim().toLowerCase(Locale.ROOT);
                    render();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        loadServices();
    }

    private EditText findSearchInput() {
        View field = host.findViewById(R.id.admin_search_field);
        return findEditText(field);
    }

    private EditText findEditText(View view) {
        if (view instanceof EditText) {
            return (EditText) view;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                EditText result = findEditText(group.getChildAt(index));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void loadServices() {
        executor.execute(() -> {
            List<Servicios> loaded = database.serviciosDAO().obtenerTodos();
            host.runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                render();
            });
        });
    }

    private void render() {
        TextView count = host.findViewById(R.id.services_count);
        count.setText(rows.size() + " items");
        LinearLayout list = host.findViewById(R.id.services_list);
        list.removeAllViews();
        for (Servicios service : rows) {
            if (!matches(service)) {
                continue;
            }
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_service, list, false);
            list.addView(item);
            ((TextView) item.findViewById(R.id.service_name))
                    .setText(valueOrDefault(service.nombre_servicio, "Sin nombre"));
            ((TextView) item.findViewById(R.id.service_duration))
                    .setText((service.duracion == null ? 0 : service.duracion) + " min");
            ((TextView) item.findViewById(R.id.service_price))
                    .setText(String.format(Locale.getDefault(), "$%.2f",
                            service.precio == null ? 0d : service.precio));
            SwitchMaterial active = item.findViewById(R.id.service_active_switch);
            active.setOnCheckedChangeListener(null);
            active.setChecked(Boolean.TRUE.equals(service.estado));
            if (Boolean.TRUE.equals(service.estado)) {
                restoreActiveStyle(item);
                host.applyDarkModeToView(item);
            } else {
                host.setDisabledCard(item, R.id.service_active_switch);
                host.applyDarkModeToView(item);
            }
            active.setOnCheckedChangeListener((button, checked) ->
                    showStatusConfirmation(service, item, active, checked));
            item.findViewById(R.id.action_edit_service).setOnClickListener(v ->
                    openServiceForm(service));
            item.findViewById(R.id.action_delete_service).setOnClickListener(v ->
                    showDeleteConfirmation(service));
        }
    }

    private void showStatusConfirmation(Servicios service, View item,
            SwitchMaterial active, boolean newState) {
        active.setOnCheckedChangeListener(null);
        active.setChecked(Boolean.TRUE.equals(service.estado));
        String action = newState ? "habilitar" : "deshabilitar";
        host.createLightDialogBuilder()
                .setTitle("Cambiar estado")
                .setMessage("¿Estás seguro de que quieres " + action + " este servicio?")
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    active.setOnCheckedChangeListener(null);
                    active.setChecked(Boolean.TRUE.equals(service.estado));
                    active.setOnCheckedChangeListener((button, checked) ->
                            showStatusConfirmation(service, item, active, checked));
                })
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    Boolean previousState = service.estado;
                    service.estado = newState;
                    apiClient.updateServiceStatus(SessionManager.getToken(host), service,
                            new AuthApiClient.ServiceActionCallback() {
                                @Override
                                public void onSuccess() {
                                    executor.execute(() -> {
                                        database.serviciosDAO().actualizar(service);
                                        host.runOnUiThread(() -> {
                                            active.setOnCheckedChangeListener((button, checked) ->
                                                    showStatusConfirmation(service, item,
                                                            active, checked));
                                            render();
                                        });
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    service.estado = previousState;
                                    active.setOnCheckedChangeListener(null);
                                    active.setChecked(Boolean.TRUE.equals(previousState));
                                    active.setOnCheckedChangeListener((button, checked) ->
                                            showStatusConfirmation(service, item, active, checked));
                                    host.createLightDialogBuilder()
                                            .setTitle("No se pudo actualizar")
                                            .setMessage(message)
                                            .setPositiveButton("Aceptar", null)
                                            .show();
                                }
                            });
                })
                .show();
    }

    private void showDeleteConfirmation(Servicios service) {
        host.createLightDialogBuilder()
                .setTitle("Eliminar servicio")
                .setMessage("¿Estás seguro de que quieres eliminar este servicio?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) ->
                        apiClient.deleteService(SessionManager.getToken(host),
                                service.idservicio,
                                new AuthApiClient.ServiceActionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        executor.execute(() -> {
                                            database.serviciosDAO().eliminar(service);
                                            host.runOnUiThread(() -> {
                                                rows.remove(service);
                                                render();
                                            });
                                        });
                                    }

                                    @Override
                                    public void onError(String message) {
                                        host.createLightDialogBuilder()
                                                .setTitle("No se pudo eliminar")
                                                .setMessage(message)
                                                .setPositiveButton("Aceptar", null)
                                                .show();
                                    }
                                }))
                .show();
    }

    private void restoreActiveStyle(View item) {
        com.google.android.material.card.MaterialCardView card =
                (com.google.android.material.card.MaterialCardView) item;
        card.setCardBackgroundColor(Color.WHITE);
        TextView name = item.findViewById(R.id.service_name);
        TextView duration = item.findViewById(R.id.service_duration);
        TextView price = item.findViewById(R.id.service_price);
        name.setTextColor(Color.rgb(37, 37, 37));
        duration.setTextColor(Color.rgb(117, 117, 117));
        price.setTextColor(Color.rgb(20, 122, 62));
        item.findViewById(R.id.action_edit_service).setAlpha(1f);
        item.findViewById(R.id.action_delete_service).setAlpha(1f);
    }

    private void openServiceForm(Servicios service) {
        executor.execute(() -> {
            List<ues.edu.sv.proyect_bs.modelos.CategoriaServicio> categories =
                    database.categoriaServicioDAO().obtenerTodos();
            host.runOnUiThread(() -> host.showServiceForm(service != null, service, categories,
                    value -> {
                        String token = SessionManager.getToken(host);
                        apiClient.saveService(token, service != null, value,
                                new AuthApiClient.ServiceFormCallback() {
                                    @Override
                                    public void onSuccess(Servicios saved) {
                                        executor.execute(() -> {
                                            database.serviciosDAO().insertar(saved);
                                            host.runOnUiThread(() -> {
                                                if (service == null) {
                                                    rows.add(saved);
                                                }
                                                render();
                                            });
                                        });
                                    }

                                    @Override
                                    public void onError(String message) {
                                        host.createLightDialogBuilder()
                                                .setTitle("No se pudo guardar")
                                                .setMessage(message)
                                                .setPositiveButton("Aceptar", null)
                                                .show();
                                    }
                                });
                    }));
        });
    }

    private boolean matches(Servicios service) {
        boolean active = Boolean.TRUE.equals(service.estado);
        if ((statusFilter == 1 && !active) || (statusFilter == 2 && active)) {
            return false;
        }
        String name = valueOrDefault(service.nombre_servicio, "")
                .toLowerCase(Locale.ROOT);
        return searchText.isEmpty() || name.contains(searchText);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
