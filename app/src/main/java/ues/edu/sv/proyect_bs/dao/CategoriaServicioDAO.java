package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import java.util.List;
@Dao public interface CategoriaServicioDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(CategoriaServicio value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<CategoriaServicio> values);
    @Update void actualizar(CategoriaServicio value);
    @Delete void eliminar(CategoriaServicio value);
    @Query("SELECT * FROM categoria_servicio") List<CategoriaServicio> obtenerTodos();
    @Query("SELECT * FROM categoria_servicio WHERE idcategoria_servicio = :id LIMIT 1") CategoriaServicio obtenerPorId(long id);
}
