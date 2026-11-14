package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.TipoDocumento;
import java.util.List;
@Dao public interface TipoDocumentoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(TipoDocumento value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<TipoDocumento> values);
    @Update void actualizar(TipoDocumento value);
    @Delete void eliminar(TipoDocumento value);
    @Query("SELECT * FROM tipodocumento") List<TipoDocumento> obtenerTodos();
    @Query("SELECT * FROM tipodocumento WHERE iddocumento = :id LIMIT 1") TipoDocumento obtenerPorId(long id);
}
