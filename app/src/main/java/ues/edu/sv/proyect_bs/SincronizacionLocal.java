package ues.edu.sv.proyect_bs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.*;

final class SincronizacionLocal {
    private static final String TAG = "SincronizacionLocal";

    interface Callback {
        void onComplete(int recursosSincronizados, int recursosFallidos);
    }

    interface LocalProfileCallback {
        void onResult(boolean available);
    }

    private final AppDB database;
    private final AuthApiClient apiClient;
    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    SincronizacionLocal(Context context) {
        database = AppDB.getInstance(context);
        apiClient = new AuthApiClient();
    }

    void sincronizar(String token, Callback callback) {
        executor.execute(() -> {
            int correctos = 0;
            int fallidos = 0;
            correctos += sync("/personas", token, this::savePersonas);
            correctos += sync("/admins", token, this::saveAdmins);
            correctos += sync("/barberos", token, this::saveBarberos);
            correctos += sync("/clientes", token, this::saveClientes);
            correctos += sync("/categorias-servicio", token, this::saveCategorias);
            correctos += sync("/servicios", token, this::saveServicios);
            correctos += sync("/beneficios", token, this::saveBeneficios);
            correctos += sync("/beneficios-cliente", token, this::saveBeneficiosCliente);
            correctos += sync("/citas", token, this::saveCitas);
            correctos += sync("/cronogramas-barbero", token, this::saveCronogramas);
            correctos += sync("/horarios-atencion-base", token, this::saveHorariosBase);
            correctos += sync("/horarios-atencion-diarios", token, this::saveHorariosDiarios);
            correctos += sync("/horarios-dia-base", token, this::saveHorariosDia);
            correctos += sync("/promociones", token, this::savePromociones);
            correctos += sync("/puntos-cliente", token, this::savePuntos);
            correctos += sync("/tarifas-puntos", token, this::saveTarifas);
            correctos += sync("/tipos-documento", token, this::saveTiposDocumento);
            correctos += sync("/tipos-promocion", token, this::saveTiposPromocion);
            correctos += sync("/turnos-base-barbero", token, this::saveTurnosBarbero);
            correctos += sync("/turnos-base", token, this::saveTurnos);
            fallidos = 20 - correctos;
            replayPendingOperations(token);
            int ok = correctos;
            int failed = fallidos;
            mainHandler.post(() -> callback.onComplete(ok, failed));
        });
    }

    void hasLocalProfile(long userId, long personId, LocalProfileCallback callback) {
        executor.execute(() -> {
            boolean available = userId > 0L
                    && personId > 0L
                    && database.adminsDAO().obtenerPorId(userId) != null
                    && database.personaDAO().obtenerPorId(personId) != null;
            mainHandler.post(() -> callback.onResult(available));
        });
    }

    void shutdown() {
        executor.shutdownNow();
        apiClient.shutdown();
    }

    private int sync(String path, String token, ResourceSaver saver) {
        try {
            saver.save(apiClient.getAuthorizedArray(path, token));
            return 1;
        } catch (Exception exception) {
            Log.w(TAG, "No se pudo sincronizar " + path, exception);
            return 0;
        }
    }

    private void replayPendingOperations(String token) {
        java.util.List<OperacionPendiente> pending =
                database.operacionPendienteDAO().obtenerPendientes();
        for (OperacionPendiente operation : pending) {
            boolean completed = replayOperation(operation, token);
            if (completed) {
                database.operacionPendienteDAO().eliminarPorId(operation.id);
            } else {
                operation.intentos++;
                database.operacionPendienteDAO().actualizar(operation);
            }
        }
    }

