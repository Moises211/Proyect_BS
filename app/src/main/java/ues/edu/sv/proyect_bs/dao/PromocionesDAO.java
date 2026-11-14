package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Promociones;
import java.util.List;
@Dao public interface PromocionesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Promociones value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Promociones> values);
    @Update void actualizar(Promociones value);
    @Delete void eliminar(Promociones value);
    @Query("SELECT * FROM promociones") List<Promociones> obtenerTodos();
    @Query("SELECT * FROM promociones WHERE idpromocion = :id LIMIT 1") Promociones obtenerPorId(long id);
}
