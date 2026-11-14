package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cronogramabarbero")
public class CronogramaBarbero {
    @PrimaryKey
    public long idcronogramabarbero;
    public String fecha;
    public boolean receso;
    public String horainicioreceso;
    public String horafinreceso;
    public boolean retirotemprano;
    public String horaretiro;
    public long idbarbero;
    public long idhorario;
}
