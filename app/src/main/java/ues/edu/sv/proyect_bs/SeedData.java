package ues.edu.sv.proyect_bs;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.Beneficios;
import ues.edu.sv.proyect_bs.modelos.CategoriaServicio;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.CronogramaBarbero;
import ues.edu.sv.proyect_bs.modelos.HorarioAtencionBase;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.Promociones;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import ues.edu.sv.proyect_bs.modelos.Servicios;
import ues.edu.sv.proyect_bs.modelos.TipoPromocion;

final class SeedData {

    interface Callback { void onDone(); }

    static void inicializarSiVacio(Context context, Runnable onMain, android.os.Handler mainHandler) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(() -> {
            AppDB db = AppDB.getInstance(context);
            if (!db.personaDAO().obtenerTodos().isEmpty()) {
                mainHandler.post(onMain);
                exec.shutdown();
                return;
            }
            insertar(db);
            mainHandler.post(onMain);
            exec.shutdown();
        });
    }

    private static void insertar(AppDB db) {
        // ── Personas ────────────────────────────────────────────────────────
        Persona p1 = new Persona();
        p1.idpersona = 1;
        p1.nombres = "Ana María";
        p1.apellidos = "López Martínez";
        p1.correo_electronico = "cliente@goodcuts.com";
        p1.numero_celular = "78901234";
        p1.fecha_nacimiento = "1995-06-15";
        db.personaDAO().insertar(p1);

        Persona p2 = new Persona();
        p2.idpersona = 2;
        p2.nombres = "Carlos Eduardo";
        p2.apellidos = "Mendoza Rivas";
        p2.correo_electronico = "barbero@goodcuts.com";
        p2.numero_celular = "72345678";
        p2.fecha_nacimiento = "1990-03-22";
        db.personaDAO().insertar(p2);

        // ── Cliente ─────────────────────────────────────────────────────────
        Cliente c = new Cliente();
        c.idcliente = 1;
        c.idpersona = 1L;
        c.estado = true;
        c.preferencia_especialidad = 1L;
        db.clienteDAO().insertar(c);

        // ── CategoriaServicio ───────────────────────────────────────────────
        CategoriaServicio cat = new CategoriaServicio();
        cat.idcategoria_servicio = 1;
        cat.nombre = "Corte y Estilo";
        db.categoriaServicioDAO().insertar(cat);

        // ── Barberos ────────────────────────────────────────────────────────
        Barberos b = new Barberos();
        b.idbarberos = 2;
        b.idpersona = 2L;
        b.especialidad = 1L;
        b.estado = true;
        b.correo_electronico = "barbero@goodcuts.com";
        b.salario = 450.0;
        db.barberosDAO().insertar(b);

        // ── Servicios ───────────────────────────────────────────────────────
        db.serviciosDAO().insertar(servicio(1, "Corte Clásico", 8.0, 30, 1L));
        db.serviciosDAO().insertar(servicio(2, "Degradado Moderno", 12.0, 45, 1L));
        db.serviciosDAO().insertar(servicio(3, "Arreglo de Barba", 5.0, 20, 1L));
        db.serviciosDAO().insertar(servicio(4, "Combo Corte + Barba", 15.0, 50, 1L));

        // ── TipoPromocion ───────────────────────────────────────────────────
        TipoPromocion tp = new TipoPromocion();
        tp.idtipopromocion = 1;
        tp.tipo = "Descuento";
        db.tipoPromocionDAO().insertar(tp);

        // ── Promociones ─────────────────────────────────────────────────────
        db.promocionesDAO().insertar(promocion(1,
                "20% de descuento en tu próximo Corte Clásico",
                "Corte Clásico", 1L));
        db.promocionesDAO().insertar(promocion(2,
                "Combo Corte + Barba por solo $15 este mes",
                "Combo Especial", 1L));
        db.promocionesDAO().insertar(promocion(3,
                "Arreglo de Barba gratis al acumular 200 puntos",
                "Arreglo de Barba", 1L));

        // ── Beneficios ──────────────────────────────────────────────────────
        db.beneficiosDAO().insertar(beneficio(1, 100, 1L, 1L));
        db.beneficiosDAO().insertar(beneficio(2, 150, 1L, 3L));
        db.beneficiosDAO().insertar(beneficio(3, 250, 1L, 2L));

        // ── PuntosCliente ───────────────────────────────────────────────────
        PuntosCliente pts = new PuntosCliente();
        pts.idcliente = 1;
        pts.cantidadpuntos = 150;
        db.puntosClienteDAO().insertar(pts);

        // ── HorarioAtencionBase ─────────────────────────────────────────────
        HorarioAtencionBase horBase = new HorarioAtencionBase();
        horBase.idhorarioatencionbase = 1;
        horBase.apertura = "09:00";
        horBase.cierre = "18:00";
        horBase.recesobase = true;
        horBase.inicioreceso = "13:00";
        horBase.finreceso = "14:00";
        db.horarioAtencionBaseDAO().insertar(horBase);

        // ── CronogramaBarbero (lun–vie de la semana actual) ─────────────────
        Calendar lunes = Calendar.getInstance();
        int dow = lunes.get(Calendar.DAY_OF_WEEK);
        int offset = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
        lunes.add(Calendar.DAY_OF_MONTH, offset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 5; i++) {
            Calendar dia = (Calendar) lunes.clone();
            dia.add(Calendar.DAY_OF_MONTH, i);
            CronogramaBarbero crono = new CronogramaBarbero();
            crono.idcronogramabarbero = 10 + i;
            crono.idbarbero = 2L;
            crono.idhorario = 1L;
            crono.fecha = sdf.format(dia.getTime());
            crono.receso = true;
            crono.horainicioreceso = "13:00";
            crono.horafinreceso = "14:00";
            crono.retirotemprano = false;
            crono.horaretiro = null;
            db.cronogramaBarberoDAO().insertar(crono);
        }

        // ── CitasCliente (hoy) ──────────────────────────────────────────────
        String hoy = sdf.format(new Date());
        db.citasClienteDAO().insertar(cita(1, hoy, "09:00", "COMPLETADA", 2L, 1L, 1L));
        db.citasClienteDAO().insertar(cita(2, hoy, "10:00", "EN_ATENCION",  2L, 1L, 2L));
        db.citasClienteDAO().insertar(cita(3, hoy, "11:00", "PENDIENTE",    2L, 1L, 3L));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Servicios servicio(long id, String nombre, double precio, int duracion, Long cat) {
        Servicios s = new Servicios();
        s.idservicio = id;
        s.nombre_servicio = nombre;
        s.precio = precio;
        s.duracion = duracion;
        s.estado = true;
        s.idcategoria_servicio = cat;
        return s;
    }

    private static Promociones promocion(long id, String desc, String producto, long tipo) {
        Promociones p = new Promociones();
        p.idpromocion = id;
        p.descripcion = desc;
        p.producto_descuento = producto;
        p.tipo = tipo;
        p.notificacion = false;
        return p;
    }

    private static Beneficios beneficio(long id, int puntos, Long cat, Long servicio) {
        Beneficios b = new Beneficios();
        b.idbeneficio = id;
        b.costepuntos = puntos;
        b.estado = true;
        b.categoria = cat;
        b.servicio = servicio;
        return b;
    }

    private static CitasCliente cita(long id, String fecha, String hora, String estado,
                                     long idbarbero, long idcliente, long idservicio) {
        CitasCliente cc = new CitasCliente();
        cc.idcitas = id;
        cc.fecha = fecha;
        cc.hora = hora;
        cc.estado = estado;
        cc.idbarbero = idbarbero;
        cc.idcliente = idcliente;
        cc.idservicio = idservicio;
        return cc;
    }
}
