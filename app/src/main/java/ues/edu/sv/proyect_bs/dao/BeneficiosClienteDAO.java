package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.BeneficiosCliente;
import java.util.List;
@Dao public interface BeneficiosClienteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(BeneficiosCliente value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<BeneficiosCliente> values);
    @Update void actualizar(BeneficiosCliente value);
    @Delete void eliminar(BeneficiosCliente value);
    @Query("SELECT * FROM beneficioscliente") List<BeneficiosCliente> obtenerTodos();
    @Query("SELECT * FROM beneficioscliente WHERE idbeneficioscliente = :id LIMIT 1") BeneficiosCliente obtenerPorId(long id);
}
