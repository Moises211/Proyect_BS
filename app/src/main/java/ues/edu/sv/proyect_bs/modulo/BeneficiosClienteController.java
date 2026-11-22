package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import ues.edu.sv.proyect_bs.modelos.BeneficiosCliente;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import ues.edu.sv.proyect_bs.modelos.Servicios;
import ues.edu.sv.proyect_bs.modelos.TarifaPuntos;

final class BeneficiosClienteController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Long, String> benefitTitles = new HashMap<>();
    private String searchText = "";
    private int statusFilter;
    private long clientId;

    BeneficiosClienteController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        clientId = host.getSelectedBenefitClientId();
        host.findViewById(R.id.toolbar_benefits_client).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_beneficios));
        host.configureSpinner(R.id.dropdown_benefit_status, Color.WHITE,
                "Todas", "Activas", "Canjeadas");
        ((android.widget.Spinner) host.findViewById(R.id.dropdown_benefit_status))
                .setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                       View view, int position, long id) {
                        statusFilter = position;
                        render();
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
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
        loadData();
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

    private void loadData() {
        executor.execute(() -> {
            Cliente client = database.clienteDAO().obtenerPorId(clientId);
            Persona person = client == null || client.idpersona == null
                    ? null : database.personaDAO().obtenerPorId(client.idpersona);
            PuntosCliente points = database.puntosClienteDAO().obtenerPorId(clientId);
            TarifaPuntos tariff =
                    database.tarifaPuntosDAO().obtenerPrimera();
            host.runOnUiThread(() -> {
                MaterialToolbar toolbar = host.findViewById(R.id.toolbar_benefits_client);
                String name = person == null ? "Cliente #" + clientId
                        : ((person.nombres == null ? "" : person.nombres.trim()) + " "
                        + (person.apellidos == null ? "" : person.apellidos.trim())).trim();
                toolbar.setSubtitle("Administración Beneficios " + name);
                if (tariff != null) {
                    TextView baseRate = findTextViewByDrawableHost();
                    if (baseRate != null) {
                        double amount = tariff.tarifamonetaria == null ? 1d : tariff.tarifamonetaria;
                        int pts = tariff.puntos == null ? 1 : tariff.puntos;
                        baseRate.setText("Gasto de $ " + String.format(Locale.US, "%.2f", amount)
                                + " = " + pts + " Pts.");
                    }
                }
                render();
            });
        });
    }

    private TextView findTextViewByDrawableHost() {
        View root = host.findViewById(android.R.id.content);
        return findTextByText(root, "Gasto de $");
    }

    private TextView findTextByText(View view, String prefix) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            if (text.getText() != null
                    && text.getText().toString().startsWith(prefix)) return text;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                TextView result = findTextByText(group.getChildAt(index), prefix);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void render() {
        executor.execute(() -> {
            List<Beneficios> benefits = database.beneficiosDAO().obtenerTodos();
            List<BeneficiosCliente> redeemed =
                    database.beneficiosClienteDAO().obtenerTodos();
            List<Servicios> services = database.serviciosDAO().obtenerTodos();
            List<CategoriaServicio> categories =
                    database.categoriaServicioDAO().obtenerTodos();
            Map<Long, String> resolvedTitles = new HashMap<>();
            for (Beneficios benefit : benefits) {
                resolvedTitles.put(benefit.idbeneficio,
                        resolveBenefitTitle(benefit, services, categories));
            }
            java.util.Set<Long> redeemedIds = new java.util.HashSet<>();
            for (BeneficiosCliente entry : redeemed) {
                if (entry.idcliente == clientId) redeemedIds.add(entry.idbeneficio);
            }
            host.runOnUiThread(() -> {
                benefitTitles.clear();
                benefitTitles.putAll(resolvedTitles);
                LinearLayout list = host.findViewById(R.id.benefits_client_list);
                list.removeAllViews();
                int activeCount = 0;
                for (Beneficios benefit : benefits) {
                    if (Boolean.TRUE.equals(benefit.estado)) activeCount++;
                    boolean isRedeemed = redeemedIds.contains(benefit.idbeneficio);
                    if (statusFilter == 1 && !Boolean.TRUE.equals(benefit.estado)) continue;
                    if (statusFilter == 2 && !isRedeemed) continue;
                    if (statusFilter == 1 && isRedeemed) continue;
                    if (!searchText.isEmpty()
                            && !benefitTitle(benefit).toLowerCase(Locale.ROOT).contains(searchText)) continue;
                    View item;
                    if (isRedeemed) {
                        item = LayoutInflater.from(host).inflate(
                                R.layout.item_admin_benefit_redeemed, list, false);
                        TextView title = item.findViewById(R.id.benefit_redeemed_title);
                        title.setText(benefitTitle(benefit));
                    } else if (Boolean.TRUE.equals(benefit.estado)) {
                        item = LayoutInflater.from(host).inflate(
                                R.layout.item_admin_benefit_available, list, false);
                        TextView title = item.findViewById(R.id.benefit_available_title);
                        TextView cost = item.findViewById(R.id.benefit_available_cost);
                        title.setText(benefitTitle(benefit));
                        cost.setText("Costo     "
                                + (benefit.costepuntos == null ? 0 : benefit.costepuntos)
                                + " Pts");
                        item.findViewById(R.id.action_redeem_benefit).setOnClickListener(v ->
                                redeemBenefit(benefit));
                        item.findViewById(R.id.action_remove_benefit).setOnClickListener(v ->
                                removeBenefit(benefit));
                    } else {
                        continue;
                    }
                    list.addView(item);
                    host.applyDarkModeToView(item);
                }
                TextView count = findTextViewWithPrefix("Activas");
                if (count != null) {
                    count.setText("Activas  " + activeCount + "/" + benefits.size());
                }
            });
        });
    }

    private TextView findTextViewWithPrefix(String prefix) {
        View root = host.findViewById(android.R.id.content);
        return findTextByText(root, prefix);
    }

    private String benefitTitle(Beneficios benefit) {
        String title = benefitTitles.get(benefit.idbeneficio);
        return title == null ? "Beneficio #" + benefit.idbeneficio : title;
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

    private void redeemBenefit(Beneficios benefit) {
        host.createLightDialogBuilder()
                .setTitle("Confirmar canje")
                .setMessage("¿Deseas canjear este beneficio?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Canjear", (dialog, which) -> {
                    executor.execute(() -> {
                        PuntosCliente points =
                                database.puntosClienteDAO().obtenerPorId(clientId);
                        int available = points == null || points.cantidadpuntos == null
                                ? 0 : points.cantidadpuntos;
                        int cost = benefit.costepuntos == null ? 0 : benefit.costepuntos;
                        if (available < cost) {
                            host.runOnUiThread(() -> Toast.makeText(host,
                                    "Puntos insuficientes para canjear este beneficio.",
                                    Toast.LENGTH_LONG).show());
                            return;
                        }
                        BeneficiosCliente redeemed = new BeneficiosCliente();
                        redeemed.idbeneficioscliente = nextTemporaryRedemptionId();
                        redeemed.idbeneficio = benefit.idbeneficio;
                        redeemed.idcliente = clientId;
                        redeemed.fechacanje = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.US).format(new Date());
                        database.beneficiosClienteDAO().insertar(redeemed);
                        PuntosCliente updated = points == null
                                ? new PuntosCliente() : points;
                        updated.idcliente = clientId;
                        updated.cantidadpuntos = available - cost;
                        database.puntosClienteDAO().insertar(updated);
                        OperacionesPendientes.guardar(host, "beneficio_cliente",
                                "POST", redeemed.idbeneficioscliente, redeemed);
                        host.runOnUiThread(() -> {
                            render();
                            Toast.makeText(host, "Beneficio canjeado correctamente.",
                                    Toast.LENGTH_LONG).show();
                        });
                        apiClient.saveBenefitClient(SessionManager.getToken(host),
                                redeemed, new AuthApiClient.BenefitClientFormCallback() {
                                    @Override public void onSuccess(BeneficiosCliente saved) {
                                        executor.execute(() -> {
                                            BeneficiosCliente temporary =
                                                    database.beneficiosClienteDAO()
                                                            .obtenerPorId(redeemed.idbeneficioscliente);
                                            if (temporary != null) {
                                                database.beneficiosClienteDAO().eliminar(temporary);
                                            }
                                            database.beneficiosClienteDAO().insertar(saved);
                                            database.operacionPendienteDAO()
                                                    .eliminarPorOperacion("beneficio_cliente",
                                                            "POST", redeemed.idbeneficioscliente);
                                            host.runOnUiThread(BeneficiosClienteController.this::render);
                                        });
                                    }
                                    @Override public void onError(String message) {}
                                });
                    });
                })
                .show();
    }

    private void removeBenefit(Beneficios benefit) {
        host.createLightDialogBuilder()
                .setTitle("Eliminar beneficio")
                .setMessage("¿Deseas eliminar este beneficio de la lista disponible?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) ->
                        Toast.makeText(host, "Beneficio eliminado de la lista.",
                                Toast.LENGTH_LONG).show())
                .show();
    }

    private long nextTemporaryRedemptionId() {
        long minimum = -1L;
        for (BeneficiosCliente entry :
                database.beneficiosClienteDAO().obtenerTodos()) {
            if (entry.idbeneficioscliente < minimum) minimum = entry.idbeneficioscliente;
        }
        return minimum - 1L;
    }
}
