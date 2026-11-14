package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "beneficios")
public class Beneficios {
    @PrimaryKey
    public long idbeneficio;
    public Boolean estado;
    public Integer costepuntos;
    public Long categoria;
    public Long servicio;
}
