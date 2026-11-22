package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.Servicios;
import ues.edu.sv.proyect_bs.modelos.TarifaPuntos;

final class BeneficiosAdminController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Beneficios> rows = new ArrayList<>();
    private final Map<Long, String> benefitTitles = new HashMap<>();
    private String searchText = "";
    private int statusFilter;

    BeneficiosAdminController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_benefits).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_cortes_servicios));
        host.configureSpinner(R.id.admin_benefit_status_spinner, Color.WHITE,
                "Todas", "Activas", "Inactivas");
        ((android.widget.Spinner) host.findViewById(R.id.admin_benefit_status_spinner))
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                       int position, long id) {
                        statusFilter = position;
                        render();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
        host.findViewById(R.id.action_add_admin_benefit).setOnClickListener(v ->
                openBenefitForm(null));
        host.findViewById(R.id.action_edit_base_rate).setOnClickListener(v ->
                host.showBaseRateForm());
        EditText search = findSearchInput();
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
        loadBenefits();
    }

    private EditText findSearchInput() {
        View field = host.findViewById(R.id.admin_search_field);
        if (field instanceof EditText) return (EditText) field;
        if (field instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) field;
            for (int index = 0; index < group.getChildCount(); index++) {
                View child = group.getChildAt(index);
                if (child instanceof EditText) return (EditText) child;
                if (child instanceof android.view.ViewGroup) {
                    android.view.ViewGroup nested = (android.view.ViewGroup) child;
                    for (int nestedIndex = 0; nestedIndex < nested.getChildCount(); nestedIndex++) {
                        if (nested.getChildAt(nestedIndex) instanceof EditText) {
                            return (EditText) nested.getChildAt(nestedIndex);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void loadBenefits() {
        executor.execute(() -> {
            List<Beneficios> loaded = database.beneficiosDAO().obtenerTodos();
            Map<Long, String> resolvedTitles = new HashMap<>();
            for (Beneficios benefit : loaded) {
                resolvedTitles.put(benefit.idbeneficio, resolveBenefitTitle(
                        benefit,
                        database.serviciosDAO().obtenerTodos(),
                        database.categoriaServicioDAO().obtenerTodos()));
            }
            host.runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                benefitTitles.clear();
                benefitTitles.putAll(resolvedTitles);
                renderBaseRate();
                render();
            });
        });
    }

    private String resolveBenefitTitle(Beneficios benefit,
                                       List<Servicios> services,
                                       List<CategoriaServicio> categories) {
        if (benefit.servicio != null && benefit.servicio > 0) {
            for (Servicios service : services) {
                if (benefit.servicio != null
                        && service.idservicio == benefit.servicio
                        && service.nombre_servicio != null
                        && !service.nombre_servicio.trim().isEmpty()) {
                    return service.nombre_servicio;
                }
            }
        }
        if (benefit.categoria != null && benefit.categoria > 0) {
            for (CategoriaServicio category : categories) {
                if (category.idcategoria_servicio == benefit.categoria
                        && category.nombre != null) {
                    return category.nombre;
                }
            }
        }
        return "Beneficio #" + benefit.idbeneficio;
    }

    private void renderBaseRate() {
        executor.execute(() -> {
            TarifaPuntos tariff =
                    database.tarifaPuntosDAO().obtenerPrimera();
            host.runOnUiThread(() -> {
                TextView rate = host.findViewById(R.id.benefit_base_rate);
                if (tariff == null) {
                    rate.setText("Gasto de $1.00 = 1 Pts.");
                } else {
                    double amount = tariff.tarifamonetaria == null ? 1d : tariff.tarifamonetaria;
                    int points = tariff.puntos == null ? 1 : tariff.puntos;
                    rate.setText("Gasto de $" + String.format(Locale.US, "%.2f", amount)
                            + " = " + points + " Pts.");
                }
            });
        });
    }

    private void render() {
        int activeCount = 0;
        for (Beneficios benefit : rows) {
            if (Boolean.TRUE.equals(benefit.estado)) activeCount++;
        }
        TextView count = host.findViewById(R.id.admin_benefits_active_count);
        count.setText("Activas " + activeCount + "/" + rows.size());

        LinearLayout list = host.findViewById(R.id.admin_benefits_list);
        list.removeAllViews();
        for (Beneficios benefit : rows) {
            if (!matches(benefit)) continue;
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_benefit_admin, list, false);
            list.addView(item);
            TextView title = item.findViewById(R.id.benefit_title);
            TextView cost = item.findViewById(R.id.benefit_cost);
            title.setText(benefitTitle(benefit));
            cost.setText("Costo    " + (benefit.costepuntos == null ? 0 : benefit.costepuntos) + " Pts");
            SwitchMaterial active = item.findViewById(R.id.admin_benefit_active_switch);
            active.setOnCheckedChangeListener(null);
            active.setChecked(Boolean.TRUE.equals(benefit.estado));
            if (!Boolean.TRUE.equals(benefit.estado)) {
                host.setDisabledCard(item, R.id.admin_benefit_active_switch);
            } else {
                host.applyDarkModeToView(item);
            }
            active.setOnCheckedChangeListener((button, checked) ->
                    changeStatus(benefit, active, checked));
            item.findViewById(R.id.action_edit_benefit).setOnClickListener(v ->
                    openBenefitForm(benefit));
            item.findViewById(R.id.action_delete_benefit).setOnClickListener(v ->
                    deleteBenefit(benefit));
        }
    }

    private String benefitTitle(Beneficios benefit) {
        String title = benefitTitles.get(benefit.idbeneficio);
        return title == null ? "Beneficio #" + benefit.idbeneficio : title;
    }

    private boolean matches(Beneficios benefit) {
        boolean active = Boolean.TRUE.equals(benefit.estado);
        if (statusFilter == 1 && !active) return false;
        if (statusFilter == 2 && active) return false;
        return searchText.isEmpty()
                || benefitTitle(benefit).toLowerCase(Locale.ROOT).contains(searchText);
    }

    private void changeStatus(Beneficios benefit, SwitchMaterial active, boolean checked) {
        active.setOnCheckedChangeListener(null);
        active.setChecked(Boolean.TRUE.equals(benefit.estado));
        host.createLightDialogBuilder()
                .setTitle("Cambiar estado")
                .setMessage("¿Estás seguro de que quieres "
                        + (checked ? "habilitar" : "deshabilitar") + " este beneficio?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    benefit.estado = checked;
                    executor.execute(() -> {
                        database.beneficiosDAO().actualizar(benefit);
                        OperacionesPendientes.guardar(host, "beneficio",
                                "PUT", benefit.idbeneficio, benefit);
                    });
                    host.runOnUiThread(() -> {
                        render();
                        Toast.makeText(host, "Estado del beneficio actualizado correctamente.",
                                Toast.LENGTH_LONG).show();
                    });
                    apiClient.updateBenefitStatus(SessionManager.getToken(host), benefit,
                            new AuthApiClient.BenefitActionCallback() {
                                @Override public void onSuccess() {
                                    executor.execute(() -> database.operacionPendienteDAO()
                                            .eliminarPorOperacion("beneficio", "PUT",
                                                    benefit.idbeneficio));
                                }
                                @Override public void onError(String message) {
                                    host.runOnUiThread(() ->
                                            Toast.makeText(host, message,
                                                    Toast.LENGTH_LONG).show());
                                }
                            });
                })
                .show();
    }

    private void deleteBenefit(Beneficios benefit) {
        host.createLightDialogBuilder()
                .setTitle("Eliminar beneficio")
                .setMessage("¿Estás seguro de que quieres eliminar este beneficio?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    executor.execute(() -> {
                        database.beneficiosDAO().eliminar(benefit);
                        OperacionesPendientes.guardar(host, "beneficio",
                                "DELETE", benefit.idbeneficio, null);
                        host.runOnUiThread(() -> {
                            rows.remove(benefit);
                            render();
                            Toast.makeText(host, "Beneficio eliminado correctamente.",
                                    Toast.LENGTH_LONG).show();
                        });
                    });
                    apiClient.deleteBenefit(SessionManager.getToken(host),
                            benefit.idbeneficio, new AuthApiClient.BenefitActionCallback() {
                                @Override public void onSuccess() {
                                    executor.execute(() -> database.operacionPendienteDAO()
                                            .eliminarPorOperacion("beneficio", "DELETE",
                                                    benefit.idbeneficio));
                                }
                                @Override public void onError(String message) {
                                    executor.execute(() -> {
                                        database.beneficiosDAO().insertar(benefit);
                                        host.runOnUiThread(() -> {
                                            if (!rows.contains(benefit)) rows.add(benefit);
                                            render();
                                            Toast.makeText(host, message,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    });
                                }
                            });
                })
                .show();
    }

    private void openBenefitForm(Beneficios benefit) {
        executor.execute(() -> {
            List<CategoriaServicio> categories =
                    database.categoriaServicioDAO().obtenerTodos();
            List<Servicios> services = database.serviciosDAO().obtenerTodos();
            host.runOnUiThread(() -> host.showBenefitForm(
                    benefit != null, benefit, categories, services,
                    (value, onSuccess, onFailure) -> executor.execute(() -> {
                        boolean editing = benefit != null;
                        if (value.categoria == null) {
                            host.runOnUiThread(() -> {
                                Toast.makeText(host,
                                        "Selecciona una categoría para el beneficio.",
                                        Toast.LENGTH_LONG).show();
                                onFailure.run();
                            });
                            return;
                        }
                        for (Beneficios existing : rows) {
                            if (editing && existing.idbeneficio == benefit.idbeneficio) continue;
                            if (existing.categoria != null
                                    && existing.categoria.equals(value.categoria)
                                    && java.util.Objects.equals(existing.servicio, value.servicio)) {
                                host.runOnUiThread(() -> {
                                    Toast.makeText(host,
                                            value.servicio == null
                                                    ? "Ya existe un beneficio para esta categoría."
                                                    : "Ya existe un beneficio para esta categoría y servicio.",
                                            Toast.LENGTH_LONG).show();
                                    onFailure.run();
                                });
                                return;
                            }
                        }
                        Beneficios local = copyBenefit(editing ? benefit : null);
                        if (!editing) local.idbeneficio = nextTemporaryId();
                        local.estado = value.estado;
                        local.costepuntos = value.costepuntos;
                        local.categoria = value.categoria;
                        local.servicio = value.servicio;
                        apiClient.saveBenefit(SessionManager.getToken(host),
                                editing, local, new AuthApiClient.BenefitFormCallback() {
                                    @Override public void onSuccess(Beneficios saved) {
                                        executor.execute(() -> {
                                            if (!editing) {
                                                Beneficios temporary =
                                                        database.beneficiosDAO()
                                                                .obtenerPorId(local.idbeneficio);
                                                if (temporary != null) {
                                                    database.beneficiosDAO().eliminar(temporary);
                                                }
                                            }
                                            database.beneficiosDAO().insertar(saved);
                                            database.operacionPendienteDAO()
                                                    .eliminarPorOperacion("beneficio",
                                                            editing ? "PUT" : "POST",
                                                            local.idbeneficio);
                                            host.runOnUiThread(() -> {
                                                if (editing) {
                                                    replaceBenefitRow(local.idbeneficio, saved);
                                                } else {
                                                    removeBenefitRowsById(local.idbeneficio);
                                                    rows.add(saved);
                                                }
                                                deduplicateBenefitRows();
                                                loadResolvedTitles(saved);
                                                Toast.makeText(host,
                                                        editing
                                                                ? "Beneficio actualizado correctamente."
                                                                : "Beneficio agregado correctamente.",
                                                        Toast.LENGTH_LONG).show();
                                                onSuccess.run();
                                            });
                                        });
                                    }
                                    @Override public void onError(String message) {
                                        executor.execute(() -> {
                                            if (editing) {
                                                database.beneficiosDAO().insertar(
                                                        copyBenefit(benefit));
                                            } else {
                                                Beneficios temporary =
                                                        database.beneficiosDAO()
                                                                .obtenerPorId(local.idbeneficio);
                                                if (temporary != null) {
                                                    database.beneficiosDAO().eliminar(temporary);
                                                }
                                            }
                                            host.runOnUiThread(() -> {
                                                if (editing) {
                                                    replaceBenefitRow(local.idbeneficio, benefit);
                                                } else {
                                                    removeBenefitRowsById(local.idbeneficio);
                                                }
                                                render();
                                                Toast.makeText(host, message,
                                                        Toast.LENGTH_LONG).show();
                                                onFailure.run();
                                            });
                                        });
                                    }
                                });
                    })));
        });
    }

    private long nextTemporaryId() {
        long minimum = -1L;
        for (Beneficios benefit : rows) {
            if (benefit.idbeneficio < minimum) minimum = benefit.idbeneficio;
        }
        return minimum - 1L;
    }

    private void replaceBenefitRow(long benefitId, Beneficios replacement) {
        int index = -1;
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (rows.get(i).idbeneficio == benefitId) {
                if (index < 0) index = i;
                rows.remove(i);
            }
        }
        if (index < 0) index = rows.size();
        rows.add(index, replacement);
    }

    private void removeBenefitRowsById(long benefitId) {
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (rows.get(i).idbeneficio == benefitId) rows.remove(i);
        }
    }

    private void deduplicateBenefitRows() {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (int i = rows.size() - 1; i >= 0; i--) {
            long id = rows.get(i).idbeneficio;
            if (!seen.add(id)) rows.remove(i);
        }
    }

    private void loadResolvedTitles(Beneficios changed) {
        executor.execute(() -> {
            List<Servicios> services = database.serviciosDAO().obtenerTodos();
            List<CategoriaServicio> categories =
                    database.categoriaServicioDAO().obtenerTodos();
            Map<Long, String> resolvedTitles = new HashMap<>();
            for (Beneficios row : rows) {
                resolvedTitles.put(row.idbeneficio,
                        resolveBenefitTitle(row, services, categories));
            }
            host.runOnUiThread(() -> {
                benefitTitles.clear();
                benefitTitles.putAll(resolvedTitles);
                render();
            });
        });
    }

    private Beneficios copyBenefit(Beneficios source) {
        Beneficios copy = new Beneficios();
        if (source != null) {
            copy.idbeneficio = source.idbeneficio;
            copy.estado = source.estado;
            copy.costepuntos = source.costepuntos;
            copy.categoria = source.categoria;
            copy.servicio = source.servicio;
        }
        return copy;
    }
}
