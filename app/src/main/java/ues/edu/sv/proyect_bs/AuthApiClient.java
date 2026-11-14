package ues.edu.sv.proyect_bs;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

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
            "http://192.168.0.5:8085/api/v1/PBSapirestdam235";

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

                JSONObject admin = new JSONObject()
                        .put("idadmin", userId)
                        .put("estado", true)
                        .put("correoelectronico", adminEmail)
                        .put("persona", new JSONObject().put("idpersona", personId));
                putAuthorizedJson("/admins/" + userId, token, admin);
                mainHandler.post(callback::onSuccess);
            } catch (SocketTimeoutException exception) {
                postProfileUpdateError(callback, "La API no respondió a tiempo.");
            } catch (Exception exception) {
                Log.e(TAG, "Error actualizando perfil administrativo", exception);
                postProfileUpdateError(callback, "No se pudo actualizar el perfil.");
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
        connection.setDoOutput("POST".equals(method));
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
