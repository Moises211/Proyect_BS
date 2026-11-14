package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Servicios;
import java.util.List;
@Dao public interface ServiciosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Servicios value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Servicios> values);
    @Update void actualizar(Servicios value);
    @Delete void eliminar(Servicios value);
    @Query("SELECT * FROM servicios") List<Servicios> obtenerTodos();
    @Query("SELECT * FROM servicios WHERE idservicio = :id LIMIT 1") Servicios obtenerPorId(long id);
}
