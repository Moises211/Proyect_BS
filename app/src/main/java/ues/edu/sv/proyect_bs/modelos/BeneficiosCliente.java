package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "beneficioscliente")
public class BeneficiosCliente {
    @PrimaryKey
    public long idbeneficioscliente;
    public String fechacanje;
    public long idbeneficio;
    public long idcliente;
}
