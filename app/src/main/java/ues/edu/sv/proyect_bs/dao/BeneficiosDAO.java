package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import java.util.List;
@Dao public interface BeneficiosDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Beneficios value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Beneficios> values);
    @Update void actualizar(Beneficios value);
    @Delete void eliminar(Beneficios value);
    @Query("SELECT * FROM beneficios") List<Beneficios> obtenerTodos();
    @Query("SELECT * FROM beneficios WHERE idbeneficio = :id LIMIT 1") Beneficios obtenerPorId(long id);
}
