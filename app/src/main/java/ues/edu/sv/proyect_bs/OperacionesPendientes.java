package ues.edu.sv.proyect_bs;

import android.content.Context;

import com.google.gson.Gson;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.OperacionPendiente;

final class OperacionesPendientes {
    private OperacionesPendientes() {
    }

    static void guardar(Context context, String recurso, String metodo,
                        Long registroId, Object payload) {
        OperacionPendiente operation = new OperacionPendiente(
                recurso,
                metodo,
                registroId,
                payload == null ? "{}" : new Gson().toJson(payload),
                "PENDIENTE",
                System.currentTimeMillis());
        AppDB.getInstance(context).operacionPendienteDAO().insertar(operation);
    }

    static void guardarJson(Context context, String recurso, String metodo,
                            Long registroId, String payload) {
        OperacionPendiente operation = new OperacionPendiente(
                recurso,
                metodo,
                registroId,
                payload,
                "PENDIENTE",
                System.currentTimeMillis());
        AppDB.getInstance(context).operacionPendienteDAO().insertar(operation);
    }
}
