package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "admins")
public class Admins {
    @PrimaryKey
    public long idadmin;
    public Boolean estado;
    public String correoelectronico;
    public Long idpersona;
}
