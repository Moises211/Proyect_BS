package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "turnobasebarbero")
public class TurnoBaseBarbero {
    @PrimaryKey
    public long idturno_barbero;
    public Boolean receso;
    public String horarioinicioreceso;
    public String horafinreceso;
    public long idbarbero;
    public long idhorarios;
}
