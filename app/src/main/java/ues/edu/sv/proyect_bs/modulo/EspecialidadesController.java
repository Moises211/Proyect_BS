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
                            .setPositiveButton("Eliminar", (dialog, which) ->
                                    apiClient.deleteSpecialty(SessionManager.getToken(host),
                                            specialty.idcategoria_servicio,
                                            new AuthApiClient.ServiceActionCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    executor.execute(() -> {
                                                        database.categoriaServicioDAO()
                                                                .eliminar(specialty);
                                                        host.runOnUiThread(() -> {
                                                            rows.remove(specialty);
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
                            .show());
        }
    }

    private void openSpecialtyForm(CategoriaServicio specialty) {
        host.showSpecialtyForm(specialty != null, specialty, value ->
                apiClient.saveSpecialty(SessionManager.getToken(host), specialty != null, value,
                        new AuthApiClient.SpecialtyFormCallback() {
                            @Override
                            public void onSuccess(CategoriaServicio saved) {
                                executor.execute(() -> {
                                    database.categoriaServicioDAO().insertar(saved);
                                    host.runOnUiThread(() -> {
                                        if (specialty == null) {
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
                        }));
    }
}
