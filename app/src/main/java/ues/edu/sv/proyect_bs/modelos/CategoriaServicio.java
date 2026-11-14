package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categoria_servicio")
public class CategoriaServicio {
    @PrimaryKey
    public long idcategoria_servicio;
    public String nombre;
}
