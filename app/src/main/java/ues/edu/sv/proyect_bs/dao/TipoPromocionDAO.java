package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.TipoPromocion;
import java.util.List;
@Dao public interface TipoPromocionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(TipoPromocion value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<TipoPromocion> values);
    @Update void actualizar(TipoPromocion value);
    @Delete void eliminar(TipoPromocion value);
    @Query("SELECT * FROM tipopromocion") List<TipoPromocion> obtenerTodos();
    @Query("SELECT * FROM tipopromocion WHERE idtipopromocion = :id LIMIT 1") TipoPromocion obtenerPorId(long id);
}
