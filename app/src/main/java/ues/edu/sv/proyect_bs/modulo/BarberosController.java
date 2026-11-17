package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.Persona;

final class BarberosController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<BarberRow> rows = new ArrayList<>();
    private String searchText = "";
    private int statusFilter;

    BarberosController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_barbers).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_personal_turnos));
        host.configureSpinner(R.id.barbers_status_spinner, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        ((android.widget.Spinner) host.findViewById(R.id.barbers_status_spinner))
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
        host.findViewById(R.id.action_add_barber).setOnClickListener(v ->
                openBarberForm(null));
        EditText search = findSearchInput();
        if (search != null) {
            search.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchText = s.toString().trim().toLowerCase(Locale.ROOT);
                    render();
                }

                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        loadBarbers();
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

    private void loadBarbers() {
        executor.execute(() -> {
            List<Barberos> barbers = database.barberosDAO().obtenerTodos();
            List<BarberRow> loaded = new ArrayList<>();
            for (Barberos barber : barbers) {
                Persona person = barber.idpersona == null
                        ? null : database.personaDAO().obtenerPorId(barber.idpersona);
                CategoriaServicio category = barber.especialidad == null
                        ? null : database.categoriaServicioDAO().obtenerPorId(barber.especialidad);
                loaded.add(new BarberRow(barber, person, category));
            }
            host.runOnUiThread(() -> {
                rows.clear();
                rows.addAll(loaded);
                render();
            });
        });
    }

    private void render() {
        int activeCount = 0;
        for (BarberRow row : rows) {
            if (Boolean.TRUE.equals(row.barber.estado)) {
                activeCount++;
            }
        }
        TextView activeCountView = host.findViewById(R.id.barbers_active_count);
        activeCountView.setText("Activos  " + activeCount + "/" + rows.size());

        LinearLayout list = host.findViewById(R.id.barbers_list);
        list.removeAllViews();
        for (BarberRow row : rows) {
            if (!matches(row)) {
                continue;
            }
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_barber, list, false);
            list.addView(item);
            TextView avatar = item.findViewById(R.id.barber_avatar);
            TextView name = item.findViewById(R.id.barber_name);
            TextView specialty = item.findViewById(R.id.barber_specialty);
            TextView phone = item.findViewById(R.id.barber_phone);
            String names = row.person == null ? "" : safe(row.person.nombres);
            String lastNames = row.person == null ? "" : safe(row.person.apellidos);
            String fullName = (names + " " + lastNames).trim();
            name.setText(fullName.isEmpty() ? "Sin nombre" : fullName);
            avatar.setText(initials(names, lastNames));
            specialty.setText(row.category == null ? "Sin especialidad"
                    : safe(row.category.nombre));
            phone.setText(row.person == null || safe(row.person.numero_celular).isEmpty()
                    ? "Sin teléfono" : row.person.numero_celular);
            SwitchMaterial active = item.findViewById(R.id.barber_active_switch);
            boolean isActive = Boolean.TRUE.equals(row.barber.estado);
            active.setOnCheckedChangeListener(null);
            active.setChecked(isActive);
            if (isActive) {
                restoreActiveStyle(item);
            } else {
                host.setDisabledCard(item, R.id.barber_active_switch);
                host.applyDarkModeToView(item);
            }
            active.setOnCheckedChangeListener((button, checked) ->
                    showStatusConfirmation(row, item, active, checked));
            item.findViewById(R.id.action_edit_barber).setOnClickListener(v ->
                    openBarberForm(row));
        }
    }

    private void openBarberForm(BarberRow row) {
        executor.execute(() -> {
            List<ues.edu.sv.proyect_bs.modelos.TipoDocumento> documents =
                    database.tipoDocumentoDAO().obtenerTodos();
            List<CategoriaServicio> specialties =
                    database.categoriaServicioDAO().obtenerTodos();
            host.runOnUiThread(() -> host.showBarberForm(row != null, 
                    row == null ? null : row.barber,
                    row == null ? null : row.person,
                    documents, specialties,
                    (names, lastNames, documentId, document, email, phone, password,
                            specialtyId) -> {
                        String token = SessionManager.getToken(host);
                        apiClient.saveBarber(token, row != null, 
                                row == null ? 0L : row.barber.idbarberos,
                                row == null || row.person == null ? 0L : row.person.idpersona,
                                documentId, specialtyId, names, lastNames, document, email, phone,
                                password, new AuthApiClient.BarberFormCallback() {
                                    @Override
                                    public void onSuccess(long barberId, long personId) {
                                        executor.execute(() -> {
                                            Persona savedPerson = row == null
                                                    ? new Persona() : row.person;
                                            Barberos savedBarber = row == null
                                                    ? new Barberos() : row.barber;
                                            savedPerson.idpersona = personId;
                                            savedPerson.nombres = names;
                                            savedPerson.apellidos = lastNames;
                                            savedPerson.iddocumento = documentId;
                                            savedPerson.identificadordoc = document;
                                            savedPerson.correo_electronico = email;
                                            savedPerson.numero_celular = phone;
                                            savedBarber.idbarberos = barberId;
                                            savedBarber.idpersona = personId;
                                            savedBarber.correo_electronico = email;
                                            savedBarber.especialidad = specialtyId;
                                            savedBarber.estado = row == null
                                                    ? Boolean.TRUE : row.barber.estado;
                                            database.personaDAO().insertar(savedPerson);
                                            database.barberosDAO().insertar(savedBarber);
                                            CategoriaServicio savedSpecialty =
                                                    database.categoriaServicioDAO()
                                                            .obtenerPorId(specialtyId);
                                            host.runOnUiThread(() -> {
                                                if (row == null) {
                                                    rows.add(new BarberRow(savedBarber, savedPerson,
                                                            savedSpecialty));
                                                } else {
                                                    row.category = savedSpecialty;
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

    private void showStatusConfirmation(BarberRow row, View item, SwitchMaterial active,
                                        boolean newState) {
        active.setOnCheckedChangeListener(null);
        active.setChecked(Boolean.TRUE.equals(row.barber.estado));
        String action = newState ? "habilitar" : "deshabilitar";
        host.createLightDialogBuilder()
                .setTitle("Cambiar estado")
                .setMessage("¿Estás seguro de que quieres " + action + " a este barbero?")
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    active.setOnCheckedChangeListener(null);
                    active.setChecked(Boolean.TRUE.equals(row.barber.estado));
                    active.setOnCheckedChangeListener((button, checked) ->
                            showStatusConfirmation(row, item, active, checked));
                })
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    Boolean previousState = row.barber.estado;
                    row.barber.estado = newState;
                    String token = SessionManager.getToken(host);
                    apiClient.updateBarberStatus(token, row.barber,
                            new AuthApiClient.BarberStatusCallback() {
                                @Override
                                public void onSuccess() {
                                    executor.execute(() -> {
                                        database.barberosDAO().actualizar(row.barber);
                                        host.runOnUiThread(() -> {
                                            active.setOnCheckedChangeListener((button, checked) ->
                                                    showStatusConfirmation(row, item, active, checked));
                                            render();
                                        });
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    row.barber.estado = previousState;
                                    active.setOnCheckedChangeListener(null);
                                    active.setChecked(Boolean.TRUE.equals(previousState));
                                    active.setOnCheckedChangeListener((button, checked) ->
                                            showStatusConfirmation(row, item, active, checked));
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

    private void restoreActiveStyle(View item) {
        TextView avatar = item.findViewById(R.id.barber_avatar);
        TextView name = item.findViewById(R.id.barber_name);
        TextView specialty = item.findViewById(R.id.barber_specialty);
        TextView phone = item.findViewById(R.id.barber_phone);
        View edit = item.findViewById(R.id.action_edit_barber);
        ((com.google.android.material.card.MaterialCardView) item)
                .setCardBackgroundColor(Color.WHITE);
        name.setTextColor(Color.rgb(37, 48, 71));
        specialty.setTextColor(Color.rgb(20, 122, 62));
        phone.setTextColor(Color.rgb(25, 122, 138));
        specialty.setCompoundDrawableTintList(
                android.content.res.ColorStateList.valueOf(Color.rgb(20, 122, 62)));
        phone.setCompoundDrawableTintList(
                android.content.res.ColorStateList.valueOf(Color.rgb(25, 122, 138)));
        avatar.setTextColor(Color.rgb(37, 48, 71));
        avatar.setBackgroundResource(R.drawable.bg_module_profile);
        avatar.setBackgroundTintList(null);
        edit.setAlpha(1f);
        host.applyDarkModeToView(item);
    }

    private boolean matches(BarberRow row) {
        boolean active = Boolean.TRUE.equals(row.barber.estado);
        if (statusFilter == 1 && !active || statusFilter == 2 && active) {
            return false;
        }
        String text = (safe(row.person == null ? null : row.person.nombres) + " "
                + safe(row.person == null ? null : row.person.apellidos) + " "
                + safe(row.category == null ? null : row.category.nombre)).toLowerCase(Locale.ROOT);
        return searchText.isEmpty() || text.contains(searchText);
    }

    private String initials(String names, String lastNames) {
        String first = names.isEmpty() ? "" : names.substring(0, 1);
        String last = lastNames.isEmpty() ? "" : lastNames.substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class BarberRow {
        final Barberos barber;
        final Persona person;
        CategoriaServicio category;

        BarberRow(Barberos barber, Persona person, CategoriaServicio category) {
            this.barber = barber;
            this.person = person;
            this.category = category;
        }
    }
}
