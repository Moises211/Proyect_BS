package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tarifapuntos")
public class TarifaPuntos {
    @PrimaryKey
    public long idtarifa;
    public Integer puntos;
    public Double tarifamonetaria;
}
