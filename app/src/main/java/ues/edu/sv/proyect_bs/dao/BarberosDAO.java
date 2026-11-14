package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import java.util.List;
@Dao public interface BarberosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Barberos value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Barberos> values);
    @Update void actualizar(Barberos value);
    @Delete void eliminar(Barberos value);
    @Query("SELECT * FROM barberos") List<Barberos> obtenerTodos();
    @Query("SELECT * FROM barberos WHERE idbarberos = :id LIMIT 1") Barberos obtenerPorId(long id);
}
