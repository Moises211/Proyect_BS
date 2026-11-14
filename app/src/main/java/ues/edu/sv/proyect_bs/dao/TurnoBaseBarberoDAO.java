package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.TurnoBaseBarbero;
import java.util.List;
@Dao public interface TurnoBaseBarberoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(TurnoBaseBarbero value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<TurnoBaseBarbero> values);
    @Update void actualizar(TurnoBaseBarbero value);
    @Delete void eliminar(TurnoBaseBarbero value);
    @Query("SELECT * FROM turnobasebarbero") List<TurnoBaseBarbero> obtenerTodos();
    @Query("SELECT * FROM turnobasebarbero WHERE idturno_barbero = :id LIMIT 1") TurnoBaseBarbero obtenerPorId(long id);
}
