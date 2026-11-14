package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "persona")
public class Persona {
    @PrimaryKey
    public long idpersona;
    public String nombres;
    public String apellidos;
    public String identificadordoc;
    public String correo_electronico;
    public String numero_celular;
    public String fecha_nacimiento;
    public Long iddocumento;
}
