package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.TarifaPuntos;
import java.util.List;
@Dao public interface TarifaPuntosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(TarifaPuntos value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<TarifaPuntos> values);
    @Update void actualizar(TarifaPuntos value);
    @Delete void eliminar(TarifaPuntos value);
    @Query("SELECT * FROM tarifapuntos") List<TarifaPuntos> obtenerTodos();
    @Query("SELECT * FROM tarifapuntos WHERE idtarifa = :id LIMIT 1") TarifaPuntos obtenerPorId(long id);
}
