package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import java.util.List;
@Dao public interface CronogramaBarberoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(CronogramaBarbero value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<CronogramaBarbero> values);
    @Update void actualizar(CronogramaBarbero value);
    @Delete void eliminar(CronogramaBarbero value);
    @Query("SELECT * FROM cronogramabarbero") List<CronogramaBarbero> obtenerTodos();
    @Query("SELECT * FROM cronogramabarbero WHERE idcronogramabarbero = :id LIMIT 1") CronogramaBarbero obtenerPorId(long id);
}
