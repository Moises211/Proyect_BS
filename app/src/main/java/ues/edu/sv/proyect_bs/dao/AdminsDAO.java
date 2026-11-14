package ues.edu.sv.proyect_bs.dao;
import androidx.room.*;
import ues.edu.sv.proyect_bs.modelos.Admins;
import java.util.List;
@Dao public interface AdminsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertar(Admins value);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertarTodos(List<Admins> values);
    @Update void actualizar(Admins value);
    @Delete void eliminar(Admins value);
    @Query("SELECT * FROM admins") List<Admins> obtenerTodos();
    @Query("SELECT * FROM admins WHERE idadmin = :id LIMIT 1") Admins obtenerPorId(long id);
}
