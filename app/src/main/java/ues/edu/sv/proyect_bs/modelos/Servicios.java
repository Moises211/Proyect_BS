package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "servicios")
public class Servicios {
    @PrimaryKey
    public long idservicio;
    public String nombre_servicio;
    public Double precio;
    public Integer duracion;
    public Boolean estado;
    public Long idcategoria_servicio;
}
