package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "citasclientes")
public class CitasCliente {
    @PrimaryKey
    public long idcitas;
    public String fecha;
    public String hora;
    public String estado;
    public long idbarbero;
    public long idcliente;
    public long idservicio;
}
