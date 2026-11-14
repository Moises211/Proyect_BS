package ues.edu.sv.proyect_bs.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ues.edu.sv.proyect_bs.dao.*;
import ues.edu.sv.proyect_bs.modelos.*;

@Database(
        entities = {
                Persona.class, Admins.class, Barberos.class, Cliente.class,
                CategoriaServicio.class, Servicios.class, Beneficios.class,
                BeneficiosCliente.class, CitasCliente.class, CronogramaBarbero.class,
                HorarioAtencionBase.class, HorarioAtencionDiario.class,
                HorarioDiaBase.class, Promociones.class, PuntosCliente.class,
                TarifaPuntos.class, TipoDocumento.class, TipoPromocion.class,
                TurnoBaseBarbero.class, TurnosBase.class, OperacionPendiente.class
        },
        version = 2,
        exportSchema = false)
public abstract class AppDB extends RoomDatabase {
    private static AppDB instancia;

    public abstract PersonaDAO personaDAO();
    public abstract AdminsDAO adminsDAO();
    public abstract BarberosDAO barberosDAO();
    public abstract ClienteDAO clienteDAO();
    public abstract CategoriaServicioDAO categoriaServicioDAO();
    public abstract ServiciosDAO serviciosDAO();
    public abstract BeneficiosDAO beneficiosDAO();
    public abstract BeneficiosClienteDAO beneficiosClienteDAO();
    public abstract CitasClienteDAO citasClienteDAO();
    public abstract CronogramaBarberoDAO cronogramaBarberoDAO();
    public abstract HorarioAtencionBaseDAO horarioAtencionBaseDAO();
    public abstract HorarioAtencionDiarioDAO horarioAtencionDiarioDAO();
    public abstract HorarioDiaBaseDAO horarioDiaBaseDAO();
    public abstract PromocionesDAO promocionesDAO();
    public abstract PuntosClienteDAO puntosClienteDAO();
    public abstract TarifaPuntosDAO tarifaPuntosDAO();
    public abstract TipoDocumentoDAO tipoDocumentoDAO();
    public abstract TipoPromocionDAO tipoPromocionDAO();
    public abstract TurnoBaseBarberoDAO turnoBaseBarberoDAO();
    public abstract TurnosBaseDAO turnosBaseDAO();
    public abstract OperacionPendienteDAO operacionPendienteDAO();

    public static synchronized AppDB getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDB.class,
                            "pbs_local.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instancia;
    }
}
