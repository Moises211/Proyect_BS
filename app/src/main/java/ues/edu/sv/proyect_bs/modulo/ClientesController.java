package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.AdminViewController;
import ues.edu.sv.proyect_bs.AuthApiClient;
import ues.edu.sv.proyect_bs.MainActivity;
import ues.edu.sv.proyect_bs.OperacionesPendientes;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import ues.edu.sv.proyect_bs.modelos.TipoDocumento;

final class ClientesController implements AdminViewController {
    private final MainActivity host;
    private final AppDB database;
    private final AuthApiClient apiClient = new AuthApiClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ClientRow> rows = new ArrayList<>();
    private String searchText = "";
    private int statusFilter;

    ClientesController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                host.showScreen(R.layout.pantalla_control_citas));
        host.findViewById(R.id.action_add_client).setOnClickListener(v ->
                openClientForm(null));
        host.configureSpinner(R.id.dropdown_client_status, Color.DKGRAY,
                "Todos", "Activos", "Inactivos");
        ((android.widget.Spinner) host.findViewById(R.id.dropdown_client_status))
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
        loadClients();
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

    private void loadClients() {
        executor.execute(() -> {
            List<Cliente> clients = database.clienteDAO().obtenerTodos();
            List<ClientRow> loaded = new ArrayList<>();
            for (Cliente client : clients) {
                Persona person = client.idpersona == null
                        ? null : database.personaDAO().obtenerPorId(client.idpersona);
                CategoriaServicio specialty = client.preferencia_especialidad == null
                        ? null : database.categoriaServicioDAO()
                        .obtenerPorId(client.preferencia_especialidad);
                PuntosCliente points = database.puntosClienteDAO()
                        .obtenerPorId(client.idcliente);
                loaded.add(new ClientRow(client, person, specialty,
                        points == null ? 0 : points.cantidadpuntos));
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
        for (ClientRow row : rows) {
            if (Boolean.TRUE.equals(row.client.estado)) {
                activeCount++;
            }
        }
        TextView count = host.findViewById(R.id.clients_active_count);
        count.setText("Activos  " + activeCount + "/" + rows.size());

        LinearLayout list = host.findViewById(R.id.clients_list);
        list.removeAllViews();
        for (ClientRow row : rows) {
            if (!matches(row)) {
                continue;
            }
            View item = LayoutInflater.from(host).inflate(
                    R.layout.item_admin_client, list, false);
            list.addView(item);
            TextView avatar = item.findViewById(R.id.client_avatar);
            TextView name = item.findViewById(R.id.client_name);
            TextView specialty = item.findViewById(R.id.client_specialty);
            TextView phone = item.findViewById(R.id.client_phone);
            TextView points = item.findViewById(R.id.client_points);
            String names = row.person == null ? "" : safe(row.person.nombres);
            String lastNames = row.person == null ? "" : safe(row.person.apellidos);
            String fullName = (names + " " + lastNames).trim();
            name.setText(fullName.isEmpty() ? "Sin nombre" : fullName);
            avatar.setText(initials(names, lastNames));
            specialty.setText(row.specialty == null ? "Sin especialidad"
                    : safe(row.specialty.nombre));
            phone.setText(row.person == null || safe(row.person.numero_celular).isEmpty()
                    ? "Sin teléfono" : row.person.numero_celular);
            points.setText(row.points + "Pts");
            host.applyDarkModeToView(item);
            if (!Boolean.TRUE.equals(row.client.estado)) {
                host.setDisabledCard(item, -1);
            }

            item.findViewById(R.id.action_edit_client).setOnClickListener(v ->
                    openClientForm(row));
            item.findViewById(R.id.action_client_appointments).setOnClickListener(v ->
                    host.showScreen(R.layout.pantalla_citas_cliente));
        }
    }

    private void openClientForm(ClientRow row) {
        executor.execute(() -> {
            List<TipoDocumento> documents =
                    database.tipoDocumentoDAO().obtenerTodos();
            List<CategoriaServicio> specialties =
                    database.categoriaServicioDAO().obtenerTodos();
            host.runOnUiThread(() -> host.showClientForm(row != null,
                    row == null ? null : row.client,
                    row == null ? null : row.person,
                    documents, specialties,
                    (names, lastNames, documentId, document, email, phone, birthDate,
                     password, active, specialtyId) -> {
                        executor.execute(() -> {
                            boolean editing = row != null;
                            Persona localPerson = new Persona();
                            Cliente localClient = new Cliente();
                            if (editing) {
                                localPerson.idpersona = row.person.idpersona;
                                localClient.idcliente = row.client.idcliente;
                                localClient.estado = active;
                            } else {
                                localPerson.idpersona = nextTemporaryPersonId();
                                localClient.idcliente = nextTemporaryClientId();
                                localClient.estado = active;
                            }
                            localPerson.nombres = names;
                            localPerson.apellidos = lastNames;
                            localPerson.iddocumento = documentId;
                            localPerson.identificadordoc = document;
                            localPerson.correo_electronico = email;
                            localPerson.numero_celular = phone;
                            localPerson.fecha_nacimiento = birthDate;
                            localClient.idpersona = localPerson.idpersona;
                            localClient.preferencia_especialidad = specialtyId;
                            database.personaDAO().insertar(localPerson);
                            database.clienteDAO().insertar(localClient);
                            try {
                                JSONObject payload = new JSONObject()
                                        .put("clientId", localClient.idcliente)
                                        .put("personId", localPerson.idpersona)
                                        .put("documentId", documentId)
                                        .put("specialtyId", specialtyId)
                                        .put("names", names)
                                        .put("lastNames", lastNames)
                                        .put("document", document)
                                        .put("email", email)
                                        .put("phone", phone)
                                        .put("birthDate", birthDate)
                                        .put("password", password)
                                        .put("estado", Boolean.TRUE.equals(localClient.estado));
                                OperacionesPendientes.guardarJson(host, "cliente",
                                        editing ? "PUT" : "POST",
                                        localClient.idcliente, payload.toString());
                            } catch (Exception ignored) {
                            }
                            CategoriaServicio localSpecialty =
                                    database.categoriaServicioDAO().obtenerPorId(specialtyId);
                            host.runOnUiThread(() -> {
                                if (!editing) {
                                    rows.add(new ClientRow(localClient, localPerson,
                                            localSpecialty, 0));
                                } else {
                                    row.person.nombres = names;
                                    row.person.apellidos = lastNames;
                                    row.person.iddocumento = documentId;
                                    row.person.identificadordoc = document;
                                    row.person.correo_electronico = email;
                                    row.person.numero_celular = phone;
                                    row.person.fecha_nacimiento = birthDate;
                                    row.client.estado = active;
                                    row.client.preferencia_especialidad = specialtyId;
                                    row.specialty = localSpecialty;
                                }
                                render();
                            });

                            String token = SessionManager.getToken(host);
                            apiClient.saveClient(token, editing,
                                    localClient.idcliente, localPerson.idpersona,
                                    documentId, specialtyId, names, lastNames, document,
                                    email, phone, birthDate, password, localClient.estado,
                                    new AuthApiClient.ClientFormCallback() {
                                        @Override
                                        public void onSuccess(long clientId, long personId) {
                                            executor.execute(() -> {
                                                if (!editing) {
                                                    Cliente temporaryClient =
                                                            database.clienteDAO()
                                                                    .obtenerPorId(localClient.idcliente);
                                                    Persona temporaryPerson =
                                                            database.personaDAO()
                                                                    .obtenerPorId(localPerson.idpersona);
                                                    if (temporaryClient != null) {
                                                        database.clienteDAO().eliminar(temporaryClient);
                                                    }
                                                    if (temporaryPerson != null) {
                                                        database.personaDAO().eliminar(temporaryPerson);
                                                    }
                                                }
                                                Persona savedPerson = localPerson;
                                                Cliente savedClient = localClient;
                                                savedPerson.idpersona = personId;
                                                savedClient.idcliente = clientId;
                                                savedClient.idpersona = personId;
                                                database.personaDAO().insertar(savedPerson);
                                                database.clienteDAO().insertar(savedClient);
                                                database.operacionPendienteDAO()
                                                        .eliminarPorOperacion(
                                                                "cliente",
                                                                editing ? "PUT" : "POST",
                                                                localClient.idcliente);
                                                host.runOnUiThread(() -> {
                                                    render();
                                                    Toast.makeText(host,
                                                            editing
                                                                    ? "Cliente actualizado correctamente."
                                                                    : "Cliente agregado correctamente.",
                                                            Toast.LENGTH_LONG).show();
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

    private boolean matches(ClientRow row) {
        boolean active = Boolean.TRUE.equals(row.client.estado);
        if (statusFilter == 1 && !active || statusFilter == 2 && active) {
            return false;
        }
        String text = (safe(row.person == null ? null : row.person.nombres) + " "
                + safe(row.person == null ? null : row.person.apellidos) + " "
                + safe(row.specialty == null ? null : row.specialty.nombre))
                .toLowerCase(Locale.ROOT);
        return searchText.isEmpty() || text.contains(searchText);
    }

    private String initials(String names, String lastNames) {
        String first = names.isEmpty() ? "" : names.substring(0, 1);
        String last = lastNames.isEmpty() ? "" : lastNames.substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private long nextTemporaryPersonId() {
        long minimum = -1L;
        for (ClientRow row : rows) {
            if (row.person != null && row.person.idpersona < minimum) {
                minimum = row.person.idpersona;
            }
        }
        return minimum - 1L;
    }

    private long nextTemporaryClientId() {
        long minimum = -1L;
        for (ClientRow row : rows) {
            if (row.client != null && row.client.idcliente < minimum) {
                minimum = row.client.idcliente;
            }
        }
        return minimum - 1L;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class ClientRow {
        final Cliente client;
        final Persona person;
        CategoriaServicio specialty;
        final int points;

        ClientRow(Cliente client, Persona person, CategoriaServicio specialty, int points) {
            this.client = client;
            this.person = person;
            this.specialty = specialty;
            this.points = points;
        }
    }
}
