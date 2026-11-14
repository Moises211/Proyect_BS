package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "barberos")
public class Barberos {
    @PrimaryKey
    public long idbarberos;
    public String foto_perfil;
    public Double salario;
    public Boolean estado;
    public String correo_electronico;
    public Long idpersona;
    public Long especialidad;
}
