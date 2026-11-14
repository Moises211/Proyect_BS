package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import java.util.List;
@Dao public interface CitasClienteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(CitasCliente value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<CitasCliente> values);
    @Update void actualizar(CitasCliente value);
    @Delete void eliminar(CitasCliente value);
    @Query("SELECT * FROM citasclientes") List<CitasCliente> obtenerTodos();
    @Query("SELECT * FROM citasclientes WHERE idcitas = :id LIMIT 1") CitasCliente obtenerPorId(long id);
}
