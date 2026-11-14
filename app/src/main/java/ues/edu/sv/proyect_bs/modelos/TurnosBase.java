package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "turnosbase")
public class TurnosBase {
    @PrimaryKey
    public long idturnosbase;
    public String enunciadoturno;
    public String inicio;
    public String fin;
    public Boolean estado;
}
