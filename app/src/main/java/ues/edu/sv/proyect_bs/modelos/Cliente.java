package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cliente")
public class Cliente {
    @PrimaryKey
    public long idcliente;
    public Boolean estado;
    public Long idpersona;
    public Long preferencia_especialidad;
}
