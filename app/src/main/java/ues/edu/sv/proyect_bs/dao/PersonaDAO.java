package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Persona;
import java.util.List;
@Dao public interface PersonaDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Persona value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Persona> values);
    @Update void actualizar(Persona value);
    @Delete void eliminar(Persona value);
    @Query("SELECT * FROM persona") List<Persona> obtenerTodos();
    @Query("SELECT * FROM persona WHERE idpersona = :id LIMIT 1") Persona obtenerPorId(long id);
}
