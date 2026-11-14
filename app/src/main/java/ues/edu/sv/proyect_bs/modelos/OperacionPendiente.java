package ues.edu.sv.proyect_bs.modelos;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "operaciones_pendientes")
public class OperacionPendiente {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String recurso;
    public String metodo;
    public Long registroId;
    public String payload;
    public String estado;
    public long creadoEn;
    public int intentos;

    public OperacionPendiente(String recurso, String metodo, Long registroId,
                              String payload, String estado, long creadoEn) {
        this.recurso = recurso;
        this.metodo = metodo;
        this.registroId = registroId;
        this.payload = payload;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.intentos = 0;
    }
}
