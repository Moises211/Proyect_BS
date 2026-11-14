package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionBase;
import java.util.List;
@Dao public interface HorarioAtencionBaseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(HorarioAtencionBase value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<HorarioAtencionBase> values);
    @Update void actualizar(HorarioAtencionBase value);
    @Delete void eliminar(HorarioAtencionBase value);
    @Query("SELECT * FROM horarioatencionbase") List<HorarioAtencionBase> obtenerTodos();
    @Query("SELECT * FROM horarioatencionbase WHERE idhorarioatencionbase = :id LIMIT 1") HorarioAtencionBase obtenerPorId(long id);
}
