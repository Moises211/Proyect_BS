package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.TurnosBase;
import java.util.List;
@Dao public interface TurnosBaseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(TurnosBase value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<TurnosBase> values);
    @Update void actualizar(TurnosBase value);
    @Delete void eliminar(TurnosBase value);
    @Query("SELECT * FROM turnosbase") List<TurnosBase> obtenerTodos();
    @Query("SELECT * FROM turnosbase WHERE idturnosbase = :id LIMIT 1") TurnosBase obtenerPorId(long id);
}