    private boolean replayOperation(OperacionPendiente operation, String token) {
        try {
            JSONObject payload = operation.payload == null
                    ? new JSONObject() : new JSONObject(operation.payload);
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] success = {false};

            if ("categoria_servicio".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    apiClient.deleteSpecialty(token, operation.registroId,
                            actionCallback(latch, success));
                } else {
                    CategoriaServicio value = gson.fromJson(
                            operation.payload, CategoriaServicio.class);
                    boolean editing = "PUT".equals(operation.metodo);
                    apiClient.saveSpecialty(token, editing, value,
                            new AuthApiClient.SpecialtyFormCallback() {
                                @Override
                                public void onSuccess(CategoriaServicio saved) {
                                    executor.execute(() -> {
                                        if (!editing) {
                                            CategoriaServicio temporary =
                                                    database.categoriaServicioDAO()
                                                            .obtenerPorId(operation.registroId);
                                            if (temporary != null) {
                                                database.categoriaServicioDAO().eliminar(temporary);
                                            }
                                        }
                                        database.categoriaServicioDAO().insertar(saved);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else if ("servicio".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    apiClient.deleteService(token, operation.registroId,
                            actionCallback(latch, success));
                } else {
                    Servicios value = gson.fromJson(operation.payload, Servicios.class);
                    boolean editing = "PUT".equals(operation.metodo);
                    apiClient.saveService(token, editing, value,
                            new AuthApiClient.ServiceFormCallback() {
                                @Override
                                public void onSuccess(Servicios saved) {
                                    executor.execute(() -> {
                                        if (!editing) {
                                            Servicios temporary =
                                                    database.serviciosDAO()
                                                            .obtenerPorId(operation.registroId);
                                            if (temporary != null) {
                                                database.serviciosDAO().eliminar(temporary);
                                            }
                                        }
                                        database.serviciosDAO().insertar(saved);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else if ("beneficio".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    apiClient.deleteBenefit(token, operation.registroId,
                            benefitActionCallback(latch, success));
                } else {
                    Beneficios value = gson.fromJson(operation.payload, Beneficios.class);
                    boolean editing = "PUT".equals(operation.metodo);
                    apiClient.saveBenefit(token, editing, value,
                            new AuthApiClient.BenefitFormCallback() {
                                @Override
                                public void onSuccess(Beneficios saved) {
                                    executor.execute(() -> {
                                        if (!editing) {
                                            Beneficios temporary =
                                                    database.beneficiosDAO()
                                                            .obtenerPorId(operation.registroId);
                                            if (temporary != null) {
                                                database.beneficiosDAO().eliminar(temporary);
                                            }
                                        }
                                        database.beneficiosDAO().insertar(saved);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else if ("horario_dia_base".equals(operation.recurso)) {
                HorarioDiaBase value =
                        gson.fromJson(operation.payload, HorarioDiaBase.class);
                apiClient.saveBaseScheduleDay(token, value,
                        new AuthApiClient.BaseScheduleCallback() {
                            @Override
                            public void onSuccess(HorarioDiaBase saved) {
                                executor.execute(() ->
                                        database.horarioDiaBaseDAO().actualizar(saved));
                                success[0] = true;
                                latch.countDown();
                            }

                            @Override
                            public void onError(String message) {
                                latch.countDown();
                            }
                        });
            } else if ("beneficio_cliente".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    apiClient.deleteBenefitClient(token, operation.registroId,
                            benefitActionCallback(latch, success));
                } else {
                    BeneficiosCliente value =
                            gson.fromJson(operation.payload, BeneficiosCliente.class);
                    apiClient.saveBenefitClient(token, value,
                            new AuthApiClient.BenefitClientFormCallback() {
                                @Override
                                public void onSuccess(BeneficiosCliente saved) {
                                    executor.execute(() -> {
                                        BeneficiosCliente temporary =
                                                database.beneficiosClienteDAO()
                                                        .obtenerPorId(operation.registroId);
                                        if (temporary != null) {
                                            database.beneficiosClienteDAO().eliminar(temporary);
                                        }
                                        database.beneficiosClienteDAO().insertar(saved);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else if ("cliente".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    apiClient.deleteClient(token, operation.registroId,
                            actionCallback(latch, success));
                } else if ("PUT".equals(operation.metodo)
                        && payload.has("statusOnly")) {
                    Cliente client = gson.fromJson(operation.payload, Cliente.class);
                    apiClient.updateClientStatus(token, client,
                            actionCallback(latch, success));
                } else {
                    boolean editing = "PUT".equals(operation.metodo);
                    apiClient.saveClient(token, editing,
                            payload.optLong("clientId", operation.registroId),
                            payload.optLong("personId", 0L),
                            payload.optLong("documentId"), payload.optLong("specialtyId"),
                            payload.optString("names"), payload.optString("lastNames"),
                            payload.optString("document"), payload.optString("email"),
                            payload.optString("phone"), payload.optString("birthDate"),
                            payload.optString("password"),
                            payload.optBoolean("estado", true),
                            new AuthApiClient.ClientFormCallback() {
                                @Override
                                public void onSuccess(long clientId, long personId) {
                                    executor.execute(() -> {
                                        if (!editing) {
                                            Cliente temporary = database.clienteDAO()
                                                    .obtenerPorId(operation.registroId);
                                            Persona temporaryPerson = database.personaDAO()
                                                    .obtenerPorId(payload.optLong("personId", 0L));
                                            if (temporary != null) {
                                                database.clienteDAO().eliminar(temporary);
                                            }
                                            if (temporaryPerson != null) {
                                                database.personaDAO().eliminar(temporaryPerson);
                                            }
                                        }
                                        Persona person = new Persona();
                                        person.idpersona = personId;
                                        person.nombres = payload.optString("names");
                                        person.apellidos = payload.optString("lastNames");
                                        person.identificadordoc = payload.optString("document");
                                        person.correo_electronico = payload.optString("email");
                                        person.numero_celular = payload.optString("phone");
                                        person.correo_electronico = payload.optString("email");
                                        person.fecha_nacimiento = payload.optString("birthDate");
                                        person.iddocumento = payload.optLong("documentId");
                                        Cliente client = new Cliente();
                                        client.idcliente = clientId;
                                        client.idpersona = personId;
                                        client.preferencia_especialidad =
                                                payload.optLong("specialtyId");
                                        client.estado = payload.optBoolean("estado", true);
                                        database.personaDAO().insertar(person);
                                        database.clienteDAO().insertar(client);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else if ("barbero".equals(operation.recurso)) {
                if ("DELETE".equals(operation.metodo)) {
                    success[0] = false;
                    latch.countDown();
                } else if ("PUT".equals(operation.metodo)
                        && !payload.has("names")) {
                    Barberos barber = gson.fromJson(operation.payload, Barberos.class);
                    apiClient.updateBarberStatus(token, barber,
                            new AuthApiClient.BarberStatusCallback() {
                                @Override
                                public void onSuccess() {
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                } else {
                    boolean editing = "PUT".equals(operation.metodo);
                    apiClient.saveBarber(token, editing,
                            payload.optLong("barberId", operation.registroId),
                            payload.optLong("personId", 0L),
                            payload.optLong("documentId"), payload.optLong("specialtyId"),
                            payload.optString("names"), payload.optString("lastNames"),
                            payload.optString("document"), payload.optString("email"),
                            payload.optString("originalEmail",
                                    payload.optString("email")),
                            payload.optString("phone"), payload.optString("password"),
                            payload.optString("birthDate"),
                            payload.optDouble("salary", 0.0),
                            payload.optBoolean("estado", true),
                            new AuthApiClient.BarberFormCallback() {
                                @Override
                                public void onSuccess(long barberId, long personId) {
                                    executor.execute(() -> {
                                        if (!editing) {
                                            Barberos temporaryBarber =
                                                    database.barberosDAO()
                                                            .obtenerPorId(operation.registroId);
                                            Persona temporaryPerson =
                                                    database.personaDAO()
                                                            .obtenerPorId(payload.optLong("personId", 0L));
                                            if (temporaryBarber != null) {
                                                database.barberosDAO().eliminar(temporaryBarber);
                                            }
                                            if (temporaryPerson != null) {
                                                database.personaDAO().eliminar(temporaryPerson);
                                            }
                                        }
                                        Persona person = new Persona();
                                        person.idpersona = personId;
                                        person.nombres = payload.optString("names");
                                        person.apellidos = payload.optString("lastNames");
                                        person.identificadordoc = payload.optString("document");
                                        person.numero_celular = payload.optString("phone");
                                        person.iddocumento = payload.optLong("documentId");
                                        person.fecha_nacimiento = payload.optString("birthDate");
                                        Barberos barber = new Barberos();
                                        barber.idbarberos = barberId;
                                        barber.idpersona = personId;
                                        barber.correo_electronico = payload.optString("email");
                                        barber.especialidad = payload.optLong("specialtyId");
                                        barber.estado = true;
                                        database.personaDAO().insertar(person);
                                        database.barberosDAO().insertar(barber);
                                    });
                                    success[0] = true;
                                    latch.countDown();
                                }

                                @Override
                                public void onError(String message) {
                                    latch.countDown();
                                }
                            });
                }
            } else {
                latch.countDown();
            }
            latch.await(20, TimeUnit.SECONDS);
            return success[0];
        } catch (Exception exception) {
            Log.w(TAG, "No se pudo reproducir operacion pendiente", exception);
            return false;
        }
    }

    private AuthApiClient.ServiceActionCallback actionCallback(
            CountDownLatch latch, boolean[] success) {
        return new AuthApiClient.ServiceActionCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                latch.countDown();
            }
        };
    }

    private AuthApiClient.BenefitActionCallback benefitActionCallback(
            CountDownLatch latch, boolean[] success) {
        return new AuthApiClient.BenefitActionCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                latch.countDown();
            }
        };
    }

    private interface ResourceSaver {
        void save(org.json.JSONArray values) throws Exception;
    }

    private JSONObject flat(JSONObject value, String jsonName, String idName) {
        try {
            JSONObject result = new JSONObject(value.toString());
            JSONObject nested = result.optJSONObject(jsonName);
            if (nested == null && "tipoDocumento".equals(jsonName)) {
                nested = result.optJSONObject("tipo_documento");
            }
            if (nested != null) {
                Long nestedId = findNestedId(nested, idName);
                if (nestedId != null) {
                    result.put(idName, nestedId);
                }
            }
            return result;
        } catch (Exception exception) {
            return value;
        }
    }

    private Long findNestedId(JSONObject nested, String preferredIdName) {
        if (nested.has(preferredIdName) && !nested.isNull(preferredIdName)) {
            return nested.optLong(preferredIdName);
        }
        if (nested.has("id") && !nested.isNull("id")) {
            return nested.optLong("id");
        }
        java.util.Iterator<String> keys = nested.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("id") && !nested.isNull(key)) {
                Object value = nested.opt(key);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
        }
        return null;
    }

    private <T> T parse(JSONObject value, Class<T> type) {
        return gson.fromJson(value.toString(), type);
    }

    private void savePersonas(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.personaDAO().insertar(parse(flat(values.optJSONObject(i),
                    "tipoDocumento", "iddocumento"), Persona.class));
        }
    }

    private void saveAdmins(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.adminsDAO().insertar(parse(flat(values.optJSONObject(i),
                    "persona", "idpersona"), Admins.class));
        }
    }

    private void saveBarberos(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "persona", "idpersona");
            item = flat(item, "especialidad", "especialidad");
            database.barberosDAO().insertar(parse(item, Barberos.class));
        }
    }

    private void saveClientes(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "persona", "idpersona");
            item = flat(item, "especialidad", "preferencia_especialidad");
            database.clienteDAO().insertar(parse(item, Cliente.class));
        }
    }

    private void saveCategorias(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.categoriaServicioDAO().insertar(parse(values.optJSONObject(i),
                    CategoriaServicio.class));
        }
    }

    private void saveServicios(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.serviciosDAO().insertar(parse(flat(values.optJSONObject(i),
                    "categoriaServicio", "idcategoria_servicio"), Servicios.class));
        }
    }

    private void saveBeneficios(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "categoriaServicio", "categoria");
            item = flat(item, "servicios", "servicio");
            Beneficios server = parse(item, Beneficios.class);
            if (server.idbeneficio <= 0L) continue;
            for (Beneficios local : database.beneficiosDAO().obtenerTodos()) {
                if (local.idbeneficio >= 0L) continue;
                if (java.util.Objects.equals(local.estado, server.estado)
                        && java.util.Objects.equals(local.costepuntos, server.costepuntos)
                        && java.util.Objects.equals(local.categoria, server.categoria)
                        && java.util.Objects.equals(local.servicio, server.servicio)) {
                    database.beneficiosDAO().eliminar(local);
                }
            }
            database.beneficiosDAO().insertar(server);
        }
    }

    private void saveBeneficiosCliente(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "beneficios", "idbeneficio");
            item = flat(item, "cliente", "idcliente");
            database.beneficiosClienteDAO().insertar(parse(item, BeneficiosCliente.class));
        }
    }

    private void saveCitas(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "barberos", "idbarbero");
            item = flat(item, "cliente", "idcliente");
            item = flat(item, "servicio", "idservicio");
            database.citasClienteDAO().insertar(parse(item, CitasCliente.class));
        }
    }

    private void saveCronogramas(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "barberos", "idbarbero");
            item = flat(item, "turnosBase", "idhorario");
            database.cronogramaBarberoDAO().insertar(parse(item, CronogramaBarbero.class));
        }
    }

    private void saveHorariosBase(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.horarioAtencionBaseDAO().insertar(parse(values.optJSONObject(i),
                    HorarioAtencionBase.class));
        }
    }

    private void saveHorariosDiarios(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.horarioAtencionDiarioDAO().insertar(parse(values.optJSONObject(i),
                    HorarioAtencionDiario.class));
        }
    }

    private void saveHorariosDia(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.horarioDiaBaseDAO().insertar(parse(values.optJSONObject(i),
                    HorarioDiaBase.class));
        }
    }

    private void savePromociones(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.promocionesDAO().insertar(parse(flat(values.optJSONObject(i),
                    "tipoPromocion", "tipo"), Promociones.class));
        }
    }

    private void savePuntos(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.puntosClienteDAO().insertar(parse(values.optJSONObject(i),
                    PuntosCliente.class));
        }
    }

    private void saveTarifas(org.json.JSONArray values) {
        java.util.Set<Long> serverIds = new java.util.HashSet<>();
        for (int i = 0; i < values.length(); i++) {
            TarifaPuntos server = parse(values.optJSONObject(i),
                    TarifaPuntos.class);
            serverIds.add(server.idtarifa);
            database.tarifaPuntosDAO().insertar(server);
        }
        for (TarifaPuntos local : database.tarifaPuntosDAO().obtenerTodos()) {
            if (!serverIds.contains(local.idtarifa)) {
                database.tarifaPuntosDAO().eliminar(local);
            }
        }
    }

    private void saveTiposDocumento(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.tipoDocumentoDAO().insertar(parse(values.optJSONObject(i),
                    TipoDocumento.class));
        }
    }

    private void saveTiposPromocion(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.tipoPromocionDAO().insertar(parse(values.optJSONObject(i),
                    TipoPromocion.class));
        }
    }

    private void saveTurnosBarbero(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = flat(values.optJSONObject(i), "barberos", "idbarbero");
            item = flat(item, "turnosBase", "idhorarios");
            database.turnoBaseBarberoDAO().insertar(parse(item, TurnoBaseBarbero.class));
        }
    }

    private void saveTurnos(org.json.JSONArray values) {
        for (int i = 0; i < values.length(); i++) {
            database.turnosBaseDAO().insertar(parse(values.optJSONObject(i),
                    TurnosBase.class));
        }
    }
}
