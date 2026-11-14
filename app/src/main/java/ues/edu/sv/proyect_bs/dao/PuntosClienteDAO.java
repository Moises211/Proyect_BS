package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import java.util.List;
@Dao public interface PuntosClienteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(PuntosCliente value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<PuntosCliente> values);
    @Update void actualizar(PuntosCliente value);
    @Delete void eliminar(PuntosCliente value);
    @Query("SELECT * FROM puntoscliente") List<PuntosCliente> obtenerTodos();
    @Query("SELECT * FROM puntoscliente WHERE idcliente = :id LIMIT 1") PuntosCliente obtenerPorId(long id);
}
