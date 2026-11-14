package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "horarioatencionbase")
public class HorarioAtencionBase {
    @PrimaryKey
    public long idhorarioatencionbase;
    public String apertura;
    public String cierre;
    public Boolean recesobase;
    public String inicioreceso;
    public String finreceso;
}
