package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "puntoscliente")
public class PuntosCliente {
    @PrimaryKey
    public long idcliente;
    public Integer cantidadpuntos;
}
