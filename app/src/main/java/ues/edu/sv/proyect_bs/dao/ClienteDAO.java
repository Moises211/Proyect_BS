package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import java.util.List;
@Dao public interface ClienteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Cliente value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Cliente> values);
    @Update void actualizar(Cliente value);
    @Delete void eliminar(Cliente value);
    @Query("SELECT * FROM cliente") List<Cliente> obtenerTodos();
    @Query("SELECT * FROM cliente WHERE idcliente = :id LIMIT 1") Cliente obtenerPorId(long id);
}
