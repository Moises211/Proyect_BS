package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario;
import java.util.List;
@Dao public interface HorarioAtencionDiarioDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(HorarioAtencionDiario value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<HorarioAtencionDiario> values);
    @Update void actualizar(HorarioAtencionDiario value);
    @Delete void eliminar(HorarioAtencionDiario value);
    @Query("SELECT * FROM horarioatenciondiario") List<HorarioAtencionDiario> obtenerTodos();
    @Query("SELECT * FROM horarioatenciondiario WHERE idhorarioatenciondiario = :id LIMIT 1") HorarioAtencionDiario obtenerPorId(long id);
}
