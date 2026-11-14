package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.HorarioDiaBase;
import java.util.List;
@Dao public interface HorarioDiaBaseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(HorarioDiaBase value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<HorarioDiaBase> values);
    @Update void actualizar(HorarioDiaBase value);
    @Delete void eliminar(HorarioDiaBase value);
    @Query("SELECT * FROM horariodiabase") List<HorarioDiaBase> obtenerTodos();
    @Query("SELECT * FROM horariodiabase WHERE idhorariodia = :id LIMIT 1") HorarioDiaBase obtenerPorId(long id);
}
