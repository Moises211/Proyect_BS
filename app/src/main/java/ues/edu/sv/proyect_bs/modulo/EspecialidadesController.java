package ues.edu.sv.proyect_bs;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;

final class EspecialidadesController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<CategoriaServicio> rows = new ArrayList<>();
    private String searchText = "";

    EspecialidadesController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        MaterialToolbar toolbar = host.findViewById(R.id.toolbar_admin_back);
        toolbar.setSubtitle("Administración Especialidades");
        toolbar.setNavigationOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
        host.findViewById(R.id.action_add_specialty).setOnClickListener(v ->
                openSpecialtyForm(null));
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
        loadSpecialties();
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

    private void loadSpecialties() {
        executor.execute(() -> {
            List<CategoriaServicio> loaded =
                    database.categoriaServicioDAO().obtenerTodos();
            host.runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                render();
            });
        });
    }

    private void render() {
        ((TextView) host.findViewById(R.id.specialties_count))
                .setText(rows.size() + " items");
        LinearLayout list = host.findViewById(R.id.specialties_list);
        list.removeAllViews();
        for (CategoriaServicio specialty : rows) {
            String name = specialty.nombre == null ? "" : specialty.nombre.trim();
            if (!searchText.isEmpty()
                    && !name.toLowerCase(Locale.ROOT).contains(searchText)) {
                continue;
            }
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_specialty, list, false);
            list.addView(item);
            ((TextView) item.findViewById(R.id.specialty_name))
                    .setText(name.isEmpty() ? "Sin nombre" : name);
            host.applyDarkModeToView(item);
            item.findViewById(R.id.action_edit_specialty).setOnClickListener(v ->
                    openSpecialtyForm(specialty));
            item.findViewById(R.id.action_delete_specialty).setOnClickListener(v ->
                    host.createLightDialogBuilder()
                            .setTitle("Eliminar especialidad")
                            .setMessage("¿Deseas eliminar esta especialidad?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                executor.execute(() -> {
                                    database.categoriaServicioDAO().eliminar(specialty);
                                    OperacionesPendientes.guardar(host, "categoria_servicio",
                                            "DELETE", specialty.idcategoria_servicio, null);
                                    host.runOnUiThread(() -> {
                                        rows.remove(specialty);
                                        render();
                                    });
                                    apiClient.deleteSpecialty(SessionManager.getToken(host),
                                            specialty.idcategoria_servicio,
                                            new AuthApiClient.ServiceActionCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    executor.execute(() ->
                                                            database.operacionPendienteDAO()
                                                                    .eliminarPorOperacion(
                                                                            "categoria_servicio", "DELETE",
                                                                            specialty.idcategoria_servicio));
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
                                });
                            })
                            .show());
        }
    }

    private void openSpecialtyForm(CategoriaServicio specialty) {
        host.showSpecialtyForm(specialty != null, specialty, value -> {
            executor.execute(() -> {
                boolean editing = specialty != null;
                CategoriaServicio local = new CategoriaServicio();
                if (editing) {
                    local.idcategoria_servicio = specialty.idcategoria_servicio;
                } else {
                    local.idcategoria_servicio = nextTemporaryId();
                }
                local.nombre = value.nombre;
                database.categoriaServicioDAO().insertar(local);
                OperacionesPendientes.guardar(host, "categoria_servicio",
                        editing ? "PUT" : "POST", local.idcategoria_servicio, local);
                host.runOnUiThread(() -> {
                    if (!editing) {
                        rows.add(local);
                    } else {
                        int index = rows.indexOf(specialty);
                        if (index >= 0) {
                            rows.set(index, local);
                        }
                    }
                    render();
                });
                apiClient.saveSpecialty(SessionManager.getToken(host), editing, local,
                        new AuthApiClient.SpecialtyFormCallback() {
                            @Override
                            public void onSuccess(CategoriaServicio saved) {
                                executor.execute(() -> {
                                    if (!editing) {
                                        CategoriaServicio temporary =
                                                database.categoriaServicioDAO()
                                                        .obtenerPorId(local.idcategoria_servicio);
                                        if (temporary != null) {
                                            database.categoriaServicioDAO().eliminar(temporary);
                                        }
                                    }
                                    database.categoriaServicioDAO().insertar(saved);
                                    database.operacionPendienteDAO().eliminarPorOperacion(
                                            "categoria_servicio",
                                            editing ? "PUT" : "POST",
                                            local.idcategoria_servicio);
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
        });
    }

    private long nextTemporaryId() {
        long minimum = -1L;
        for (CategoriaServicio row : rows) {
            if (row.idcategoria_servicio < minimum) {
                minimum = row.idcategoria_servicio;
            }
        }
        return minimum - 1L;
    }
}
