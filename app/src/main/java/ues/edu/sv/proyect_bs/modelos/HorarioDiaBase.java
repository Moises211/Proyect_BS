package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "horariodiabase")
public class HorarioDiaBase {
    @PrimaryKey
    public long idhorariodia;
    public Boolean estado;
    public String apertura;
    public String cierre;
    public boolean receso;
    public String inicio;
    public String fin;
    public String dia;
}
