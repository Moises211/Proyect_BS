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
                    executor.execute(() -> {
                        database.serviciosDAO().actualizar(service);
                        OperacionesPendientes.guardar(host, "servicio",
                                "PUT", service.idservicio, service);
                    });
                    host.runOnUiThread(() -> {
                        active.setOnCheckedChangeListener((button, checked) ->
                                showStatusConfirmation(service, item, active, checked));
                        render();
                    });
                    apiClient.updateServiceStatus(SessionManager.getToken(host), service,
                            new AuthApiClient.ServiceActionCallback() {
                                @Override
                                public void onSuccess() {
                                    executor.execute(() -> {
                                        database.operacionPendienteDAO().eliminarPorOperacion(
                                                "servicio", "PUT", service.idservicio);
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    host.runOnUiThread(() ->
                                            host.createLightDialogBuilder()
                                                    .setTitle("Guardado local")
                                                    .setMessage("El cambio quedó pendiente y se enviará cuando vuelva la conexión.")
                                                    .setPositiveButton("Aceptar", null)
                                                    .show());
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
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    executor.execute(() -> {
                        database.serviciosDAO().eliminar(service);
                        OperacionesPendientes.guardar(host, "servicio",
                                "DELETE", service.idservicio, null);
                        host.runOnUiThread(() -> {
                            rows.remove(service);
                            render();
                        });
                    });
                    apiClient.deleteService(SessionManager.getToken(host),
                            service.idservicio,
                            new AuthApiClient.ServiceActionCallback() {
                                @Override
                                public void onSuccess() {
                                    executor.execute(() ->
                                            database.operacionPendienteDAO()
                                                    .eliminarPorOperacion(
                                                            "servicio", "DELETE",
                                                            service.idservicio));
                                }

                                @Override
                                public void onError(String message) {
                                    host.runOnUiThread(() ->
                                            host.createLightDialogBuilder()
                                                    .setTitle("Guardado local")
                                                    .setMessage("Se eliminará en la API cuando vuelva la conexión.")
                                                    .setPositiveButton("Aceptar", null)
                                                    .show());
                                }
                            });
                })
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
                            executor.execute(() -> {
                                boolean editing = service != null;
                                Servicios local = new Servicios();
                                if (editing) {
                                    local.idservicio = service.idservicio;
                                } else {
                                    local.idservicio = nextTemporaryServiceId();
                                }
                                local.nombre_servicio = value.nombre_servicio;
                                local.precio = value.precio;
                                local.duracion = value.duracion;
                                local.estado = value.estado;
                                local.idcategoria_servicio = value.idcategoria_servicio;
                                database.serviciosDAO().insertar(local);
                                OperacionesPendientes.guardar(host, "servicio",
                                        editing ? "PUT" : "POST", local.idservicio, local);
                                host.runOnUiThread(() -> {
                                    if (!editing) {
                                        rows.add(local);
                                    } else {
                                        int index = rows.indexOf(service);
                                        if (index >= 0) {
                                            rows.set(index, local);
                                        }
                                    }
                                    render();
                                });

                                apiClient.saveService(SessionManager.getToken(host),
                                        editing, local, new AuthApiClient.ServiceFormCallback() {
                                            @Override
                                            public void onSuccess(Servicios saved) {
                                                executor.execute(() -> {
                                                    if (!editing) {
                                                        Servicios temporary =
                                                                database.serviciosDAO()
                                                                        .obtenerPorId(local.idservicio);
                                                        if (temporary != null) {
                                                            database.serviciosDAO().eliminar(temporary);
                                                        }
                                                    }
                                                    database.serviciosDAO().insertar(saved);
                                                    database.operacionPendienteDAO()
                                                            .eliminarPorOperacion(
                                                                    "servicio",
                                                                    editing ? "PUT" : "POST",
                                                                    local.idservicio);
                                                    host.runOnUiThread(() -> {
                                                        if (!editing) {
                                                            rows.remove(local);
                                                            rows.add(saved);
                                                        } else {
                                                            int index = rows.indexOf(local);
                                                            if (index >= 0) {
                                                                rows.set(index, saved);
                                                            }
                                                        }
                                                        render();
                                                    });
                                                });
                                            }

                                            @Override
                                            public void onError(String message) {
                                                host.runOnUiThread(() ->
                                                        host.createLightDialogBuilder()
                                                                .setTitle("Guardado local")
                                                                .setMessage("El cambio quedó pendiente y se enviará cuando vuelva la conexión.")
                                                                .setPositiveButton("Aceptar", null)
                                                                .show());
                                            }
                                        });
                            });
                    }));
        });
    }

    private long nextTemporaryServiceId() {
        long minimum = -1L;
        for (Servicios row : rows) {
            if (row.idservicio < minimum) {
                minimum = row.idservicio;
            }
        }
        return minimum - 1L;
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
