package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tipodocumento")
public class TipoDocumento {
    @PrimaryKey
    public long iddocumento;
    public String nombredocumento;
}
