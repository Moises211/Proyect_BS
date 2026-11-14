package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "horarioatenciondiario")
public class HorarioAtencionDiario {
    @PrimaryKey
    public long idhorarioatenciondiario;
    public String fecha;
    public Boolean estado;
    public String apertura;
    public String cierre;
    public boolean receso;
    public String inicio;
    public String fin;
    public String dia;
}
