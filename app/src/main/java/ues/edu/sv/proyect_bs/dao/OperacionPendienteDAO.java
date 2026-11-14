package ues.edu.sv.proyect_bs.dao;

import androidx.room.*;
import java.util.List;
import ues.edu.sv.proyect_bs.modelos.OperacionPendiente;

@Dao
public interface OperacionPendienteDAO {
    @Insert
    long insertar(OperacionPendiente value);

    @Update
    void actualizar(OperacionPendiente value);

    @Delete
    void eliminar(OperacionPendiente value);

    @Query("SELECT * FROM operaciones_pendientes WHERE estado = 'PENDIENTE' ORDER BY creadoEn")
    List<OperacionPendiente> obtenerPendientes();

    @Query("DELETE FROM operaciones_pendientes WHERE id = :id")
    void eliminarPorId(long id);
}
