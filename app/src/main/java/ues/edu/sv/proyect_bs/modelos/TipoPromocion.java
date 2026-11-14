package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tipopromocion")
public class TipoPromocion {
    @PrimaryKey
    public long idtipopromocion;
    public String tipo;
}
