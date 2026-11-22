package ues.edu.sv.proyect_bs;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.Servicios;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import ues.edu.sv.proyect_bs.modelos.BeneficiosCliente;
import ues.edu.sv.proyect_bs.modelos.TarifaPuntos;
import ues.edu.sv.proyect_bs.modelos.HorarioDiaBase;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AuthApiClient {
    private static final String TAG = "AuthApiClient";
    static final String BASE_URL =
            "http://192.168.0.3:8085/api/v1/PBSapirestdam235";

    interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }

    interface LogoutCallback {
        void onComplete();
    }

    interface CatalogCallback {
        void onSuccess(List<CatalogItem> items);
        void onError(String message);
    }

    interface RegisterCallback {
        void onSuccess();
        void onError(String message);
    }

    interface AdminProfileCallback {
        void onSuccess(AdminProfile profile);
        void onError(String message);
    }

    interface ProfileUpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    interface DailyShiftCallback {
        void onSuccess(HorarioAtencionDiario saved);
        void onError(String message);
    }

    interface BarberStatusCallback {
        void onSuccess();
        void onError(String message);
    }

    interface BarberFormCallback {
        void onSuccess(long barberId, long personId);
        void onError(String message);
    }

    interface ServiceFormCallback {
        void onSuccess(Servicios service);
        void onError(String message);
    }

    interface ServiceActionCallback {
        void onSuccess();
        void onError(String message);
    }

    interface SpecialtyFormCallback {
        void onSuccess(CategoriaServicio specialty);
        void onError(String message);
    }

    interface ClientFormCallback {
        void onSuccess(long clientId, long personId);
        void onError(String message);
    }

    interface BenefitFormCallback {
        void onSuccess(Beneficios benefit);
        void onError(String message);
    }

    interface BaseRateCallback {
        void onSuccess(TarifaPuntos tariff);
        void onError(String message);
    }

    interface BaseScheduleCallback {
        void onSuccess(HorarioDiaBase day);
        void onError(String message);
    }

    interface BenefitActionCallback {
        void onSuccess();
        void onError(String message);
    }

    interface BenefitClientFormCallback {
        void onSuccess(BeneficiosCliente benefitClient);
        void onError(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    void login(String correo, String contrasena, LoginCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection("/auth/login", "POST");
                JSONObject body = new JSONObject()
                        .put("correo", correo)
                        .put("contrasena", contrasena);
                writeBody(connection, body.toString());
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    postError(callback, statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                            ? "Correo o contraseña incorrectos."
                            : "No se pudo conectar con el servidor.");
                    return;
                }
                JSONObject response = new JSONObject(responseBody);
                LoginResponse loginResponse = new LoginResponse(
                        response.optString("token", ""),
                        response.optString("tipo", "Bearer"),
                        response.optString("perfil", ""),
                        response.optLong("idUsuario", 0L),
                        response.optLong("idPersona", 0L));
                if (loginResponse.token.isEmpty() || loginResponse.profile.isEmpty()) {
                    postError(callback, "La respuesta del servidor no contiene una sesión válida.");
                    return;
                }
                mainHandler.post(() -> callback.onSuccess(loginResponse));
            } catch (SocketTimeoutException exception) {
                postError(callback, "La API no respondió a tiempo.");
            } catch (java.net.ConnectException exception) {
                postError(callback, "No se pudo conectar con la API.");
            } catch (Exception exception) {
                postError(callback, "Error de comunicación con la API.");
            }
        });
    }

    void loadCatalog(String path, CatalogCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection(path, "GET");
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                Log.d(TAG, "GET " + path + " -> " + statusCode + " " + responseBody);
                if (statusCode < 200 || statusCode >= 300) {
                    postCatalogError(callback, "No se pudo cargar el catálogo.");
                    return;
                }
                org.json.JSONArray values = new org.json.JSONArray(responseBody);
                List<CatalogItem> items = new ArrayList<>();
                for (int index = 0; index < values.length(); index++) {
                    JSONObject value = values.getJSONObject(index);
                    long id = value.has("iddocumento")
                            ? value.optLong("iddocumento")
                            : value.optLong("idcategoria_servicio");
                    String name = value.has("nombredocumento")
                            ? value.optString("nombredocumento")
                            : value.optString("nombre");
                    if (id > 0 && !name.isEmpty()) {
                        items.add(new CatalogItem(id, name));
                    }
                }
                mainHandler.post(() -> callback.onSuccess(items));
            } catch (Exception exception) {
                Log.e(TAG, "Error cargando catálogo " + path, exception);
                postCatalogError(callback, "No se pudieron cargar los catálogos.");
            }
        });
    }

    void register(String nombres, String apellidos, long idDocumento,
                  String identificadorDocumento, String correoElectronico,
                  String numeroCelular, String fechaNacimiento,
                  long idCategoriaServicio, String contrasena,
                  RegisterCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection("/auth/register", "POST");
                JSONObject body = new JSONObject()
                        .put("nombres", nombres)
                        .put("apellidos", apellidos)
                        .put("idDocumento", idDocumento)
                        .put("identificadorDocumento", identificadorDocumento)
                        .put("correoElectronico", correoElectronico)
                        .put("numeroCelular", numeroCelular)
                        .put("fechaNacimiento", fechaNacimiento)
                        .put("idCategoriaServicio", idCategoriaServicio)
                        .put("contrasena", contrasena);
                writeBody(connection, body.toString());
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    postRegisterError(callback, extractApiMessage(responseBody));
                    return;
                }
                mainHandler.post(callback::onSuccess);
            } catch (SocketTimeoutException exception) {
                postRegisterError(callback, "La API no respondió a tiempo.");
            } catch (java.net.ConnectException exception) {
                postRegisterError(callback, "No se pudo conectar con la API.");
            } catch (Exception exception) {
                postRegisterError(callback, "Error de comunicación con la API.");
            }
        });
    }

    void logout(String token, LogoutCallback callback) {
        executor.execute(() -> {
            if (token != null && !token.isEmpty()) {
                try {
                    HttpURLConnection connection = openConnection("/auth/logout", "POST");
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                    connection.getResponseCode();
                    connection.disconnect();
                } catch (IOException ignored) {
                    // La sesión local se limpia aunque el token ya haya expirado.
                }

            }
            mainHandler.post(callback::onComplete);
        });
    }

    void loadAdminProfile(String token, long userId, long personId,
                          AdminProfileCallback callback) {
        executor.execute(() -> {
            try {
                if (userId <= 0L || personId <= 0L) {
                    throw new IOException("La sesión no contiene los identificadores del perfil.");
                }
                JSONObject admin = getAuthorizedJson(
                        "/admins/" + userId, token);
                JSONObject person = getAuthorizedJson(
                        "/personas/" + personId, token);
                JSONObject nestedPerson = admin.optJSONObject("persona");
                if (nestedPerson != null) {
                    person = mergeProfileJson(nestedPerson, person);
                }
                AdminProfile profile = new AdminProfile(
                        firstValue(person, "nombres"),
                        firstValue(person, "apellidos"),
                        firstValue(admin, "correoelectronico", "correoElectronico",
                                "correo_electronico", "correo"),
                        firstValue(person, "identificadordoc", "identificadorDoc",
                                "identificadorDocumento"),
                        firstValue(person, "numero_celular", "numeroCelular"),
                        firstValue(person, "fecha_nacimiento", "fechaNacimiento"),
                        firstLong(person, "iddocumento", "idDocumento"));
                mainHandler.post(() -> callback.onSuccess(profile));
            } catch (SocketTimeoutException exception) {
                postAdminProfileError(callback, "La API no respondió a tiempo.");
            } catch (Exception exception) {
                Log.e(TAG, "Error cargando perfil administrativo", exception);
                postAdminProfileError(callback, "No se pudo cargar el perfil.");
            }
        });
    }

    void updateAdminProfile(String token, long userId, long personId, long documentId,
                            String names, String lastNames, String document,
                            String adminEmail, String personEmail, String phone, String birthDate,
                            ProfileUpdateCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject person = new JSONObject()
                        .put("idpersona", personId)
                        .put("nombres", names)
                        .put("apellidos", lastNames)
                        .put("identificadordoc", document)
                        .put("correo_electronico", personEmail)
                        .put("numero_celular", phone)
                        .put("fecha_nacimiento", birthDate);
                if (documentId > 0L) {
                    person.put("tipoDocumento",
                            new JSONObject().put("iddocumento", documentId));
                }
                putAuthorizedJson("/personas/" + personId, token, person);
                mainHandler.post(callback::onSuccess);
            } catch (SocketTimeoutException exception) {
                postProfileUpdateError(callback, "La API no respondió a tiempo.");
            } catch (Exception exception) {
                Log.e(TAG, "Error actualizando perfil administrativo", exception);
                postProfileUpdateError(callback, "No se pudo actualizar el perfil.");
            }
        });
    }

    void updateBarberStatus(String token, Barberos barber, BarberStatusCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("idbarberos", barber.idbarberos)
                        .put("foto_perfil", barber.foto_perfil)
                        .put("salario", barber.salario)
                        .put("estado", Boolean.TRUE.equals(barber.estado));
                if (barber.idpersona != null) {
                    body.put("persona", new JSONObject().put("idpersona", barber.idpersona));
                }
                if (barber.especialidad != null) {
                    body.put("especialidad",
                            new JSONObject().put("idcategoria_servicio", barber.especialidad));
                }
                putAuthorizedJson("/barberos/" + barber.idbarberos, token, body);
                mainHandler.post(callback::onSuccess);
            } catch (SocketTimeoutException exception) {
                mainHandler.post(() -> callback.onError("La API no respondió a tiempo."));
            } catch (Exception exception) {
                Log.e(TAG, "Error actualizando estado del barbero", exception);
                mainHandler.post(() -> callback.onError("No se pudo actualizar el estado del barbero."));
            }
        });
    }

    void saveBarber(String token, boolean editing, long barberId, long personId,
                    long documentId, long specialtyId, String names, String lastNames,
                    String document, String email, String originalEmail,
                    String phone, String password,
                    String birthDate, double salary, boolean active,
                    BarberFormCallback callback) {
        executor.execute(() -> {
            try {
                if (editing && (barberId <= 0L || personId <= 0L)) {
                    throw new IOException("No se pueden editar barberos sin sus IDs existentes.");
                }
                boolean emailChanged = !editing
                        || email == null
                        || originalEmail == null
                        || !email.trim().equalsIgnoreCase(originalEmail.trim());
                JSONObject person = new JSONObject()
                        .put("nombres", names)
                        .put("apellidos", lastNames)
                        .put("identificadordoc", document)
                        .put("numero_celular", phone)
                        .put("fecha_nacimiento", birthDate)
                        .put("tipoDocumento",
                                new JSONObject().put("iddocumento", documentId));
                if (emailChanged) {
                    person.put("correo_electronico", email);
                }
                JSONObject personResponse;
                if (editing) {
                    person.put("idpersona", personId);
                    Log.d(TAG, "PUT persona existente: " + personId);
                    personResponse = putAuthorizedJson("/personas/" + personId, token, person);
                } else {
                    Log.d(TAG, "POST persona nueva");
                    personResponse = postAuthorizedJson("/personas", token, person);
                }
                long savedPersonId = firstLong(personResponse, "idpersona", "idPersona");
                if (savedPersonId <= 0L) {
                    savedPersonId = personId;
                }
                JSONObject barber = new JSONObject()
                        .put("estado", active)
                        .put("salario", salary)
                        .put("persona", new JSONObject().put("idpersona", savedPersonId))
                        .put("especialidad",
                                new JSONObject().put("idcategoria_servicio", specialtyId));
                if (emailChanged) {
                    barber.put("correo_electronico", email);
                }
                JSONObject barberResponse;
                if (editing) {
                    barber.put("idbarberos", barberId);
                    Log.d(TAG, "PUT barbero existente: " + barberId);
                    barberResponse = putAuthorizedJson("/barberos/" + barberId, token, barber);
                } else {
                    barberResponse = postAuthorizedJson("/barberos", token, barber);
                }
                long savedBarberId = firstLong(barberResponse, "idbarberos", "idBarberos");
                if (savedBarberId <= 0L) {
                    savedBarberId = barberId;
                }
                final long finalPersonId = savedPersonId;
                final long finalBarberId = savedBarberId;
                mainHandler.post(() -> callback.onSuccess(finalBarberId, finalPersonId));
            } catch (SocketTimeoutException exception) {
                mainHandler.post(() -> callback.onError("La API no respondió a tiempo."));
            } catch (java.net.ConnectException | java.net.UnknownHostException exception) {
                Log.e(TAG, "Sin conexión alcanzando la API al guardar barbero", exception);
                mainHandler.post(() -> callback.onError(
                        "Sin conexión con la API: " + exception.getClass().getSimpleName()));
            } catch (IOException exception) {
                Log.e(TAG, "Error HTTP al guardar barbero", exception);
                String detail = exception.getMessage();
                mainHandler.post(() -> callback.onError(
                        "[API] " + (detail == null || detail.trim().isEmpty()
                                ? "La API rechazó la operación."
                                : detail)));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando barbero", exception);
                mainHandler.post(() -> callback.onError(
                        "[CLIENTE] " + (exception.getMessage() == null
                                ? "No se pudo preparar el guardado del barbero."
                                : exception.getMessage())));
            }
        });
    }

    void saveClient(String token, boolean editing, long clientId, long personId,
                    long documentId, long specialtyId, String names, String lastNames,
                    String document, String email, String phone, String birthDate, String password,
                    Boolean estado, ClientFormCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject person = new JSONObject()
                        .put("nombres", names)
                        .put("apellidos", lastNames)
                        .put("identificadordoc", document)
                        .put("correo_electronico", email)
                        .put("numero_celular", phone)
                        .put("fecha_nacimiento", birthDate)
                        .put("tipoDocumento",
                                new JSONObject().put("iddocumento", documentId));
                JSONObject personResponse;
                if (editing) {
                    person.put("idpersona", personId);
                    personResponse = putAuthorizedJson("/personas/" + personId, token, person);
                } else {
                    personResponse = postAuthorizedJson("/personas", token, person);
                }
                long savedPersonId = firstLong(personResponse, "idpersona", "idPersona");
                if (savedPersonId <= 0L) {
                    savedPersonId = personId;
                }

                JSONObject client = new JSONObject()
                        .put("estado", Boolean.TRUE.equals(estado))
                        .put("contraseña", password == null ? "" : password)
                        .put("persona", new JSONObject().put("idpersona", savedPersonId));
                if (specialtyId > 0L) {
                    client.put("especialidad",
                            new JSONObject().put("idcategoria_servicio", specialtyId));
                }
                JSONObject clientResponse;
                if (editing) {
                    client.put("idcliente", clientId);
                    clientResponse = putAuthorizedJson("/clientes/" + clientId, token, client);
                } else {
                    clientResponse = postAuthorizedJson("/clientes", token, client);
                }
                long savedClientId = firstLong(clientResponse, "idcliente", "idCliente");
                if (savedClientId <= 0L) {
                    savedClientId = clientId;
                }
                long finalPersonId = savedPersonId;
                long finalClientId = savedClientId;
                mainHandler.post(() -> callback.onSuccess(finalClientId, finalPersonId));
            } catch (SocketTimeoutException exception) {
                mainHandler.post(() -> callback.onError("La API no respondió a tiempo."));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando cliente", exception);
                mainHandler.post(() -> callback.onError(
                        exception.getMessage() == null
                                ? "No se pudo guardar el cliente."
                                : exception.getMessage()));
            }
        });
    }

    void updateClientStatus(String token, Cliente client, ServiceActionCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("idcliente", client.idcliente)
                        .put("estado", Boolean.TRUE.equals(client.estado))
                        .put("contraseña", "")
                        .put("persona", new JSONObject().put("idpersona", client.idpersona));
                if (client.preferencia_especialidad != null
                        && client.preferencia_especialidad > 0L) {
                    body.put("especialidad", new JSONObject()
                            .put("idcategoria_servicio", client.preferencia_especialidad));
                }
                putAuthorizedJson("/clientes/" + client.idcliente, token, body);
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error actualizando estado del cliente", exception);
                mainHandler.post(() -> callback.onError(
                        exception.getMessage() == null
                                ? "No se pudo actualizar el estado del cliente."
                                : exception.getMessage()));
            }
        });
    }

    void deleteClient(String token, long clientId, ServiceActionCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection("/clientes/" + clientId, "DELETE");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP " + statusCode + ": " + responseBody);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error eliminando cliente", exception);
                mainHandler.post(() -> callback.onError(
                        exception.getMessage() == null
                                ? "No se pudo eliminar el cliente."
                                : exception.getMessage()));
            }
        });
    }

    void saveService(String token, boolean editing, Servicios service,
            ServiceFormCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("nombre_servicio", service.nombre_servicio)
                        .put("precio", service.precio)
                        .put("duracion", service.duracion)
                        .put("estado", Boolean.TRUE.equals(service.estado))
                        .put("categoriaServicio", new JSONObject()
                                .put("idcategoria_servicio", service.idcategoria_servicio));
                JSONObject response;
                if (editing) {
                    body.put("idservicio", service.idservicio);
                    response = putAuthorizedJson("/servicios/" + service.idservicio, token, body);
                } else {
                    response = postAuthorizedJson("/servicios", token, body);
                }
                Servicios saved = new Servicios();
                saved.idservicio = firstLong(response, "idservicio", "idServicio");
                if (saved.idservicio <= 0L) {
                    saved.idservicio = service.idservicio;
                }
                saved.nombre_servicio = response.optString("nombre_servicio",
                        service.nombre_servicio);
                saved.precio = response.has("precio")
                        ? response.optDouble("precio") : service.precio;
                saved.duracion = response.has("duracion")
                        ? response.optInt("duracion") : service.duracion;
                saved.estado = response.has("estado")
                        ? response.optBoolean("estado") : service.estado;
                saved.idcategoria_servicio = firstLong(response,
                        "idcategoria_servicio", "idCategoriaServicio");
                if (saved.idcategoria_servicio <= 0L) {
                    JSONObject category = response.optJSONObject("categoriaServicio");
                    saved.idcategoria_servicio = category == null
                            ? service.idcategoria_servicio
                            : firstLong(category, "idcategoria_servicio",
                                    "idCategoriaServicio");
                }
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (SocketTimeoutException exception) {
                mainHandler.post(() -> callback.onError("La API no respondió a tiempo."));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando servicio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo guardar el servicio." : exception.getMessage()));
            }
        });
    }

    void saveDailyShift(String token, HorarioAtencionDiario shift,
                        DailyShiftCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("idhorarioatenciondiario", shift.idhorarioatenciondiario)
                        .put("fecha", shift.fecha)
                        .put("estado", Boolean.TRUE.equals(shift.estado))
                        .put("apertura", shift.apertura)
                        .put("cierre", shift.cierre)
                        .put("receso", shift.receso)
                        .put("dia", shift.dia);
                if (shift.inicio != null) {
                    body.put("inicio", shift.inicio);
                }
                if (shift.fin != null) {
                    body.put("fin", shift.fin);
                }
                putAuthorizedJson("/horarios-atencion-diarios/"
                        + shift.idhorarioatenciondiario, token, body);
                mainHandler.post(() -> callback.onSuccess(shift));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando jornada diaria", exception);
                mainHandler.post(() -> callback.onError(
                        exception.getMessage() == null
                                ? "No se pudo guardar la jornada."
                                : exception.getMessage()));
            }
        });
    }

    void updateServiceStatus(String token, Servicios service, ServiceActionCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("idservicio", service.idservicio)
                        .put("nombre_servicio", service.nombre_servicio)
                        .put("precio", service.precio)
                        .put("duracion", service.duracion)
                        .put("estado", Boolean.TRUE.equals(service.estado))
                        .put("categoriaServicio", new JSONObject()
                                .put("idcategoria_servicio", service.idcategoria_servicio));
                putAuthorizedJson("/servicios/" + service.idservicio, token, body);
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error actualizando estado del servicio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo actualizar el estado del servicio." : exception.getMessage()));
            }
        });
    }

    void deleteService(String token, long serviceId, ServiceActionCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection("/servicios/" + serviceId, "DELETE");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP " + statusCode + ": " + responseBody);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error eliminando servicio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo eliminar el servicio." : exception.getMessage()));
            }
        });
    }

    void saveBenefit(String token, boolean editing, Beneficios benefit,
                     BenefitFormCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("estado", Boolean.TRUE.equals(benefit.estado))
                        .put("costepuntos", benefit.costepuntos == null ? 0 : benefit.costepuntos);
                if (editing) {
                    body.put("idbeneficio", benefit.idbeneficio);
                }
                if (benefit.categoria != null) {
                    body.put("categoriaServicio",
                            new JSONObject().put("idcategoria_servicio", benefit.categoria));
                }
                if (benefit.servicio != null) {
                    body.put("servicios",
                            new JSONObject().put("idservicio", benefit.servicio));
                }
                JSONObject response = editing
                        ? putAuthorizedJson("/beneficios/" + benefit.idbeneficio, token, body)
                        : postAuthorizedJson("/beneficios", token, body);
                Beneficios saved = new Beneficios();
                saved.idbeneficio = firstLong(response, "idbeneficio", "idBeneficio");
                if (saved.idbeneficio <= 0L) {
                    JSONObject nested = response.optJSONObject("beneficio");
                    if (nested != null) {
                        saved.idbeneficio = firstLong(nested, "idbeneficio", "idBeneficio");
                    }
                }
                if (saved.idbeneficio <= 0L && !editing) {
                    try {
                        org.json.JSONArray current =
                                getAuthorizedArray("/beneficios", token);
                        for (int index = 0; index < current.length(); index++) {
                            JSONObject item = current.optJSONObject(index);
                            if (item == null) continue;
                            Beneficios candidate = parseBenefitJson(item);
                            if (candidate.costepuntos != null
                                    && candidate.costepuntos.equals(benefit.costepuntos)
                                    && java.util.Objects.equals(candidate.estado, benefit.estado)
                                    && java.util.Objects.equals(candidate.categoria, benefit.categoria)
                                    && java.util.Objects.equals(candidate.servicio, benefit.servicio)) {
                                saved.idbeneficio = candidate.idbeneficio;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, "No se pudo reconciliar el beneficio creado.");
                    }
                }
                if (editing) {
                    saved.idbeneficio = benefit.idbeneficio;
                } else if (saved.idbeneficio <= 0L) {
                    saved.idbeneficio = benefit.idbeneficio;
                }
                saved.estado = response.has("estado")
                        ? response.optBoolean("estado") : benefit.estado;
                saved.costepuntos = response.has("costepuntos")
                        ? response.optInt("costepuntos") : benefit.costepuntos;
                JSONObject category = response.optJSONObject("categoriaServicio");
                if (category == null) {
                    category = response.optJSONObject("categoria");
                }
                if (category != null) {
                    long categoryId = firstLong(category,
                            "idcategoria_servicio", "idCategoriaServicio");
                    saved.categoria = categoryId > 0L ? categoryId : benefit.categoria;
                } else if (response.has("categoria") && !response.isNull("categoria")) {
                    saved.categoria = response.optLong("categoria", 0L) > 0L
                            ? response.optLong("categoria") : benefit.categoria;
                } else {
                    saved.categoria = benefit.categoria;
                }
                JSONObject service = response.optJSONObject("servicios");
                if (service == null) {
                    service = response.optJSONObject("servicio");
                }
                if (service != null) {
                    long serviceId = firstLong(service, "idservicio", "idServicio");
                    saved.servicio = serviceId > 0L ? serviceId : null;
                } else if (response.has("servicios")
                        && !response.isNull("servicios")) {
                    saved.servicio = response.optLong("servicios", 0L) > 0L
                            ? response.optLong("servicios") : null;
                } else if (response.has("servicio")
                        && !response.isNull("servicio")) {
                    saved.servicio = response.optLong("servicio", 0L) > 0L
                            ? response.optLong("servicio") : null;
                } else {
                    saved.servicio = benefit.servicio;
                }
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando beneficio", exception);
                String message = cleanApiErrorMessage(exception.getMessage(),
                        "No se pudo guardar el beneficio.");
                mainHandler.post(() -> callback.onError(message));
            }
        });
    }

    void updateBenefitStatus(String token, Beneficios benefit,
                             BenefitActionCallback callback) {
        saveBenefit(token, true, benefit, new BenefitFormCallback() {
            @Override public void onSuccess(Beneficios saved) {
                mainHandler.post(callback::onSuccess);
            }
            @Override public void onError(String message) {
                mainHandler.post(() -> callback.onError(message));
            }
        });
    }

    void deleteBenefit(String token, long benefitId, BenefitActionCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection =
                        openConnection("/beneficios/" + benefitId, "DELETE");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP " + statusCode + ": " + responseBody);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error eliminando beneficio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo eliminar el beneficio." : exception.getMessage()));
            }
        });
    }

    void saveBaseRate(String token, TarifaPuntos tariff, BaseRateCallback callback) {
        executor.execute(() -> {
            try {
                if (tariff.idtarifa <= 0) {
                    throw new IOException("No hay una tarifa base para actualizar.");
                }
                JSONObject body = new JSONObject()
                        .put("idtarifa", tariff.idtarifa)
                        .put("puntos", tariff.puntos == null ? 1 : tariff.puntos)
                        .put("tarifamonetaria",
                                tariff.tarifamonetaria == null ? 1d : tariff.tarifamonetaria);
                JSONObject response = putAuthorizedJson(
                        "/tarifas-puntos/" + tariff.idtarifa, token, body);

                TarifaPuntos saved = new TarifaPuntos();
                saved.idtarifa = tariff.idtarifa;
                saved.puntos = response.has("puntos")
                        ? response.optInt("puntos", tariff.puntos)
                        : tariff.puntos;
                saved.tarifamonetaria = response.has("tarifamonetaria")
                        ? response.optDouble("tarifamonetaria", tariff.tarifamonetaria)
                        : tariff.tarifamonetaria;
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando tarifa base", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo guardar la tarifa base." : exception.getMessage()));
            }
        });
    }

    void saveBaseScheduleDay(String token, HorarioDiaBase day,
                             BaseScheduleCallback callback) {
        executor.execute(() -> {
            try {
                if (day.idhorariodia <= 0) {
                    throw new IOException("No hay un día base disponible para actualizar.");
                }
                JSONObject body = new JSONObject()
                        .put("idhorariodia", day.idhorariodia)
                        .put("estado", Boolean.TRUE.equals(day.estado))
                        .put("apertura", day.apertura)
                        .put("cierre", day.cierre)
                        .put("receso", day.receso)
                        .put("inicio", day.inicio)
                        .put("fin", day.fin)
                        .put("dia", day.dia);
                JSONObject response = putAuthorizedJson(
                        "/horarios-dia-base/" + day.idhorariodia, token, body);

                HorarioDiaBase saved = new HorarioDiaBase();
                saved.idhorariodia = day.idhorariodia;
                saved.estado = response.has("estado")
                        ? response.optBoolean("estado", Boolean.TRUE.equals(day.estado))
                        : day.estado;
                saved.apertura = response.has("apertura")
                        ? response.optString("apertura", day.apertura) : day.apertura;
                saved.cierre = response.has("cierre")
                        ? response.optString("cierre", day.cierre) : day.cierre;
                saved.receso = response.has("receso")
                        ? response.optBoolean("receso", day.receso) : day.receso;
                saved.inicio = response.has("inicio")
                        ? response.optString("inicio", day.inicio) : day.inicio;
                saved.fin = response.has("fin")
                        ? response.optString("fin", day.fin) : day.fin;
                saved.dia = response.has("dia")
                        ? response.optString("dia", day.dia) : day.dia;
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando horario día base", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo guardar el horario base." : exception.getMessage()));
            }
        });
    }

    void saveBenefitClient(String token, BeneficiosCliente value,
                           BenefitClientFormCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("fechacanje", value.fechacanje)
                        .put("beneficios",
                                new JSONObject().put("idbeneficio", value.idbeneficio))
                        .put("cliente",
                                new JSONObject().put("idcliente", value.idcliente));
                JSONObject response = postAuthorizedJson("/beneficios-cliente", token, body);
                BeneficiosCliente saved = new BeneficiosCliente();
                saved.idbeneficioscliente = firstLong(response,
                        "idbeneficioscliente", "idBeneficiosCliente");
                if (saved.idbeneficioscliente <= 0L) {
                    saved.idbeneficioscliente = value.idbeneficioscliente;
                }
                saved.fechacanje = response.optString("fechacanje", value.fechacanje);
                JSONObject benefit = response.optJSONObject("beneficios");
                saved.idbeneficio = benefit == null
                        ? value.idbeneficio
                        : firstLong(benefit, "idbeneficio", "idBeneficio");
                JSONObject client = response.optJSONObject("cliente");
                saved.idcliente = client == null
                        ? value.idcliente
                        : firstLong(client, "idcliente", "idCliente");
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (Exception exception) {
                Log.e(TAG, "Error canjeando beneficio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo canjear el beneficio." : exception.getMessage()));
            }
        });
    }

    void deleteBenefitClient(String token, long benefitClientId,
                             BenefitActionCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection =
                        openConnection("/beneficios-cliente/" + benefitClientId, "DELETE");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP " + statusCode + ": " + responseBody);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error eliminando canje de beneficio", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo eliminar el canje." : exception.getMessage()));
            }
        });
    }

    void saveSpecialty(String token, boolean editing, CategoriaServicio specialty,
            SpecialtyFormCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject().put("nombre", specialty.nombre);
                JSONObject response;
                if (editing) {
                    body.put("idcategoria_servicio", specialty.idcategoria_servicio);
                    response = putAuthorizedJson("/categorias-servicio/"
                            + specialty.idcategoria_servicio, token, body);
                } else {
                    response = postAuthorizedJson("/categorias-servicio", token, body);
                }
                CategoriaServicio saved = new CategoriaServicio();
                saved.idcategoria_servicio = firstLong(response,
                        "idcategoria_servicio", "idCategoriaServicio");
                if (saved.idcategoria_servicio <= 0L) {
                    saved.idcategoria_servicio = specialty.idcategoria_servicio;
                }
                saved.nombre = response.optString("nombre", specialty.nombre);
                mainHandler.post(() -> callback.onSuccess(saved));
            } catch (Exception exception) {
                Log.e(TAG, "Error guardando especialidad", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo guardar la especialidad." : exception.getMessage()));
            }
        });
    }

    void deleteSpecialty(String token, long specialtyId, ServiceActionCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection connection = openConnection(
                        "/categorias-servicio/" + specialtyId, "DELETE");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                int statusCode = connection.getResponseCode();
                String responseBody = readBody(connection, statusCode);
                if (statusCode < 200 || statusCode >= 300) {
                    throw new IOException("HTTP " + statusCode + ": " + responseBody);
                }
                mainHandler.post(callback::onSuccess);
            } catch (Exception exception) {
                Log.e(TAG, "Error eliminando especialidad", exception);
                mainHandler.post(() -> callback.onError(exception.getMessage() == null
                        ? "No se pudo eliminar la especialidad." : exception.getMessage()));
            }
        });
    }

    private JSONObject getAuthorizedJson(String path, String token) throws IOException {
        HttpURLConnection connection = openConnection(path, "GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        int statusCode = connection.getResponseCode();
        String responseBody = readBody(connection, statusCode);
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP " + statusCode);
        }

        try {
            return new JSONObject(responseBody);
        } catch (org.json.JSONException exception) {
            throw new IOException("La API devolvió un JSON inválido.", exception);
        }
    }

    private JSONObject putAuthorizedJson(String path, String token, JSONObject body)
            throws IOException {
        HttpURLConnection connection = openConnection(path, "PUT");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        writeBody(connection, body.toString());
        int statusCode = connection.getResponseCode();
        String responseBody = readBody(connection, statusCode);
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP " + statusCode + " PUT " + path + ": " + responseBody);
        }
        try {
            return responseBody.isEmpty() ? new JSONObject() : new JSONObject(responseBody);
        } catch (org.json.JSONException exception) {
            throw new IOException("La API devolvió un JSON inválido.", exception);
        }
    }

    private JSONObject postAuthorizedJson(String path, String token, JSONObject body)
            throws IOException {
        HttpURLConnection connection = openConnection(path, "POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        writeBody(connection, body.toString());
        int statusCode = connection.getResponseCode();
        String responseBody = readBody(connection, statusCode);
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP " + statusCode + ": " + responseBody);
        }
        try {
            return responseBody.isEmpty() ? new JSONObject() : new JSONObject(responseBody);
        } catch (org.json.JSONException exception) {
            throw new IOException("La API devolvió un JSON inválido.", exception);
        }
    }

    org.json.JSONArray getAuthorizedArray(String path, String token) throws IOException {
        HttpURLConnection connection = openConnection(path, "GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        int statusCode = connection.getResponseCode();
        String responseBody = readBody(connection, statusCode);
        if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return new org.json.JSONArray();
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP " + statusCode);
        }
        try {
            return new org.json.JSONArray(responseBody);
        } catch (org.json.JSONException exception) {
            throw new IOException("La API devolvió un arreglo JSON inválido.", exception);
        }
    }


    private Beneficios parseBenefitJson(JSONObject item) {
        JSONObject category = item.optJSONObject("categoriaServicio");
        if (category == null) category = item.optJSONObject("categoria");
        JSONObject service = item.optJSONObject("servicios");
        if (service == null) service = item.optJSONObject("servicio");

        Beneficios benefit = new Beneficios();
        benefit.idbeneficio = firstLong(item, "idbeneficio", "idBeneficio");
        benefit.estado = item.has("estado") ? item.optBoolean("estado") : null;
        benefit.costepuntos = item.has("costepuntos")
                ? item.optInt("costepuntos") : null;
        benefit.categoria = category == null
                ? item.has("categoria") && !item.isNull("categoria")
                        && !(item.opt("categoria") instanceof JSONObject)
                        ? item.optLong("categoria") : null
                : firstLong(category, "idcategoria_servicio", "idCategoriaServicio");
        benefit.servicio = service == null
                ? item.has("servicio") && !item.isNull("servicio")
                        && !(item.opt("servicio") instanceof JSONObject)
                        ? item.optLong("servicio") : null
                : firstLong(service, "idservicio", "idServicio");
        return benefit;
    }

    private JSONObject mergeProfileJson(JSONObject primary, JSONObject fallback) {
        JSONObject merged = new JSONObject();
        try {
            java.util.Iterator<String> keys = fallback.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                merged.put(key, fallback.opt(key));
            }
            keys = primary.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                merged.put(key, primary.opt(key));
            }
        } catch (org.json.JSONException exception) {
            Log.w(TAG, "No se pudo combinar la persona anidada del administrador.", exception);
            return primary;
        }
        return merged;
    }

    private String firstValue(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.optString(key, "").trim();
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return "";
    }

    private long firstLong(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.isNull(key)) {
                Object value = object.opt(key);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                try {
                    return Long.parseLong(String.valueOf(value));
                } catch (NumberFormatException ignored) {
                    // Probar la siguiente representación.
                }
            }
        }
        JSONObject nested = object.optJSONObject("tipoDocumento");
        return nested == null ? 0L : firstLong(nested, "iddocumento", "id");
    }

    void shutdown() {
        executor.shutdownNow();
    }

    private HttpURLConnection openConnection(String path, String method) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput("POST".equals(method) || "PUT".equals(method));
        return connection;
    }

    private void writeBody(HttpURLConnection connection, String body) throws IOException {
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readBody(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream stream = statusCode >= 400
                ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } finally {
            connection.disconnect();
        }
    }

    private String cleanApiErrorMessage(String rawMessage, String fallback) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) return fallback;
        int start = rawMessage.indexOf("{");
        int end = rawMessage.lastIndexOf("}");
        if (start >= 0 && end > start) {
            try {
                JSONObject error = new JSONObject(rawMessage.substring(start, end + 1));
                String message = error.optString("message", "").trim();
                if (!message.isEmpty()) return message;
            } catch (Exception ignored) {
            }
        }
        return rawMessage;
    }

    private String extractApiMessage(String responseBody) {
        try {
            String message = new JSONObject(responseBody).optString("message", "");
            return message.isEmpty() ? "No se pudo completar el registro." : message;
        } catch (Exception exception) {
            return "No se pudo completar el registro.";
        }
    }

    private void postError(LoginCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void postCatalogError(CatalogCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void postRegisterError(RegisterCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void postAdminProfileError(AdminProfileCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void postProfileUpdateError(ProfileUpdateCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    static final class CatalogItem {
        final long id;
        final String name;

        CatalogItem(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static final class LoginResponse {
        final String token;
        final String tokenType;
        final String profile;
        final long userId;
        final long personId;

        LoginResponse(String token, String tokenType, String profile,
                      long userId, long personId) {
            this.token = token;
            this.tokenType = tokenType;
            this.profile = profile;
            this.userId = userId;
            this.personId = personId;
        }

    }

    static final class AdminProfile {
        final String names;
        final String lastNames;
        final String email;
        final String document;
        final String phone;
        final String birthDate;
        final long documentId;

        AdminProfile(String names, String lastNames, String email,
                     String document, String phone, String birthDate) {
            this(names, lastNames, email, document, phone, birthDate, 0L);
        }

        AdminProfile(String names, String lastNames, String email,
                     String document, String phone, String birthDate, long documentId) {
            this.names = names;
            this.lastNames = lastNames;
            this.email = email;
            this.document = document;
            this.phone = phone;
            this.birthDate = birthDate;
            this.documentId = documentId;
        }
    }
}
