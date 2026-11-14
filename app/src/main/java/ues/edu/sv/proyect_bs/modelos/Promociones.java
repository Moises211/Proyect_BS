package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "promociones")
public class Promociones {
    @PrimaryKey
    public long idpromocion;
    public String imagenpromocionlink;
    public String descripcion;
    public String producto_descuento;
    public Boolean notificacion;
    public String mensajenotificacion;
    public long tipo;
}
