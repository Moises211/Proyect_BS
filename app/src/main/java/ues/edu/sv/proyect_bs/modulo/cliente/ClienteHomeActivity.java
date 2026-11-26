package ues.edu.sv.proyect_bs.modulo.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ues.edu.sv.proyect_bs.GeneralidadesActivity;
import ues.edu.sv.proyect_bs.R;
import ues.edu.sv.proyect_bs.SessionManager;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.CitasCliente;
import ues.edu.sv.proyect_bs.modelos.Cliente;
import ues.edu.sv.proyect_bs.modelos.Barberos;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.PuntosCliente;
import ues.edu.sv.proyect_bs.modelos.Servicios;

public class ClienteHomeActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_home);
        setupNavigation();
        cargarDatos();
    }

    private void cargarDatos() {
        long personId = SessionManager.getPersonId(this);
        executor.execute(() -> {
            AppDB db = AppDB.getInstance(this);

            Persona persona = db.personaDAO().obtenerPorId(personId);

            Cliente cliente = db.clienteDAO().obtenerTodos().stream()
                    .filter(c -> c.idpersona != null && c.idpersona == personId)
                    .findFirst().orElse(null);

            long clienteId = (cliente != null) ? cliente.idcliente : -1;

            List<CitasCliente> todasCitas = db.citasClienteDAO().obtenerTodos();
            List<CitasCliente> misCitas = todasCitas.stream()
                    .filter(c -> c.idcliente == clienteId)
                    .collect(java.util.stream.Collectors.toList());

            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            CitasCliente proxima = misCitas.stream()
                    .filter(c -> c.fecha != null && c.fecha.compareTo(hoy) >= 0
                            && !"COMPLETADA".equalsIgnoreCase(c.estado))
                    .min((a, b) -> {
                        int cmp = a.fecha.compareTo(b.fecha);
                        if (cmp != 0) return cmp;
                        if (a.hora == null) return 1;
                        if (b.hora == null) return -1;
                        return a.hora.compareTo(b.hora);
                    }).orElse(null);

            String nombreServicio = "";
            String nombreBarbero = "";
            if (proxima != null) {
                Servicios srv = db.serviciosDAO().obtenerPorId(proxima.idservicio);
                nombreServicio = (srv != null) ? srv.nombre_servicio : "Servicio";
                Barberos barb = db.barberosDAO().obtenerTodos().stream()
                        .filter(b -> b.idbarberos == proxima.idbarbero)
                        .findFirst().orElse(null);
                if (barb != null && barb.idpersona != null) {
                    Persona pb = db.personaDAO().obtenerPorId(barb.idpersona);
                    nombreBarbero = (pb != null) ? pb.nombres + " " + pb.apellidos : "Barbero";
                }
            }

            PuntosCliente pts = (clienteId >= 0)
                    ? db.puntosClienteDAO().obtenerPorId(clienteId) : null;
            long puntos = (pts != null && pts.cantidadpuntos != null) ? pts.cantidadpuntos : 0;

            long totalPromos = db.promocionesDAO().obtenerTodos().size();

            final String nombre = (persona != null) ? persona.nombres.split(" ")[0] : "Cliente";
            final int totalCitas = misCitas.size();
            final long puntosF = puntos;
            final long promosF = totalPromos;
            final CitasCliente proximaF = proxima;
            final String srvNomF = nombreServicio;
            final String barbNomF = nombreBarbero;

            runOnUiThread(() -> poblarUI(nombre, puntosF, totalCitas, promosF, proximaF, srvNomF, barbNomF));
        });
    }

    private void poblarUI(String nombre, long puntos, int totalCitas, long promos,
                          CitasCliente proxima, String srvNom, String barbNom) {
        ((TextView) findViewById(R.id.tv_saludo)).setText("Hola, " + nombre);
        ((TextView) findViewById(R.id.tv_puntos)).setText(String.valueOf(puntos));
        ((TextView) findViewById(R.id.tv_total_citas)).setText(String.valueOf(totalCitas));
        ((TextView) findViewById(R.id.tv_promos)).setText(String.valueOf(promos));

        View cardCita   = findViewById(R.id.card_proxima_cita);
        View cardSinCita = findViewById(R.id.card_sin_cita);

        if (proxima != null) {
            cardSinCita.setVisibility(View.GONE);
            cardCita.setVisibility(View.VISIBLE);

            String fecha = formatearFecha(proxima.fecha);
            String hora  = trimHora(proxima.hora);
            ((TextView) findViewById(R.id.tv_proxima_fecha)).setText(fecha + " · " + hora);
            ((TextView) findViewById(R.id.tv_proxima_detalle)).setText(srvNom + " con " + barbNom);

            final CitasCliente cita = proxima;
            findViewById(R.id.btn_ver_detalle).setOnClickListener(v -> {
                Intent i = new Intent(this, ClienteAppointmentsActivity.class);
                startActivity(i);
            });
            findViewById(R.id.btn_reprogramar).setOnClickListener(v -> {
                startActivity(new Intent(this, ClienteServicesActivity.class));
            });
        } else {
            cardCita.setVisibility(View.GONE);
            cardSinCita.setVisibility(View.VISIBLE);
        }
    }

    private void setupNavigation() {
        findViewById(R.id.nav_inicio).setOnClickListener(v -> cargarDatos());
        findViewById(R.id.nav_citas).setOnClickListener(v ->
                startActivity(new Intent(this, ClienteAppointmentsActivity.class)));
        findViewById(R.id.nav_servicios).setOnClickListener(v ->
                startActivity(new Intent(this, ClientePromocionesActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(this, ClientePerfilActivity.class)));
        findViewById(R.id.row_servicios).setOnClickListener(v ->
                startActivity(new Intent(this, ClienteServicesActivity.class)));
        findViewById(R.id.row_beneficios).setOnClickListener(v ->
                startActivity(new Intent(this, ClienteBeneficiosActivity.class)));
    }

    private void cerrarSesion() {
        SessionManager.clear(this);
        startActivity(new Intent(this, GeneralidadesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private String trimHora(String hora) {
        if (hora == null) return "—";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    private String formatearFecha(String fecha) {
        if (fecha == null) return "—";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha);
            return new SimpleDateFormat("EEE d 'de' MMM", new Locale("es", "SV")).format(d);
        } catch (Exception e) {
            return fecha;
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
