package ues.edu.sv.proyect_bs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            database.beneficiosDAO().insertar(parse(item, Beneficios.class));
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
        for (int i = 0; i < values.length(); i++) {
            database.tarifaPuntosDAO().insertar(parse(values.optJSONObject(i),
                    TarifaPuntos.class));
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
